package Master;

import Task.Task;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This class handles the master's connection with a given worker.
 */

public class WorkerHandler {
    private final String workerID;
    private final Socket socket;
    private boolean isOccupied;
    private boolean isAssigned;
    private Task assignedTask;

    public WorkerHandler(String workerID, Socket socket) {
        this.workerID = workerID;
        this.socket = socket;
        this.isOccupied = false;
        this.isAssigned = false;
    }

    public void start() {
        TaskAssigner.start();
        Feedback.start();
    }

    public String getWorkerID() {
        return workerID;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public synchronized void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public synchronized void setAssigned(boolean assigned) {
        isAssigned = assigned;
    }

    public void setTask(Task assignedTask) {
        this.assignedTask = assignedTask;
        setAssigned(true);
    }

    private class TaskAssigner extends Thread {

        // This class contains the Thread which will be used by the Master to assign Tasks to this Worker.

        @Override
        public void run() {

            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                while (isAssigned()) {
                    setOccupied(true);
                    setAssigned(false);
                    out.writeObject(assignedTask);
                }
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Feedback extends Thread {

        // This class contains the Thread which will be used by the Master to receive feedback of completed Tasks from this Worker.

        @Override
        public void run() {
            Task task;
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                while ((task = (Task) in.readObject()) != null) {
                    setOccupied(false);
                    Master.getCompletedTasks().add(task);
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
}
