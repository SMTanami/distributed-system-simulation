package sim.conductor.cwcomms;

import sim.component.ComponentID;
import sim.task.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class handles the master's connection with a given worker.
 */

public class WorkerHandler {

    private final ComponentID workerComponentID;
    private final Socket socket;
    private boolean isOccupied;
    private boolean isAssigned;
    private final TaskAssigner taskAssigner;
    private final Feedback feedback;

    private final BlockingQueue<Task> tasksToSend = new ArrayBlockingQueue<>(100);
    private BlockingQueue<Task> completedTaskQueue;

    public WorkerHandler(ComponentID workerComponentID, Socket socket) {

        this.workerComponentID = workerComponentID;
        this.socket = socket;
        isOccupied = false;
        isAssigned = false;
        taskAssigner = new TaskAssigner();
        feedback = new Feedback();
    }

    public void start() {
        taskAssigner.start();
        feedback.start();
    }

    public void setCompletedTaskQueue(BlockingQueue<Task> conductorsCompletedTaskQueue) {
        this.completedTaskQueue = conductorsCompletedTaskQueue;
    }

    public ComponentID getComponentID() {
        return workerComponentID;
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
        tasksToSend.add(assignedTask);
        setAssigned(true);
    }

    private class TaskAssigner extends Thread {

        // This class contains the Thread which will be used by the Master to assign Tasks to this Worker.

        @Override
        public void run() {

            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                Task taskToSend;
                while ((taskToSend = tasksToSend.take()) != null) {
                    setOccupied(true);
                    setAssigned(false);
                    out.writeObject(taskToSend);
                }
            }

            catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("WorkerHandler interrupted");
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
                    completedTaskQueue.add(task);
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
