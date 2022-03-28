import task.Task;
import task.TaskA;
import task.TaskB;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

public class Client
{
    private static final Random random = new Random();
    private static final int clientID = random.nextInt();
    private static final Task[] tasks = initializeTasks();

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java Client <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try(Socket clientSocket = new Socket(hostName, portNumber);
            ObjectOutputStream objOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream()))
        {

        }

    }

    private static Task[] initializeTasks()
    {
        Task[] tasks = new Task[10];
        for (int i = 0; i < 10; i++) {

            if (random.nextDouble() > 0.50)
                tasks[i] = new TaskA(clientID, i);

            else tasks[i] = new TaskB(clientID, i);

        }

        return tasks;
    }
}

