package sim.slave;

import sim.task.Task;
import sim.task.TaskA;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class SlaveA {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java Client <hostName> <portNumber>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Random r = new Random();
        final int ID = r.nextInt();
        Task task;

        try (Socket socket = new Socket(hostName, portNumber);
             PrintWriter sendID = new PrintWriter(socket.getOutputStream(), true);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            sendID.println("a " + ID);
            
            while ((task = (Task) in.readObject()) != null) {
                System.out.println("Received: " + task);
                
                if (task.getClass() == TaskA.class) {
                    Thread.sleep(2000);
                    System.out.println("This sim.task should take 2 seconds.");
                }

                else {
                    Thread.sleep(10000);
                    System.out.println("This sim.task should take 10 seconds.");
                }

                System.out.println("Completed: " + task);
                out.writeObject(task);
            }
        }

        catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }

        catch (InterruptedException e) {
            System.err.println("Thread was interrupted while asleep.");
            System.exit(1);
        }

        catch (ClassNotFoundException e) {
            System.err.println("Couldn't find the object's class.");
            System.exit(1);
        }
    }
}
