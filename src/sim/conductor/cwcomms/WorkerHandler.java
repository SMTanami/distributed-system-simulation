package sim.conductor.cwcomms;

import sim.component.ComponentID;
import sim.conductor.WorkerTracker;
import sim.obersver.Observable;
import sim.task.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class handles the master's connection with a given worker.
 */

public class WorkerHandler implements Observable {

    private boolean isOccupied = false;
    private final ObjectInputStream objIn;
    private final ObjectOutputStream objOut;
    private final ComponentID workerComponentID;
    private final TaskSender taskSender = new TaskSender();
    private final TaskReceiver taskReceiver = new TaskReceiver();
    private final BlockingQueue<Task> tasksToSend = new ArrayBlockingQueue<>(100);
    private BlockingQueue<Task> completedTaskQueue;
    private final ArrayList<WorkerTracker> observingTrackers = new ArrayList<>();

    public WorkerHandler(ComponentID workerComponentID, ObjectInputStream objIn, ObjectOutputStream objOut) {
        this.workerComponentID = workerComponentID;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    public void start() {
        taskSender.start();
        taskReceiver.start();
    }

    public void setCompletedTaskQueue(BlockingQueue<Task> conductorsCompletedTaskQueue) {
        this.completedTaskQueue = conductorsCompletedTaskQueue;
    }

    public boolean isOccupied() { return isOccupied; }

    public ComponentID getComponentID() {
        return workerComponentID;
    }

    public void sendTask(Task taskToSend) {
        tasksToSend.add(taskToSend);
        isOccupied = true;
    }

    @Override
    public void register(WorkerTracker tracker) {
        observingTrackers.add(tracker);
    }

    @Override
    public void unregister(WorkerTracker tracker) {
        observingTrackers.remove(tracker);
    }

    @Override
    public void notifyObservers() {
        for (WorkerTracker tracker : observingTrackers) {
            tracker.update(this);
        }
    }

    private class TaskSender extends Thread {

        // This class contains the Thread which will be used by the Master to assign Tasks to this Worker.

        @Override
        public void run() {

            try {
                Task taskToSend;
                while ((taskToSend = tasksToSend.take()) != null) {
                    System.out.println("CONDUCTOR: Sent task " + taskToSend + " to " + workerComponentID);
                    objOut.writeObject(taskToSend);
                }
            }

            catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("WorkerHandler interrupted");
            }
        }
    }

    private class TaskReceiver extends Thread {

        // This class contains the Thread which will be used by the Master to receive feedback of completed Tasks from this Worker.

        @Override
        public void run() {

            Task task;
            try{
                while ((task = (Task) objIn.readObject()) != null) {
                    System.out.println("CONDUCTOR: Received completed " + task + " from " + workerComponentID);
                    completedTaskQueue.add(task);
                    notifyObservers();
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
