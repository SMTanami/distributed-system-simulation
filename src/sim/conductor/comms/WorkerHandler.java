package sim.conductor.comms;

import sim.comms.Receiver;
import sim.comms.Sender;
import sim.component.ComponentID;
import sim.conductor.Conductor;
import sim.conductor.WorkerTracker;
import sim.observer.Observable;
import sim.observer.Observer;
import sim.task.TASK_TYPE;
import sim.task.Task;
import sim.worker.Worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class handles the {@link Conductor}s connection with a given worker. Via separate thread classes in:
 * {@link WorkerHandler.TaskReceiver} and {@link WorkerHandler.TaskSender},this class will send tasks upon command to
 * the appropriate worker. Once the task has been completed, the WorkerHandler will store the task in the Conductor's
 * completed tasks queue, as this class will have a reference to it via DIP.
 */
public class WorkerHandler implements Observable {

    private final Socket mySocket;
    private final TASK_TYPE workerType;
    private final ComponentID myComponentID;
    private final ObjectInputStream objIn;
    private final TaskSender taskSender = new TaskSender();
    private final TaskReceiver taskReceiver = new TaskReceiver();
    private final BlockingQueue<Task> tasksToSend = new ArrayBlockingQueue<>(100);
    private final HashSet<WorkerTracker> observingTrackers = new HashSet<>();
    private BlockingQueue<Task> completedTaskQueue;
    private ObjectOutputStream objOut;

    /**
     * @param connectingComponentID the connecting component's ComponentID
     * @param workerSocket the connecting {@link Worker} socket used to communicate with the respective worker
     * @param objIn the object input stream used to receive tasks from workers
     */
    public WorkerHandler(ComponentID connectingComponentID, Socket workerSocket, TASK_TYPE workerType, ObjectInputStream objIn) {
        this.myComponentID = connectingComponentID;
        this.mySocket = workerSocket;
        this.workerType = workerType;
        this.objIn = objIn;
        initializeStreams();
    }

    /**
     * Initializes an ObjectOutputStream for this ClientHandler instance to use to communicate with its respective client.
     */
    private void initializeStreams() {
        try {
            objOut = new ObjectOutputStream(mySocket.getOutputStream());
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts the {@link WorkerHandler.TaskReceiver} and {@link WorkerHandler.TaskSender} threads to begin collecting incoming tasks from the client and sending completed
     * tasks back to it.
     */
    public void start() {
        taskSender.start();
        taskReceiver.start();
    }

    /**
     * Sets the queue that this WorkerHandler instance should store completed tasks in. This is going to be a reference to
     * the completed tasks queue in the {@link sim.conductor.Conductor}.
     * @param conductorsCompletedTaskQueue a reference to the completed task queue in the conductor
     */
    public void setCompletedTaskQueue(BlockingQueue<Task> conductorsCompletedTaskQueue) {
        this.completedTaskQueue = conductorsCompletedTaskQueue;
    }

    /**
     * @return the {@link sim.worker.Worker} or {@link sim.worker.Worker} that is handled by this WorkerHandler's ComponentID
     */
    public ComponentID getComponentID() {
        return myComponentID;
    }

    /**
     * @return the type of worker associated with this WorkerHandler instance
     */
    public TASK_TYPE getWorkerType() {
        return workerType;
    }

    /**
     * While this method does not literally send a task to the connected worker, it does add it to a BlockingQueue that
     * this WorkerHandler's {@link TaskSender} instance will send to the worker. This is done in order to keep
     * things as asynchronous as possible.
     * @param taskToSend the task to send to the worker
     */
    public void sendTask(Task taskToSend) {
        tasksToSend.add(taskToSend);
    }

    /**
     * The TaskSender class is a private class that is leveraged by its parent WorkerHandler class. In order to retrieve
     * and send tasks to and from workers concurrently, the TaskSender assumes the responsibility of sending
     * tasks to the given worker via the parent WorkerHandler's client sockets output stream.
     */
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
                    System.out.println("CONDUCTOR: Sent task " + taskToSend + " to " + myComponentID);
                    objOut.writeObject(taskToSend);
                }
            }

            catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("WorkerHandler interrupted");
            }
        }
    }

    /**
     * The TaskReceiver class is a private class that is leveraged by its parent WorkerHandler class. In order to retrieve
     * and send tasks to and from workers concurrently, the TaskReceiver assumes the responsibility of receiving
     * tasks from the given worker via the parent WorkerHandler's client sockets input stream.
     */
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
                    System.out.println("CONDUCTOR: Received completed " + task + " from " + myComponentID);
                    completedTaskQueue.add(task);
                    notifyObservers();
                }
            }

            catch (IOException | ClassNotFoundException e) {
                if (e instanceof IOException)
                    System.out.printf("CONDUCTOR: Worker(%s) %d has disconnected...\n", workerType, myComponentID.refID());
            }
        }
    }

    /**
     * Registers the given observer to this WorkerHandler so that the observer can get notified when this WorkerHandler
     * is no longer occupied and is ready to perform another task.
     * @param observer the observer instance that would like to observe this WorkerHandler
     */
    @Override
    public void register(Observer observer) {
        if (observer instanceof WorkerTracker tracker)
            observingTrackers.add(tracker);
    }

    /**
     * Unregisters a registered observer from the registered collection. If the given observer was not registered to
     * begin with, nothing happens.
     * @param observer the observer that no longer wishes to be updated by this observable instance
     */
    @Override
    public void unregister(Observer observer) {
        if (observer instanceof WorkerTracker tracker)
            observingTrackers.remove(tracker);
    }

    /**
     * Lets all registered observers know of this WorkerHandlers availability to perform another task.
     */
    @Override
    public void notifyObservers() {
        for (WorkerTracker tracker : observingTrackers) {
            tracker.update(this);
        }
    }

    @Override
    public String toString() {
        return
                myComponentID.component_type()
                + " ("
                + myComponentID.refID()
                + ")";
    }
}
