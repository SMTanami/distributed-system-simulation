package sim.conductor;

import sim.comms.Receiver;
import sim.component.ComponentID;
import sim.conductor.comms.ClientHandler;
import sim.conductor.comms.WorkerHandler;
import sim.task.TASK_TYPE;
import sim.task.Task;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static sim.component.COMPONENT_TYPE.CLIENT;
import static sim.task.TASK_TYPE.A;

/**
 * The Conductor is the hub of the simulation. ALl components connect to the Conductor to send and receive tasks. It is
 * therefore the responsibility of this class to conduct the event flow of the simulation. In order to achieve this in
 * an asynchronous manner, the Conductor heavily leverages multithreading.
 * <p>
 * The threads that enable this behavior are: {@link ComponentListener}, and local Thread fields named: taskAssigner and
 * clientUpdater. The task assigner is the main function of the conductor. Using its algorithm, the conductor decides which
 * worker is assigned what task. The clientUpdater Thread is constantly getting completed tasks from workers and sending them
 * back to the appropriate {@link sim.client.Client}.
 * <p>
 * In order to know which Worker and Client to communicate with, the Conductor maintains a collection of ClientHandlers
 * and employs a {@link WorkerTracker} to keep track of WorkerHandlers, making use of their componentIDs to know which
 * specific Client or Worker to communicate to.
 */
public class Conductor {

    private final ServerSocket server;
    private final ComponentListener componentListener = new ComponentListener();
    private final WorkerTracker workerTracker = new WorkerTracker();
    private final Map<Integer, ClientHandler> clientHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private final BlockingQueue<Task> collectedTasks = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);

    private final Thread taskAssigner = new Thread(() -> {
        while (true) {
            try {
                Task nextTask = collectedTasks.take(); // Blocking call
                WorkerHandler assignedWorker = assignWorker(nextTask); // Blocking call
                System.out.printf("CONDUCTOR: Worker(%d) was assigned %s\n", assignedWorker.getComponentID().refID(), nextTask);
                assignedWorker.sendTask(nextTask);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Conductor was interrupted");
            }
        }
    });
    private final Thread clientUpdater = new Thread(() -> {
        try {
            Task completedTask;
            while ((completedTask = completedTasks.take()) != null) {
                clientHandlerMap.get(completedTask.clientID()).sendTask(completedTask);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("TaskConfirmer was interrupted");
        }
    });

    /**
     * @param serverSocket the server socket that all components will use to connect and communicate to
     */
    public Conductor(ServerSocket serverSocket) {
        this.server = serverSocket;
    }

    /**
     * This class will listen for clients and workers that are looking to connect to the given server socket. Upon establishing a connection,
     * a {@link ClientHandler} or a {@link WorkerHandler} will be created and placed within the {@link Conductor}s Map
     * of clients or the Conductors {@link WorkerTracker}.
     */
    private class ComponentListener extends Thread implements Receiver {

        /**
         * Listens for client sockets as they try to connect. Will immediately create and update the master with a new
         * {@link ClientHandler} to handle connection between the master and the newly connected client.
         */
        @Override
        public void run() {
            receive();
        }

        @Override
        public void receive() {
            while (!server.isClosed())
            {
                try {
                    Socket incomingComponentSocket = server.accept();
                    ObjectInputStream objIn = new ObjectInputStream(incomingComponentSocket.getInputStream());
                    ComponentID componentID = (ComponentID) objIn.readObject();

                    if (componentID.component_type() == CLIENT) {
                        System.out.println("CONDUCTOR: " + componentID + " connected...");
                        ClientHandler clientHandler = new ClientHandler(componentID,
                                objIn, new DataOutputStream(incomingComponentSocket.getOutputStream()));
                        clientHandler.setCollections(collectedTasks);
                        clientHandlerMap.put(componentID.refID(), clientHandler);
                        clientHandler.start();
                    }

                    else {
                        System.out.println("CONDUCTOR: COMPONENT RECEIVED " + componentID + " connected...");
                        TASK_TYPE workerType = (TASK_TYPE) objIn.readObject();
                        WorkerHandler workerHandler = new WorkerHandler(componentID, workerType, objIn,
                                new ObjectOutputStream(incomingComponentSocket.getOutputStream()));
                        workerHandler.setCompletedTaskQueue(completedTasks);
                        workerTracker.add(workerHandler);
                        workerHandler.register(workerTracker);
                        workerHandler.start();
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Starts all contained threads: {@link ComponentListener}, taskAssigner, and clientUpdater.
     */
    public void begin() {
        componentListener.start();
        taskAssigner.start();
        clientUpdater.start();
    }

    /**
     * This is the algorithm of the Conductor. Based on certain variables, the method will choose the best Worker to
     * work on the given task.
     * @param task the task that requires assignment
     * @return a WorkerHandler that will handle the communication with the chosen worker meant to complete the given task
     */
    private WorkerHandler assignWorker(Task task) {

        if (task.type() == A) {
            if (workerTracker.isAFree())
                return workerTracker.getAHandler();

            else if (collectedTasks.size() > 5 * workerTracker.aCount() && areNextSame(task, workerTracker.aCount())) {
                if (workerTracker.isBFree())
                    return workerTracker.getBHandler();
            }

            System.out.println("CONDUCTOR: Waiting on Worker of type A");
            return workerTracker.getAHandler();
        }

        else {
            if (workerTracker.isBFree())
                return workerTracker.getBHandler();

            else if (collectedTasks.size() > 5 * workerTracker.bCount() && areNextSame(task, workerTracker.bCount()))
                if (workerTracker.isBFree())
                    return workerTracker.getAHandler();

            System.out.println("CONDUCTOR: Waiting on Worker of type B");
            return workerTracker.getBHandler();
        }
    }

    /**
     * Method to be internally used by the Conductor. Tests to see if the next five tasks in the queue are of the same type
     * as the given queue.
     * @param task the task to be assigned a worker
     * @param length the length of the queue
     * @return true if the next five tasks in the queue are the same, false otherwise
     */
    private boolean areNextSame(Task task, int length) {
        AtomicBoolean result = new AtomicBoolean(true);
        completedTasks.stream().limit(5L * length).forEachOrdered(t -> {
            if (t.type() == task.type())
                result.set(false);
        });

        return result.get();
    }

    /**
     * Initiate the collection of clients and retrieval and processing of their tasks
     * @param args list of CL arguments. Should only contain a port number.
     * @throws IOException if an I/O error occurs when opening the soon-to-be-created ServerSocket
     */
    public static void main(String[] args) throws IOException {

        // Ensure a single argument is used when using this program
        if (args.length != 1){
            System.out.println("Server args: <portNumber>");
            System.exit(1);
        }

        Conductor conductor = new Conductor(new ServerSocket(Integer.parseInt(args[0])));
        conductor.begin();
    }
}
