package sim.conductor;

import sim.client.Client;
import sim.component.ComponentID;
import sim.conductor.cwcomms.ClientHandler;
import sim.conductor.cwcomms.ComponentListener;
import sim.conductor.cwcomms.WorkerHandler;
import sim.task.Task;
import sim.task.TaskA;
import sim.worker.WorkerA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Conductor {

    private final ServerSocket myServer;
    private final ComponentListener componentListener;
    private final Map<Integer, ClientHandler> cHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, WorkerHandler> aWorkerMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, WorkerHandler> bWorkerMap = Collections.synchronizedMap(new HashMap<>());
    private final BlockingQueue<Task> collectedTasks = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);

    public Conductor(ServerSocket serverSocket) {
        this.myServer = serverSocket;
        this.componentListener = new ComponentListener(myServer);
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
                    Socket incomingComponent = myServer.accept();
                    ObjectInputStream objIn = new ObjectInputStream(incomingComponent.getInputStream());
                    ComponentID componentID = (ComponentID) objIn.readObject();

                    if (componentID.component() instanceof Client) {
                        ClientHandler clientHandler = new ClientHandler(componentID.refID(), incomingComponent);
                        clientHandler.setTaskCollection(collectedTasks);
                    }

                    if (componentID.component() instanceof WorkerA)
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void begin() {
        componentListener.start();
        assignTasks();
    }

    private void assignTasks() {

        while (true) {
            try {
                Task nextTask = getCollectedTasks().take();
                WorkerHandler assignedWorker = assignWorker(nextTask);
                assignedWorker.setTask(nextTask);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Conductor was interrupted");
            }
        }
    }

    /**
     * To be used privately by the master, this method iterates over stored Collector Threads that have been started
     * and removes them from the collection if they have been terminated.
     */
    private void cleanTerminatedClients() {

        for (ClientHandler handler : cHandlerMap.values())
        {
            if (handler.isTerminated()) {
                cHandlerMap.remove(handler.getClientID());
            }
        }
    }

    /**
     * @return this Masters map of Client's ({@link ClientHandler}s)
     */
    public Map<Integer, ClientHandler> getClients() {
        return cHandlerMap;
    }
    
    public Map<String, WorkerHandler> getAWorkers() {
        return aWorkerMap;
    }

    public Map<String, WorkerHandler> getBWorkers() {
        return bWorkerMap;
    }

    public BlockingQueue<Task> getCollectedTasks() { return collectedTasks; }

    public BlockingQueue<Task> getCompletedTasks() { return completedTasks; }
    
    private WorkerHandler assignWorker(Task task) {
        WorkerHandler[] aArray = getAWorkers().values().toArray(new WorkerHandler[0]);
        WorkerHandler[] bArray = getBWorkers().values().toArray(new WorkerHandler[0]);
        BlockingQueue<Task> collectedTasks = getCollectedTasks();

        if (task.getClass() == TaskA.class) {
            for (WorkerHandler handler : aArray) {
                if (!handler.isOccupied()) {
                    return handler;
                }
            }

            if (collectedTasks.size() > 5 * aArray.length && areNextSame(collectedTasks, task, aArray.length)) {
                for (WorkerHandler handler : bArray) {
                    if (!handler.isOccupied()) {
                        return handler;
                    }
                }
            }

            while (aArray[0].isOccupied());
            return aArray[0];
        }

        else {
            for (WorkerHandler handler : bArray) {
                if (!handler.isOccupied()) {
                    return handler;
                }
            }

            if (collectedTasks.size() > 5 * bArray.length && areNextSame(collectedTasks, task, bArray.length)) {
                for (WorkerHandler handler : aArray) {
                    if (!handler.isOccupied()) {
                        return handler;
                    }
                }
            }

            while (bArray[0].isOccupied());
            return bArray[0];
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
