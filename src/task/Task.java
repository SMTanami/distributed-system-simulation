package task;
import java.io.Serializable;

public abstract class Task implements Serializable {
    char client;
    int taskNum;
    boolean completionStatus;

    public Task(char client, int taskNum) {
        this.client = client;
        this.taskNum = taskNum;
        completionStatus = false;
    }

    public char getClient() {
        return client;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public boolean getCompletionStatus() {
        return completionStatus;
    }

    public void taskCompleted() {
        completionStatus = true;
    }
}
