package sim.task;

import java.io.Serializable;

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
