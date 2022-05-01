package sim.task;

import java.io.Serializable;

public abstract class Task implements Serializable {

    private final int clientID;
    private final int taskID;
    private boolean completionStatus;

    public Task(int clientID, int taskID) {
        this.clientID = clientID;
        this.taskID = taskID;
        completionStatus = false;
    }

    public int getClientID() {
        return clientID;
    }

    public int getTaskID() {
        return taskID;
    }

    public boolean getCompletionStatus() {
        return completionStatus;
    }

    public void setTaskCompleted() {
        completionStatus = true;
    }

    @Override
    public abstract String toString();
}
