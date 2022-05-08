package sim.conductor;

import sim.client.Client;
import sim.component.ComponentID;
import sim.conductor.cwcomms.ClientHandler;
import sim.conductor.cwcomms.WorkerHandler;
import sim.task.Task;
import sim.task.TaskA;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static sim.component.COMPONENT_TYPE.CLIENT;

public class Conductor {

    private final ServerSocket myServer;
    private final ComponentListener componentListener = new ComponentListener();
    private final Map<Integer, ClientHandler> cHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private final WorkerTracker workerTracker = new WorkerTracker();
    private final BlockingQueue<Task> collectedTasks = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);

    private final Thread taskAssigner = new Thread(() -> {
        while (true) {
            try {
                Task nextTask = collectedTasks.take(); // Blocking call
                WorkerHandler assignedWorker = assignWorker(nextTask); // Blocking call
                System.out.println(assignedWorker + "was assigned " + nextTask);
                assignedWorker.sendTask(nextTask);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Conductor was interrupted");
            }
        }
    });
    private final Thread taskConfirmer = new Thread(() -> {
        try {
            Task completedTask;
            while ((completedTask = completedTasks.take()) != null) {
                for (ClientHandler clientHandler : cHandlerMap.values()) {
                    if (completedTask.getClientID() == clientHandler.getClientID())
                        clientHandler.sendTask(completedTask);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("TaskConfirmer was interrupted");
        }
    });

    public Conductor(ServerSocket serverSocket) {
        this.myServer = serverSocket;
    }

    /**
     * This class will listen for clients and workers that are looking to connect to the given server socket. Upon establishing a connection,
     * a {@link ClientHandler} or a {@link WorkerHandler} will be created and placed within the master's Map of clients or Map of workers.
     */
    private class ComponentListener extends Thread {

        /**
         * Listens for client sockets as they try to connect. Will immediately create and update the master with a new
         * {@link ClientHandler} to handle connection between the master and the newly connected client.
         */
        @Override
        public void run() {

            while (!myServer.isClosed())
            {
                try {
                    Socket incomingComponentSocket = myServer.accept();
                    ObjectInputStream objIn = new ObjectInputStream(incomingComponentSocket.getInputStream());
                    ComponentID componentID = (ComponentID) objIn.readObject();

                    if (componentID.component_type() == CLIENT) {
                        System.out.println("CONDUCTOR: COMPONENT RECEIVED (CLIENT)");
                        System.out.println("ComponentID: " + componentID);
                        ClientHandler clientHandler = new ClientHandler(componentID.refID(),
                                objIn, new DataOutputStream(incomingComponentSocket.getOutputStream()));
                        clientHandler.setCollections(collectedTasks);
                        cHandlerMap.put(componentID.refID(), clientHandler);
                        clientHandler.start();
                    }

                    else {
                        System.out.println("CONDUCTOR: COMPONENT RECEIVED (WORKER)");
                        System.out.println("ComponentID: " + componentID);
                        WorkerHandler workerHandler = new WorkerHandler(componentID, objIn,
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

    public void begin() throws InterruptedException{
        componentListener.start();
        taskAssigner.start();
        taskConfirmer.start();

        componentListener.join();
        taskAssigner.join();
        taskConfirmer.join();
    }

    
    private WorkerHandler assignWorker(Task task) {

        if (task instanceof TaskA) {
            if (workerTracker.isAFree())
                return workerTracker.getAHandler();

            else if (collectedTasks.size() > 5 * workerTracker.aCount() && areNextSame(collectedTasks, task, workerTracker.aCount())) {
                if (workerTracker.isBFree())
                    return workerTracker.getBHandler();
            }

            System.out.println("Waiting on AHandler");
            return workerTracker.getAHandler();
        }

        else {
            if (workerTracker.isBFree())
                return workerTracker.getBHandler();

            else if (collectedTasks.size() > 5 * workerTracker.bCount() && areNextSame(collectedTasks, task, workerTracker.bCount()))
                if (workerTracker.isBFree())
                    return workerTracker.getAHandler();

            System.out.println("Waiting on BHandler");
            return workerTracker.getBHandler();
        }
    }

    private boolean areNextSame(BlockingQueue<Task> taskQueue, Task task, int length) {
        Task[] taskArray = taskQueue.toArray(new Task[0]);
        for (int i = 0; i < 5 * length; i++) {
            if (taskArray[i].getClass() != task.getClass()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initiate the collection of clients and retrival and processing of their tasks
     * @param args list of CL arguments. Should only contain a port number.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        // Ensure a single argument is used when using this program
        if (args.length != 1){
            System.out.println("Server args: <portNumber>");
            System.exit(1);
        }

        Conductor conductor = new Conductor(new ServerSocket(Integer.parseInt(args[0])));
        //Conductor conductor = new Conductor(new ServerSocket(30121));
        conductor.begin();
    }
}
