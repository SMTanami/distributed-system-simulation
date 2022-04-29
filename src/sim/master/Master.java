package sim.master;

import sim.master.cmcomms.ClientHandler;
import sim.master.cmcomms.ClientListener;
import sim.task.Task;
import sim.task.TaskA;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Master {

    private static final Map<Integer, ClientHandler> CLIENTS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, WorkerHandler> A_WORKERS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, WorkerHandler> B_WORKERS = Collections.synchronizedMap(new HashMap<>());
    private static final BlockingQueue<Task> COLLECTED_TASKS = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<Task> COMPLETED_TASKS = new ArrayBlockingQueue<>(100);

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

        // Get port number from CL args
        int portNumber = Integer.parseInt(args[0]);

        // Instantiate ServerSocket called host, set the ClientListener's host to it, and start the listener
        try(ServerSocket host = new ServerSocket(portNumber)) {
            MasterListener listener = new MasterListener(host);
            listener.start();
            
            Task nextTask;
            while ((nextTask = getCollectedTasks().poll()) != null) {
                WorkerHandler assignedWorker = assignWorker(nextTask);
                assignedWorker.setTask(nextTask);
            }
        }
    }

    /**
     * To be used privately by the master, this method iterates over stored Collector Threads that have been started
     * and removes them from the collection if they have been terminated.
     */
    private static void cleanTerminatedClients() {

        for (ClientHandler handler : CLIENTS.values())
        {
            if (handler.isTerminated()) {
                CLIENTS.remove(handler.getClientID());
            }
        }
    }

    /**
     * @return this Masters map of Client's ({@link ClientHandler}s)
     */
    public static Map<Integer, ClientHandler> getClients() {
        return CLIENTS;
    }
    
    public static Map<String, WorkerHandler> getAWorkers() {
        return A_WORKERS;
    }

    public static Map<String, WorkerHandler> getBWorkers() {
        return B_WORKERS;
    }

    public static BlockingQueue<Task> getCollectedTasks() { return COLLECTED_TASKS; }

    public static BlockingQueue<Task> getCompletedTasks() { return COMPLETED_TASKS; }
    
    public static WorkerHandler assignWorker(Task task) {
        WorkerHandler[] aArray = getAWorkers().values().toArray(new WorkerHandler[0]);
        WorkerHandler[] bArray = getBWorkers().values().toArray(new WorkerHandler[0]);

        if (task.getClass() == TaskA.class) {
            for (WorkerHandler handler : aArray) {
                if (!handler.isOccupied()) {
                    return handler;
                }
            }

            if (getCollectedTasks().size() > 5 * aArray.length || areNextSame(getCollectedTasks(), task, aArray.length)) {
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

            if (getCollectedTasks().size() > 5 * bArray.length || areNextSame(getCollectedTasks(), task, bArray.length)) {
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

    public static boolean areNextSame(BlockingQueue<Task> taskQueue, Task task, int length) {
        Task[] taskArray = taskQueue.toArray(new Task[0]);
        for (int i = 0; i < 5 * length; i++) {
            if (taskArray[i].getClass() != task.getClass()) {
                return false;
            }
        }
        return true;
    }
}
