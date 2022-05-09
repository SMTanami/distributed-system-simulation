package sim.conductor.comms;

import sim.client.Client;
import sim.comms.Receiver;
import sim.comms.Sender;
import sim.component.ComponentID;
import sim.conductor.Conductor;
import sim.task.Task;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class handles the {@link Conductor} connection with a given client. Via separate thread classes in: {@link TaskReceiver}
 * and {@link TaskSender},this class will store tasks it receives from the appropriate client in a collection for the
 * Conductor to oversee (this class will have a reference to that collection via DIP), as well as store completed tasks
 * that have been processed by the overarching program in order to send them back to the respective client.
 * <p>
 * Once the ClientHandler receives word from the Client to terminate their connection, the ClientHandler will automatically
 * shut down its client socket.
 */
public class ClientHandler {

    private final Socket mySocket;
    private final ComponentID myComponentID;
    private final TaskSender taskSender = new TaskSender();
    private final TaskReceiver taskReceiver = new TaskReceiver();
    private final BlockingQueue<Task> completedTasks = new ArrayBlockingQueue<>(100);
    private ObjectInputStream objIn;
    private ObjectOutputStream objOut;
    private BlockingQueue<Task> collectedTasks;

    /**
     * @param connectingComponentID the component ID of the client that this ClientHandler instance will be overseeing
     *                             responsibility for.
     * @param clientSocket the connecting {@link Client} socket used to communicate with the respective client
     * @param objIn the object input stream to be used to receive tasks from the connecting client
     */
    public ClientHandler(ComponentID connectingComponentID, Socket clientSocket, ObjectInputStream objIn) {
        this.myComponentID = connectingComponentID;
        this.mySocket = clientSocket;
        this.objIn = objIn;
        initializeStreams();
    }

    /**
     * Initializes an ObjectOutputStream for this ClientHandler instance to use to communicate with its respective client.
     */
    private void initializeStreams() {
        try {
            objOut = new ObjectOutputStream(mySocket.getOutputStream());
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts the {@link TaskReceiver} and {@link TaskSender} threads to begin collecting incoming tasks from the client and sending completed
     * tasks back to it.
     */
    public void start() {
        if (collectedTasks == null)
            throw new RuntimeException("Cannot start handler without setting task collection...");

        taskSender.start();
        taskReceiver.start();
    }

    /**
     * Closes client socket, effectively killing both the {@link TaskSender} and the {@link TaskReceiver}.
     */
    private void terminate() {
        try {
            mySocket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * While this method does not literally send a task to the connected client, it does add it to a BlockingQueue that
     * this ClientHandler's {@link TaskSender} instance will send back to the client. This is done in order to keep
     * things as asynchronous as possible.
     * @param completedTask the task that has completed and should be returned to the client
     */
    public void sendTask(Task completedTask) {
        completedTasks.add(completedTask);
    }


    /**
     * This method sets the overarching {@link Task} collection that the {@link sim.conductor.Conductor} will have access
     * to in order to facilitate further handling of having the tasks done and sent back to their respective clients.
     * @param collectedTasks the overarching Task collection that the Conductor oversees
     */
    public void setCollections(BlockingQueue<Task> collectedTasks) {
        this.collectedTasks = collectedTasks;
    }

    /**
     * @return the {@link ComponentID} belonging to this ClientHandler.
     */
    public ComponentID getComponentID() {
        return myComponentID;
    }

    /**
     * The TaskReceiver class is a private class that is leveraged by its parent ClientHandler class. In order to retrieve
     * and send tasks to and from clients concurrently, the TaskCollector assumes the responsibility of retrieving
     * tasks using the parent ClientHandler's client sockets input stream.
     * <p>
     * Once the client sends word to end communications, this class will terminate the client socket using the
     * {@code terminate} method.
     */
    private class TaskReceiver extends Thread implements Receiver {

        /**
         * Receives incoming tasks from this client socket and stores them in the parent ClientHandler's Task Store.
         */
        @Override
        public void run() {
            receive();
        }

        @Override
        public void receive() {
            try {
                Task incomingTask;
                while ((incomingTask = (Task) objIn.readObject()).taskID() != -1) {
                    collectedTasks.add(incomingTask);
                    System.out.println("CONDUCTOR: RECEIVED TASK " + incomingTask);
                }
                System.out.printf("CONDUCTOR: Termination request by client %d... terminating\n", incomingTask.clientID());
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
            finally {
                terminate();
            }
        }
    }

    /**
     * The TaskConfirmer class is a private class that is leveraged by its parent ClientHandler class. In order to retrieve
     * and send tasks to and from clients concurrently, the TaskConfirmer assumes the responsibility of  confirming
     * completed tasks with the given client via the parent ClientHandler's client sockets output stream.
     */
    private class TaskSender extends Thread implements Sender {

        /**
         * When a completed task becomes available in the parent ClientHolder's blocking queue, the TaskConfirmer will
         * send it's TaskID to the client to let it know of that tasks completion.
         */
        @Override
        public void run() {
            send();
        }

        @Override
        public void send() {
            try {
                Task completedTask;
                while ((completedTask = completedTasks.take()) != null) {
                    objOut.writeObject(completedTask);
                    System.out.println("CONDUCTOR: Confirmed " + completedTask + " with client");
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
