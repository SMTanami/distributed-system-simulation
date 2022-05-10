package sim.task;

import java.io.Serializable;

import sim.client.Client;
import sim.conductor.Conductor;
import sim.worker.Worker;

/**
 * Each Task is identified by three attributes: 1) the {@link Client} who requested the Task,
 * 2) the taskID assigned to this task by the Client, and 3) the {@link TASK_TYPE}.
 * <p>
 * This record is important for the Client to track which task it sent and was completed.
 * It is also essential for the {@link Conductor} to be able to assign a {@link Worker} to perform the Task
 * and to notify the proper Client of the completed Task.
 * @param clientID the clientID of the Client requesting the Task
 * @param taskID the (should be unique) taskID to assign the Task
 * @param type the type of Task
 */
public record Task(int clientID, int taskID, TASK_TYPE type) implements Serializable {

    @Override
    public String toString() {
        return "Task: [clientID = "
                + clientID
                + ", task ID = "
                + taskID
                + ", type = "
                + type
                + "]";
    }
}
