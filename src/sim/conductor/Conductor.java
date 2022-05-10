package sim.conductor;

import sim.client.Client;
import sim.comms.Receiver;
import sim.component.ComponentID;
import sim.conductor.comms.ClientHandler;
import sim.conductor.comms.WorkerHandler;
import sim.task.TASK_TYPE;
import sim.task.Task;
import sim.worker.Worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static sim.component.COMPONENT_TYPE.CLIENT;
import static sim.task.TASK_TYPE.A;

/**
 * The Conductor is the hub of the simulation. ALl components connect to the Conductor to send and receive tasks. It is
 * therefore the responsibility of this class to conduct the event flow of the simulation. In order to achieve this in
 * an asynchronous manner, the Conductor heavily leverages multithreading.
 * <p>
 * The threads that enable this behavior are: {@link ComponentListener}, and local ExecutorService fields named: assignmentService and
 * updateService. The assignmentService executes the main function of the conductor. Using its algorithm, the conductor decides which
 * {@link sim.worker.Worker} is assigned what task. The updateService is constantly getting completed tasks from workers and sending them
 * back to the appropriate {@link sim.client.Client}.
 * <p>
 * In order to know which Worker and Client to communicate with, the Conductor maintains a collection of ClientHandlers
 * and employs a {@link WorkerTracker} to keep track of WorkerHandler, making use of their componentIDs to know which
 * specific Client or Worker to communicate to.
 */
public class Conductor {

    private final ServerSocket server;
    private final ComponentListener componentListener = new ComponentListener();
    private final WorkerTracker workerTracker = new WorkerTracker();
    private final Map<Integer, ClientHandler> clientHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private final BlockingQueue<Task> collectedTasks = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);
    private final ExecutorService assignmentService = Executors.newFixedThreadPool(2);
    private final ExecutorService updateService = Executors.newFixedThreadPool(1);

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
                        establishClientHandler(incomingComponentSocket, objIn, componentID);
                    }

                    else {
                        establishWorkerHandler(incomingComponentSocket, objIn, componentID);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Establishes a {@link WorkerHandler} that will assume responsibility for communicating to the connecting
         * {@link Worker} from this point forward.
         * @param incomingComponentSocket the connecting component socket
         * @param objIn the object input stream used to receive component information upon connection
         * @param componentID the components {@link ComponentID} used to identify the components Handler
         * @throws IOException any of the usual I/O exceptions
         * @throws ClassNotFoundException if the received object is not an instance of {@link TASK_TYPE}
         */
        private void establishWorkerHandler(Socket incomingComponentSocket, ObjectInputStream objIn, ComponentID componentID) throws IOException, ClassNotFoundException {
            System.out.println("CONDUCTOR: COMPONENT RECEIVED " + componentID + " connected...");
            TASK_TYPE workerType = (TASK_TYPE) objIn.readObject();
            WorkerHandler workerHandler = new WorkerHandler(componentID, incomingComponentSocket,
                    workerType, objIn);
            workerHandler.setCompletedTaskQueue(completedTasks);
            workerTracker.add(workerHandler);
            workerHandler.register(workerTracker);
            workerHandler.start();
        }

        /**
         * Establishes a {@link ClientHandler} that will assume responsibility for communicating to the connecting
         * {@link Client} from this point forward.
         * @param incomingComponentSocket the connecting component socket
         * @param objIn the object input stream used to receive component information upon connection
         * @param componentID the components {@link ComponentID} used to identify the components Handler
         */
        private void establishClientHandler(Socket incomingComponentSocket, ObjectInputStream objIn, ComponentID componentID) {
            System.out.println("CONDUCTOR: " + componentID + " connected...");
            ClientHandler clientHandler = new ClientHandler(componentID, incomingComponentSocket, objIn);
            clientHandler.setCollections(collectedTasks);
            clientHandlerMap.put(componentID.refID(), clientHandler);
            clientHandler.start();
        }
    }

    /**
     * Starts all contained threads and thread-pools: {@link ComponentListener}, the assignmentService and the updateService.
     */
    public void begin() {
        componentListener.start();
        assignmentService.execute(() -> {
            try {
                Task receivedTask;
                while ((receivedTask = collectedTasks.take()) != null) {
                    WorkerHandler assignedWorker = assignWorker(receivedTask); // Blocking call
                    System.out.printf("CONDUCTOR: Worker(%d) was assigned %s\n", assignedWorker.getComponentID().refID(), receivedTask);
                    assignedWorker.sendTask(receivedTask);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Conductor was interrupted");
            }
        });
        updateService.execute(() -> {
            try {
                Task completedTask;
                while ((completedTask = completedTasks.take()) != null) {
                    clientHandlerMap.get(completedTask.clientID()).sendTask(completedTask);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * This is the algorithm of the Conductor. Based on certain variables, the method will choose the best Worker to
     * work on the given task. (If only one type of Worker is connected to the Conductor, then a Worker of that type 
     * must be chosen).
     * <p>
     * Since a Worker whose type matches the type of a given task takes only 2 seconds to complete, while a
     * Worker whose type doesn't match the type of a given task takes 10 seconds to complete the task, the ideal choice
     * of Worker will be of a similar type to the given task. However, if no such Worker is available at the present
     * moment, a Worker of a different type may be chosen under the following circumstances:
     * <p>
     * 1) there are a sufficient number of tasks remaining so that all the Workers of the matching type would be occupied during the extra time that
     * the Worker whose type doesn't match can complete the task
     * <p>
     * 2) all of these tasks are of the same type as the given task
     * <p>
     * 3) a Worker of a different type is presently available. In the event that any of these conditions is false, the 
     * method will wait until a Worker of a similar type to the given task becomes available.
     * @param task the task that requires assignment
     * @return a WorkerHandler that will handle the communication with the chosen worker meant to complete the given task
     */
    private WorkerHandler assignWorker(Task task) {

        if (!workerTracker.isAConnected())
            return workerTracker.getBHandler();
        
        if (!workerTracker.isBConnected())
            return workerTracker.getAHandler();
        
        if (task.type() == A) {
            if (workerTracker.isAFree())
                return workerTracker.getAHandler();

            if (collectedTasks.size() > 5 * workerTracker.aCount() && areNextSame(task.type(), workerTracker.aCount())) {
                if (workerTracker.isBFree())
                    return workerTracker.getBHandler();
            }
            
            System.out.println("CONDUCTOR: Waiting on Worker of type A");
            return workerTracker.getAHandler();
        }

        else {
            if (workerTracker.isBFree())
                return workerTracker.getBHandler();

            if (collectedTasks.size() > 5 * workerTracker.bCount() && areNextSame(task.type(), workerTracker.bCount())) {
                if (workerTracker.isAFree())
                    return workerTracker.getAHandler();
            }
            
            System.out.println("CONDUCTOR: Waiting on Worker of type B");
            return workerTracker.getBHandler();
        }
    }

    /**
     * Method to be internally used by the Conductor. Tests to see if the next specified number of tasks in the queue 
     * are of the same type as the given task type.
     * @param taskType the task type to be compared
     * @param workersOfSimilarType the number of workers of a similar type to the taskType
     * @return true if the next specified number of tasks in the queue are of the same type as the task type, otherwise false
     */
    private boolean areNextSame(TASK_TYPE taskType, int workersOfSimilarType) {
        AtomicBoolean result = new AtomicBoolean(true);
        collectedTasks.stream().limit(5L * workersOfSimilarType).forEachOrdered(futureTask -> {
            if (futureTask.type() != taskType)
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
