package slave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SlaveA {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java Client <hostName> <portNumber>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Thread thread = new Thread();
        Task task;

        try (Socket socket = new Socket(hostName, portNumber);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while ((task = (Task) in.readObject()) != null) {
                System.out.println("Received: " + task);
                thread.start();

                if (task.getClass() == TaskA.class) {
                    Thread.sleep(2000);
                    System.out.println("This task should take 2 seconds.");
                }

                else {
                    Thread.sleep(10000);
                    System.out.println("This task should take 10 seconds.");
                }

                thread.join();
                System.out.println("Completed: " + task);
                out.writeObject(task);
            }
        }

        catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }

        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }

        catch (InterruptedException e) {
            System.err.println("Thread was interrupted.");
            System.exit(1);
        }

        catch (ClassNotFoundException e) {
            System.err.println("Couldn't find the class.");
            System.exit(1);
        }
    }
}
