package sim.conductor.cwcomms;

import sim.task.Task;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class handles the master's connection with a given client. Via a {@link TaskCollector} and {@link TaskConfirmer},
 * this call will store tasks it receives from the client as well as store completed tasks that have been processed by the
 * overarching program.
 */
public class ClientHandler {

    private final int myClientRefID;
    private final ObjectInputStream objIn;
    private final DataOutputStream dataOut;
    private final TaskConfirmer myConfirmer = new TaskConfirmer();
    private final TaskCollector myCollector = new TaskCollector();
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);

    private BlockingQueue<Task> collectedTasks;
    private boolean isTerminated = false;

    public ClientHandler(int clientID, ObjectInputStream objIn, DataOutputStream dataOut) {
        this.myClientRefID = clientID;
        this.objIn = objIn;
        this.dataOut = dataOut;
    }

    /**
     * Starts the Collector and Confirmer threads to begin collecting incoming tasks from the client and sending completed
     * tasks back to it.
     */
    public void start() {
        if (collectedTasks == null)
            throw new RuntimeException("Cannot start handler without setting task collection...");

        myConfirmer.start();
        myCollector.start();
    }

    public void sendTask(Task completedTask) {
        completedTasks.add(completedTask);
    }


    public void setCollections(BlockingQueue<Task> collectedTasks) {
        this.collectedTasks = collectedTasks;
    }

    /**
     * @return this ClientHandler's ClientID
     */
    public int getClientID() {
        return myClientRefID;
    }

    /**
     * @return the termination status of this ClientHandler. True if terminated, false otherwise.
     */
    public boolean isTerminated() {
        return isTerminated;
    }

    /**
     * Closes socket used by the contained {@link TaskCollector} and {@link TaskConfirmer} and subsequently kill them
     * gracefully. Additionally, signals this ClientHandler as "terminated" for cleanup by master.
     * @throws IOException
     */
    private void terminate() throws IOException {
        //myClientSocket.close();
        isTerminated = true;
    }

    /**
     * The TaskCollector class is a private class that is leveraged by its parent ClientHandler class. In order to retrieve
     * and send tasks to and from clients concurrently, the TaskCollector assumes the responsibility of concurrently retrieving
     * tasks using the parent ClientHandler's client socket.
     */
    private class TaskCollector extends Thread {

        /**
         * Receives incoming tasks from this client socket and stores them in the parent ClientHandler's Task Store.
         */
        @Override
        public void run() {
            System.out.println("Collector Started");
            try {
                Task incomingTask;
                while ((incomingTask = (Task) objIn.readObject()).getTaskID() != -1) {
                    collectedTasks.add(incomingTask);
                    System.out.println("CONDUCTOR: RECEIVED TASK " + incomingTask);
                }
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * The TaskConfirmer class is a private class that is leveraged by its parent ClientHandler class. In order to retrieve
     * and send tasks to and from clients concurrently, the TaskConfirmer assumes the responsibility of concurrently confirming
     * completed tasks with the given client via the parent ClientHandler's client socket.
     */
    private class TaskConfirmer extends Thread {

        /**
         * When a completed task becomes available in the parent ClientHolder's blocking queue, the TaskConfirmer will
         * send it's TaskID to the client to let it know of that tasks completion.
         */
        @Override
        public void run() {
            System.out.println("Confirmer Started");
            try {
                Task completedTask;
                while ((completedTask = completedTasks.take()) != null) {
                    dataOut.writeInt(completedTask.getTaskID());
                    System.out.println("CONDUCTOR: Confirmed " + completedTask + " with client");
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
