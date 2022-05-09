package sim.task;

import java.io.Serializable;

import sim.client.Client;
import sim.conductor.Conductor;
import sim.worker.Worker;

/**
 * Each Task is identified by three attributes: 1) the {@link Client} who requested the Task,
 * 2) the number assigned by the Client to this particular Task, and 3) the {@link TASK_TYPE}.
 * This record is important for the Client to track which task it sent and was completed.
 * It is also important for the {@link Conductor} to be able to assign a {@link Worker} to perform the Task
 * and to notify the proper Client of the completed Task.
 * @param clientID the Client requesting the Task
 * @param taskID the unique number the Client assigned to the Task
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
