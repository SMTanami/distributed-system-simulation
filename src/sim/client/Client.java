package sim.client;

import sim.comms.Receiver;
import sim.comms.Sender;
import sim.component.ComponentID;
import sim.conductor.Conductor;
import sim.task.Task;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import static sim.component.COMPONENT_TYPE.CLIENT;
import static sim.task.TASK_TYPE.A;
import static sim.task.TASK_TYPE.B;

/**
 * This class acts as a Client in a distributed system. It initializes tasks and utilizes multithreading to send and
 * receive them to a corresponding master class.
 * <p>
 * Once the Client has been begun, it initially notifies the Conductor of its request to connect to it. Once that connection
 * is established, the client will then use it's nested {@link TaskSender} class to send generated tasks to the Conductor.
 * Once all tasks are sent, the TaskSender shuts itself down. Once the respective {@link TaskReceiver} receives all the
 * tasks back from the Conductor, it calls the clients terminate method to close all related resources.
 * <p>
 * To use this class, start it from the command line by passing in a host name, port number, and the amount of tasks you
 * would like to get done.
 */
public class Client {

    private static final Random RANDOM = new Random();

    private final ComponentID myComponentID = new ComponentID(CLIENT, RANDOM.nextInt());
    private final Task ENDER_TASK = new Task(myComponentID.refID(), -1, A);
    private final Socket mySocket;
    private final TaskTracker taskTracker;
    private final TaskSender sender = new TaskSender();
    private final TaskReceiver receiver = new TaskReceiver();
    private ObjectOutputStream objOut;

    /**
     * @param clientSocket the socket that the client will use to send and receive messages from the Conductor
     * @param taskAmt how many tasks the Client should generate and send / receive to and from the Conductor
     */
    public Client(Socket clientSocket, int taskAmt) {
        mySocket = clientSocket;
        taskTracker = new TaskTracker(initializeTasks(taskAmt));
        initializeStreams();
    }

    /**
     * Notifies the {@link Conductor} of the oncoming connection. Additionally, starts {@link TaskSender} and {@link TaskReceiver}
     * threads to both send and receive tasks from the Conductor.
     */
    public void begin() {
        notifyConductor();
        sender.start();
        receiver.start();
    }

    /**
     * This method is internally used by the client. It uses the client socket used by this client instance to instantiate
     * output and input streams to use to communicate to the Conductor.
     */
    private void initializeStreams() {
        try {
            objOut = new ObjectOutputStream(mySocket.getOutputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This method is internally used to close all resources related to the client by closing the client socket used by this
     * client instance.
     */
    private void terminate() {
        System.out.printf("CLIENT %d: Received all tasks, ending communication with conductor...\n", myComponentID.refID());

        try {
            objOut.writeObject(ENDER_TASK);
            mySocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param taskAmount amount of tasks to generate and initialize
     * @return an array of Tasks that contains as many tasks as specified via 'taskAmount'
     */
    private Task[] initializeTasks(int taskAmount)
    {
        Task[] tasks = new Task[taskAmount];
        for (int i = 0; i < taskAmount; i++) {

            if (RANDOM.nextDouble() > 0.50)
                tasks[i] = new Task(myComponentID.refID(), i, A);

            else tasks[i] = new Task(myComponentID.refID(), i, B);

        }

        return tasks;
    }

    /**
     * Sends the clients {@link ComponentID} to the Conductor to let the Conductor prepare for its oncoming connection.
     */
    private void notifyConductor() {
        try {
            objOut.writeObject(myComponentID);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This class is used by the Client to send {@link Task}s to a {@link sim.conductor.Conductor} program. It extends {@link Thread}
     * and will run concurrently along with this class's counterpart, {@link TaskReceiver}.
     * <p>
     * Once all tasks are sent, the thread shuts itself down.
     */
    private class TaskSender extends Thread implements Sender {
        @Override public void run() {
            send();
        }

        @Override
        public void send() {
            try {
                Task t;
                while ((t = taskTracker.take()) != null) {
                    objOut.writeObject(t);
                    System.out.printf("CLIENT %d: Sent %s\n", myComponentID.refID(), t);
                }
                System.out.printf("CLIENT %d: All tasks sent, sender thread terminating...\n", myComponentID.refID());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This class is used by the Client to receive {@link Task}s from a {@link sim.conductor.Conductor} program. It extends {@link Thread}
     * and will run concurrently along with this class's counterpart, {@link TaskSender}.
     * <p>
     * Once the {@link Conductor} sends all tasks back to the client, this thread will shut itself, along with the connection
     * between this client instance and the Conductor down.
     */
    private class TaskReceiver extends Thread implements Receiver {
        @Override public void run() {
            receive();
        }

        @Override
        public void receive() {
            try{
                ObjectInputStream objIn = new ObjectInputStream(mySocket.getInputStream());
                Task incomingTask;
                while (!taskTracker.isSatisfied()) {
                    taskTracker.give((incomingTask = (Task) objIn.readObject()).taskID()); // Blocking call
                    System.out.printf("CLIENT %d: Received %s\n", myComponentID.refID(), incomingTask);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                terminate();
            }
        }
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.out.println("Usage: java Client <host name> <port number> <task amount>");
            System.exit(1);
        }

        Client c = new Client(new Socket(args[0], Integer.parseInt(args[1])), Integer.parseInt(args[2]));
        c.begin();
    }
}

