package client;

import client.comms.Receiver;
import client.comms.Sender;
import client.tracking.Tracker;
import task.Task;
import task.TaskA;
import task.TaskB;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class Client
{
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.out.println("Usage: java Client <host name> <port number> <task amount>");
            System.exit(1);
        }

        // Get necessary information to connect to Master
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Initialize clientID and n tasks
        int clientID = RANDOM.nextInt();
        int taskAmount = Integer.parseInt(args[2]);
        Task[] tasks = initializeTasks(taskAmount, clientID);

        // Initialize Socket, Sender/Receiver threads, and Tracker
        Socket clientSocket = new Socket(hostName, portNumber);
        Tracker tracker = new Tracker(tasks);
        Sender sender = new Sender(tracker, clientSocket);
        Receiver receiver = new Receiver(tracker, clientSocket);

        sender.run();
        receiver.run();

        try
        {
            sender.join();
            receiver.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }


    }

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

