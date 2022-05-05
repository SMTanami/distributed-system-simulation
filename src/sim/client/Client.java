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

import java.io.ObjectOutputStream;
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

    private final Socket myClientSocket;
    private final Tracker taskTracker;
    private final TaskSender sender;
    private final TaskReceiver receiver;
    private final Random RANDOM = new Random();
    private final int ID = RANDOM.nextInt();
    private final ComponentID COMPONENT_ID = new ComponentID(this, ID);

    public Client(Socket clientSocket, int taskAmt) {

        myClientSocket = clientSocket;
        taskTracker = new Tracker(initializeTasks(taskAmt));
        sender = new TaskSender(taskTracker, clientSocket);
        receiver = new TaskReceiver(taskTracker, clientSocket);
    }

    public void begin() {

        notifyConductor();
        sender.start();
        receiver.start();
    }

    /**
     * @param taskAmount amount of tasks to generate and initialize
     * @param clientID the clientID that will be used to identify sim.task's parent-sim.client in other programs
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

    @Override
    public void notifyConductor() {

        try (ObjectOutputStream objOut = new ObjectOutputStream(myClientSocket.getOutputStream())) {
            objOut.writeObject(COMPONENT_ID);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not send component ID to conductor");
        }

    }

    public static void main(String[] args) throws IOException {

        Client c = new Client(new Socket(args[0], Integer.parseInt(args[1])), Integer.parseInt(args[2]));
        c.begin();

    }
}

