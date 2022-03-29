package task;
import java.io.Serializable;

public abstract class Task implements Serializable {

    private final int CLIENTID;
    private final int TASKNUM;
    private boolean completionStatus;

    public Task(int clientID, int taskNum) {
        this.CLIENTID = clientID;
        this.TASKNUM = taskNum;
        completionStatus = false;
    }

    public int getClientID() {
        return CLIENTID;
    }

    public int getTaskNum() {
        return TASKNUM;
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
