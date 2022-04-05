import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;

/*
 *  This class contains the Thread which will be used by the Master to assign Tasks to the Slaves.
 */

public class AssignmentThread extends Thread {
    private final Socket aSocket, bSocket;
    private final Queue<Task> collectedTasks;
    private final isSlaveOccupied isAOccupied, isBOccupied;

    public AssignmentThread(Socket aSocket, Socket bSocket, Queue<Task> collectedTasks, isSlaveOccupied isAOccupied, isSlaveOccupied isBOccupied) {
        this.aSocket = aSocket;
        this.bSocket = bSocket;
        this.collectedTasks = collectedTasks;
        this.isAOccupied = isAOccupied;
        this.isBOccupied = isBOccupied;
    }

    @Override
    public void run() {
        Task task;
        try (ObjectOutputStream aOut = new ObjectOutputStream(aSocket.getOutputStream());
             ObjectOutputStream bOut = new ObjectOutputStream(bSocket.getOutputStream())) {

            while ((task = collectedTasks.remove()) != null) {
                if (assignmentAlgorithm(task) == 'a') {
                    isAOccupied.setOccupied(true);
                    aOut.writeObject(task);
                }
                else {
                    isBOccupied.setOccupied(true);
                    bOut.writeObject(task);
                }
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public char assignmentAlgorithm(Task task) {
        if (task.getClass() == TaskA.class) {
            if (!isAOccupied.isOccupied()) {
                return 'a';
            }

            else if (isBOccupied.isOccupied() || collectedTasks.size() < 5 || !areNextFiveSame(collectedTasks, task)) {
                while (isAOccupied.isOccupied());
                return 'a';
            }

            else {
                return 'b';
            }
        }

        else {
            if (!isBOccupied.isOccupied()) {
                return 'b';
            }

            else if (isAOccupied.isOccupied() || collectedTasks.size() < 5 || !areNextFiveSame(collectedTasks, task)) {
                while (isBOccupied.isOccupied());
                return 'b';
            }

            else {
                return 'a';
            }
        }
    }

    public static boolean areNextFiveSame(Queue<Task> taskQueue, Task task) {
        Task[] taskArray = taskQueue.toArray(new Task[0]);
        for (int i = 0; i < 5; i++) {
            if (taskArray[i].getClass() != task.getClass()) {
                return false;
            }
        }
        return true;
    }
}