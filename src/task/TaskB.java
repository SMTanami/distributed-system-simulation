package task;

public class TaskB extends Task {

    public TaskB(int client, int taskNum) {
        super(client, taskNum);
    }

    @Override
    public String toString() {
        return "TaskB {" + "ClientID = " + clientID + ", " +
                "Task = " + taskNum + '}';
    }
}
