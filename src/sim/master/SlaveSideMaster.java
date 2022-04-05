import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/*
 *  This class contains the Slave side of the Master. It connects to two Slaves using three threads.
 *  It accesses the collectedTasks and completedTasks Queues,
 *  along with the isWorkerOccupied classes for SlaveA and SlaveB.
 */

public class Master {
    static isSlaveOccupied isAOccupied = new isSlaveOccupied(false), isBOccupied = new isSlaveOccupied(false);
    static Queue<Task> collectedTasks = new ArrayBlockingQueue<>(100);
    static Queue<Task> completedTasks = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Server <portNumber>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket ss = new  ServerSocket(portNumber);
             Socket aSocket = ss.accept();
             Socket bSocket = ss.accept())
        {
            Thread mts = new AssignmentThread(aSocket, bSocket, collectedTasks, isAOccupied, isBOccupied);
            Thread atm = new FeedbackThread(aSocket, completedTasks, isAOccupied);
            Thread btm = new FeedbackThread(bSocket, completedTasks, isBOccupied);

            mts.start();
            atm.start();
            btm.start();

            mts.join();
            atm.join();
            btm.join();
        }

        catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection.");
            System.out.println(e.getMessage());
        }

        catch (InterruptedException e) {
            System.out.println("Thread was interrupted before it could complete.");
            System.out.println(e.getMessage());
        }
    }
}