package task;
import java.io.Serializable;

public abstract class Task implements Serializable {

    protected final int clientID;
    protected int taskNum;
    private boolean completionStatus;

    public Task(int clientID, int taskNum) {
        this.clientID = clientID;
        this.taskNum = taskNum;
        completionStatus = false;
    }

    public int getClient() {
        return clientID;
    }

    public int getTaskNum() {
        return taskNum;
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
