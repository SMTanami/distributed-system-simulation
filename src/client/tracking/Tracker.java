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
    private final Task[] tasks;
    private final Task[] completedTasks;
    private final Map<Integer, Task> tasksInProgress = Collections.synchronizedMap(new HashMap<>());

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
     * Removes the given task from tracking collection and moves it into a 'completion array'
     * @param taskID the ID of the task thast has been completed
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
