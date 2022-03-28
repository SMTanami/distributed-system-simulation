package client.tracking;

import task.Task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is to be used by Sender and Receiver threads as a point of reference for all task related
 * operations.
 */
public class Tracker {

    private int takePoint = 0;
    private int insertionPoint = 0;
    protected final Task[] tasks;
    protected final Task[] completedTasks;
    protected final Map<Integer, Task> tasksInProgress = Collections.synchronizedMap(new HashMap<>());

    /**
     * @param tasks the tasks that are to be completed by the client.
     */
    public Tracker(Task[] tasks) {
        this.tasks = tasks;
        this.completedTasks = new Task[tasks.length];
    }

    /**
     * Takes the next task from the uncompleted list of tasks and returns it or {@code null}
     * if no other task is available.
     * @return The next Task that has yet to be completed or {@code null}
     */
    public synchronized Task take() {

        if (takePoint > tasks.length)
            return null;

        else{
            Task t = tasks[takePoint];
            tasksInProgress.put(t.getTaskNum(), t);
            tasks[takePoint] = null;
            takePoint++;
            return t;
        }
    }

    /**
     * @param taskID the taskID to identify the task being given to the tracker.
     * @return true if all tasks were completed, false otherwise
     */
    public synchronized boolean give(int taskID) {
        completedTasks[insertionPoint] = tasksInProgress.remove(taskID);
        insertionPoint++;

        return insertionPoint == tasks.length;
    }
}
