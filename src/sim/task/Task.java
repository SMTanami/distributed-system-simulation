package sim.task;

import java.io.Serializable;

public abstract class Task implements Serializable {

    private final int taskID;
    private final int clientID;

    public Task(int clientID, int taskID) {
        this.clientID = clientID;
        this.taskID = taskID;
    }

    public int getClientID() {
        return clientID;
    }

    public int getTaskID() {
        return taskID;
    }

    @Override
    public abstract String toString();
}
