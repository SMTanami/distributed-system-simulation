import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Queue;

/*
 *  This class contains the Thread which will be used by the Master to receive feedback of completed Tasks by the Slaves.
 */

public class FeedbackThread extends Thread {
    private final Socket socket;
    private final Queue<Task> completedTasks;
    private final isSlaveOccupied isOccupied;

    public FeedbackThread(Socket socket, Queue<Task> completedTasks, isSlaveOccupied isOccupied) {
        this.socket = socket;
        this.completedTasks = completedTasks;
        this.isOccupied = isOccupied;
    }

    @Override
    public void run() {
        Task task;
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while ((task = (Task) in.readObject()) != null) {
                isOccupied.setOccupied(false);
                completedTasks.add(task);
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        catch (ClassNotFoundException e) {
            System.err.println("Couldn't find the object's class.");
            System.exit(1);
        }
    }
}
