package sim.master.cmcomms;

import sim.task.Task;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class handles the master's connection with a given client. Via a {@link TaskCollector} and {@link TaskConfirmer},
 * this call will store tasks it receives from the client as well as store completed tasks that have been processed by the
 * overarching program.
 */
public class ClientHandler {

    private final int myClientID;
    private boolean isTerminated = false;
    private final Socket myClientSocket;
    private final TaskCollector myCollector;
    private final TaskConfirmer myConfirmer;
    private final Queue<Task> collectedTasks = new ArrayDeque<>();
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);

    /**
     * Returns a ClientHandler to handle master-client communication
     * @param clientID the incoming client's ID
     * @param incomingClient the incoming client's socket
     */
    public ClientHandler(int clientID, Socket incomingClient) {
        myClientID = clientID;
        myClientSocket = incomingClient;
        myCollector = new TaskCollector();
        myConfirmer = new TaskConfirmer();
    }

    /**
     * Starts the Collector and Confirmer threads to begin collecting incoming tasks from the client and sending completed
     * tasks back to it.
     */
    public void start() {
        myCollector.start();
        myConfirmer.start();
    }

    /**
     * Closes socket used by the contained {@link TaskCollector} and {@link TaskConfirmer} and subsequently kill them
     * gracefully. Additionally, signals this ClientHandler as "terminated" for cleanup by master.
     * @throws IOException
     */
    public void terminate() throws IOException {
        myClientSocket.close();
        isTerminated = true;
    }

    /**
     * @return this ClientHandler's ClientID
     */
    public int getClientID() {
        return myClientID;
    }

    /**
     * @return the termination status of this ClientHandler. True if terminated, false otherwise.
     */
    public boolean isTerminated() {
        return isTerminated;
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

            try(ObjectInputStream objIn = new ObjectInputStream(myClientSocket.getInputStream())) {
                Task incomingTask;
                while ((incomingTask = (Task) objIn.readObject()) != null)
                {
                    collectedTasks.add(incomingTask);
                }
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
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

            try (DataOutputStream dataOut = new DataOutputStream(myClientSocket.getOutputStream()))
            {

                while (!myClientSocket.isClosed())
                {
                    dataOut.writeInt(completedTasks.take().getTaskID());
                }

            } catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }

        }
    }
}
