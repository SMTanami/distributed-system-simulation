package task;

public class TaskA extends Task {

    public TaskA(int client, int taskNum) {
        super(client, taskNum);
    }

    @Override
    public String toString() {
        return "TaskA {" + "ClientID = " + clientID + ", " +
                "Task = " + taskNum + '}';
    }
}
