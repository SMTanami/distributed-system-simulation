package sim.conductor.comms;

import sim.comms.Receiver;
import sim.comms.Sender;
import sim.component.ComponentID;
import sim.conductor.WorkerTracker;
import sim.observer.Observable;
import sim.observer.Observer;
import sim.task.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class handles the master's connection with a given worker.
 */

public class WorkerHandler implements Observable {

    private final ObjectInputStream objIn;
    private final ObjectOutputStream objOut;
    private final ComponentID workerComponentID;
    private final TaskSender taskSender = new TaskSender();
    private final TaskReceiver taskReceiver = new TaskReceiver();
    private final BlockingQueue<Task> tasksToSend = new ArrayBlockingQueue<>(100);
    private BlockingQueue<Task> completedTaskQueue;
    private final HashSet<WorkerTracker> observingTrackers = new HashSet<>();

    public WorkerHandler(ComponentID workerComponentID, ObjectInputStream objIn, ObjectOutputStream objOut) {
        this.workerComponentID = workerComponentID;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    private class TaskSender extends Thread implements Sender {

        // This class contains the Thread which will be used by the Master to assign Tasks to this Worker.

        @Override
        public void run() {
            send();
        }

        @Override
        public void send() {
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

    private class TaskReceiver extends Thread implements Receiver {

        // This class contains the Thread which will be used by the Master to receive feedback of completed Tasks from this Worker.

        @Override
        public void run() {
            receive();
        }

        @Override
        public void receive() {
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

    public void start() {
        taskSender.start();
        taskReceiver.start();
    }

    public void setCompletedTaskQueue(BlockingQueue<Task> conductorsCompletedTaskQueue) {
        this.completedTaskQueue = conductorsCompletedTaskQueue;
    }

    public ComponentID getComponentID() {
        return workerComponentID;
    }

    public void sendTask(Task taskToSend) {
        tasksToSend.add(taskToSend);
    }

    @Override
    public void register(Observer observer) {
        if (observer instanceof WorkerTracker tracker)
            observingTrackers.add(tracker);
    }

    @Override
    public void unregister(Observer observer) {
        if (observer instanceof WorkerTracker tracker)
            observingTrackers.remove(tracker);
    }

    @Override
    public void notifyObservers() {
        for (WorkerTracker tracker : observingTrackers) {
            tracker.update(this);
        }
    }

    @Override
    public String toString() {
        return
                workerComponentID.component_type()
                + " ("
                + workerComponentID.refID()
                + ")";
    }
}
