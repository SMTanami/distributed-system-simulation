package sim.conductor;

import sim.client.Client;
import sim.component.ComponentID;
import sim.conductor.cwcomms.ClientHandler;
import sim.conductor.cwcomms.WorkerHandler;
import sim.task.Task;
import sim.task.TaskA;
import sim.worker.Worker;
import sim.worker.WorkerA;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Conductor {

    private final ServerSocket myServer;
    private final ComponentListener componentListener = new ComponentListener();
    private final Map<Integer, ClientHandler> cHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<ComponentID, WorkerHandler> aWorkerMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<ComponentID, WorkerHandler> bWorkerMap = Collections.synchronizedMap(new HashMap<>());
    private final BlockingQueue<Task> collectedTasks = new ArrayBlockingQueue<>(100);
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);

    private final Thread taskAssigner = new Thread(() -> {
        while (true) {
            try {
                Task nextTask = collectedTasks.take(); // Blocking call
                WorkerHandler assignedWorker = assignWorker(nextTask);
                assignedWorker.setTask(nextTask);
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

                    if (componentID.component() instanceof Client) {
                        ClientHandler clientHandler = new ClientHandler(componentID.refID(), incomingComponentSocket);
                        clientHandler.setCollections(collectedTasks);
                        cHandlerMap.put(componentID.refID(), clientHandler);
                        clientHandler.start();
                    }

                    else {
                        WorkerHandler workerHandler = new WorkerHandler(componentID, incomingComponentSocket);
                        workerHandler.setCompletedTaskQueue(completedTasks);

                        if (componentID.component() instanceof WorkerA)
                            aWorkerMap.put(componentID, workerHandler);

                        else bWorkerMap.put(componentID, workerHandler);

                        workerHandler.start();
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void begin() {
        componentListener.start();
        taskAssigner.start();
        taskConfirmer.start();
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
    
    private WorkerHandler assignWorker(Task task) {

        if (task instanceof TaskA) {
            for (WorkerHandler handler : aWorkerMap.values()) {
                if (!handler.isOccupied()) {
                    return handler;
                }
            }

            if (collectedTasks.size() > 5 * aWorkerMap.size() && areNextSame(collectedTasks, task, aWorkerMap.size())) {
                for (WorkerHandler handler : bWorkerMap.values()) {
                    if (!handler.isOccupied()) {
                        return handler;
                    }
                }
            }


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

    private WorkerHandler allOccupied() {
        for (WorkerHandler handler : aWorkerMap.values()) {
            if (!handler.isOccupied())
                return handler;
        }

        return null;
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
