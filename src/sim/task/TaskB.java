package sim.task;

public class TaskB extends Task {

    public TaskB(int clientID, int taskID) {
        super(clientID, taskID);
    }

    @Override
    public String toString() {
        return "TaskB {" + "Client = " + getClientID() + ", Task = " + getTaskID() + '}';
    }
}
