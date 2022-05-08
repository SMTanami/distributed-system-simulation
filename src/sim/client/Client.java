package sim.client;

import sim.client.tracking.Tracker;
import sim.component.Component;
import sim.component.ComponentID;
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
 * To use this class, start it from the command line by passing in a host name, port number, and the amount of tasks you
 * would like to get done.
 */
public class Client implements Component {

    private ObjectOutputStream objOut;
    private DataInputStream dataIn;
    private final Tracker taskTracker;
    private final TaskSender sender = new TaskSender();
    private final TaskReceiver receiver = new TaskReceiver();
    private final Random RANDOM = new Random();
    private final int ID = RANDOM.nextInt();
    private final ComponentID COMPONENT_ID = new ComponentID(CLIENT, ID);
    private final TaskA enderTask = new TaskA(ID, -1);

    public Client(Socket clientSocket, int taskAmt) {
        try {
            objOut = new ObjectOutputStream(clientSocket.getOutputStream());
            dataIn = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
        taskTracker = new Tracker(initializeTasks(taskAmt));
    }

    public void begin() throws InterruptedException {
        notifyConductor();
        sender.start();
        receiver.start();

        sender.join();
        receiver.join();
    }

    private void terminate() {
        System.out.println("CLIENT: Received all tasks, ending communciation with conductor...");

        try {
            objOut.writeObject(enderTask);
            objOut.close();
            dataIn.close();
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

    @Override
    public void notifyConductor() {
        try {
            objOut.writeObject(COMPONENT_ID);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This class is used by the Client to send Tasks to a {@link sim.conductor.Conductor} program. It extends {@link Thread}
     * and will run concurrently along with this class's counterpart, {@link TaskReceiver}.
     */
    private class TaskSender extends Thread {
        @Override public void run() {

            try {
                Task t;
                while ((t = taskTracker.take()) != null) {
                    objOut.writeObject(t);
                    System.out.println("CLIENT: SENT TASK " + t);
                }
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
     */
    private class TaskReceiver extends Thread {
        @Override public void run() {

            try {
                while (!taskTracker.isSatisfied()) {
                    taskTracker.give(dataIn.readInt()); // Blocking call
                }

                terminate();
            }

            catch (IOException e)
            {
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

