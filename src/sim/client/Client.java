package sim.client;

import sim.client.comms.TaskReceiver;
import sim.client.comms.TaskSender;
import sim.client.tracking.Tracker;
import sim.component.Component;
import sim.component.ComponentID;
import sim.task.Task;
import sim.task.TaskA;
import sim.task.TaskB;

import java.io.IOException;

import java.net.Socket;
import java.util.Random;

/**
 * This class acts as a Client in a distributed system. It initializes tasks and utilizes multithreading to send and
 * receive them to a corresponding master class.
 * <p>
 * To use this class, start it from the command line by passing in a host name, port number, and the amount of tasks you
 * would like to get done.
 */
public class Client implements Component {

    private static final Random RANDOM = new Random();
    private static final int ID = RANDOM.nextInt();
    private static final ComponentID COMPONENT_ID = new ComponentID(this, ID);

    /**
     * @param args 1. hostName (IP address of Server) 2. Port Number of the {@link sim.conductor.Conductor} program 3. amount of tasks desired to
     *             be created and executed
     * @throws IOException if the program is interrupted
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.out.println("Usage: java Client <host name> <port number> <sim.task amount>");
            System.exit(1);
        }

        // Get necessary information to connect to sim.master.Master
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Initialize clientID and n tasks, make sure it's not 0
        int taskAmount = Integer.parseInt(args[2]);
        Task[] tasks = initializeTasks(taskAmount, ID);

        // Initialize Socket, Sender/Receiver threads, and Tracker
        Socket clientSocket = new Socket(hostName, portNumber);
        Tracker tracker = new Tracker(tasks);
        TaskSender taskSender = new TaskSender(tracker, clientSocket);
        TaskReceiver taskReceiver = new TaskReceiver(tracker, clientSocket);

        taskSender.start();
        taskReceiver.start();

        try {
            taskSender.join();
            taskReceiver.join();
        }

        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param taskAmount amount of tasks to generate and initialize
     * @param clientID the clientID that will be used to identify sim.task's parent-sim.client in other programs
     * @return an array of Tasks that contains as many tasks as specified via 'taskAmount'
     */
    private static Task[] initializeTasks(int taskAmount, int clientID)
    {
        Task[] tasks = new Task[taskAmount];
        for (int i = 0; i < taskAmount; i++) {

            if (RANDOM.nextDouble() > 0.50)
                tasks[i] = new TaskA(clientID, i);

            else tasks[i] = new TaskB(clientID, i);

        }

        return tasks;
    }
}

