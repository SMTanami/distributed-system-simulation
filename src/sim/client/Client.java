package sim.client;

import sim.comms.Receiver;
import sim.comms.Sender;
import sim.component.ComponentID;
import sim.conductor.Conductor;
import sim.task.Task;
import sim.task.TaskA;
import sim.task.TaskB;

import java.io.DataInputStream;
import java.io.IOException;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import static sim.component.COMPONENT_TYPE.CLIENT;

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

    private final Socket mySocket;
    private final TaskTracker taskTracker;
    private final TaskSender sender = new TaskSender();
    private final TaskReceiver receiver = new TaskReceiver();
    private final int ID = RANDOM.nextInt();
    private final ComponentID COMPONENT_ID = new ComponentID(CLIENT, ID);
    private final TaskA enderTask = new TaskA(ID, -1);
    private ObjectOutputStream objOut;
    private DataInputStream dataIn;

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
     * @throws InterruptedException - if the threads were already staretd
     */
    public void begin() throws InterruptedException {
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
            dataIn = new DataInputStream(mySocket.getInputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This method is internally used to close all resources related to the client by closing the client socket used by this
     * client instance.
     */
    private void terminate() {
        System.out.printf("CLIENT %d: Received all tasks, ending communication with conductor...\n", ID);

        try {
            objOut.writeObject(enderTask);
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
                tasks[i] = new TaskA(ID, i);

            else tasks[i] = new TaskB(ID, i);

        }

        return tasks;
    }

    /**
     * Sends the clients {@link ComponentID} to the Conductor to let the Conductor prepare for its oncoming connection.
     */
    private void notifyConductor() {
        try {
            objOut.writeObject(COMPONENT_ID);
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
                    System.out.printf("CLIENT %d: SENT %s\n", ID, t);
                }
                System.out.printf("CLIENT %d: All tasks sent, sender thread terminating...\n", ID);
            }
            catch (IOException e)
            {
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
            try {
                while (!taskTracker.isSatisfied()) {
                    taskTracker.give(dataIn.readInt()); // Blocking call
                }

                terminate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        if (args.length != 3) {
            System.out.println("Usage: java Client <host name> <port number> <task amount>");
            System.exit(1);
        }

        Client c = new Client(new Socket(args[0], Integer.parseInt(args[1])), Integer.parseInt(args[2]));
        c.begin();
    }
}

