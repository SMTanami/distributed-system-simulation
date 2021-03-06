package sim.client;

import sim.task.Task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is to be used by Sender and Receiver threads as a point of reference for all {@link Task} related
 * operations. The Client will use this class in order to track what tasks have been sent, received, and it all tasks
 * that have been sent have been successfully returned by the {@link sim.conductor.Conductor}.
 */
public class TaskTracker {

    private int takePoint = 0;
    private int insertionPoint = 0;
    private final Task[] tasks;
    private final Task[] completedTasks;
    private final Map<Integer, Task> tasksInProgress = Collections.synchronizedMap(new HashMap<>());

    /**
     * @param tasks the tasks that are to be completed by the sim.client.
     */
    public TaskTracker(Task[] tasks) {
        this.tasks = tasks;
        this.completedTasks = new Task[tasks.length];
    }

    /**
     * Takes the next sim.task from the uncompleted list of tasks and returns it or {@code null}
     * if no other sim.task is available.
     * @return The next Task that has yet to be completed or {@code null}
     */
    public Task take() {

        if (takePoint >= tasks.length)
            return null;

        else{
            synchronized (this) {
                Task t = tasks[takePoint];
                tasksInProgress.put(t.taskID(), t);
                tasks[takePoint] = null;
                takePoint++;
                return t;
            }
        }
    }

    /**
     * Removes the given sim.task from tracking collection and moves it into a 'completion array'
     * @param taskID the ID of the sim.task thast has been completed
     */
    public synchronized void give(int taskID) {
        completedTasks[insertionPoint] = tasksInProgress.remove(taskID);
        insertionPoint++;
    }

    /**
     * Use this method to test for more tasks to complete
     * @return true if the tracker sees all tasks complete, false otherwise
     */
    public synchronized boolean isSatisfied() {
        return insertionPoint == tasks.length;
    }
}
