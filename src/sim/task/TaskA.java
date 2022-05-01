package sim.task;

public class TaskA extends Task {

    public TaskA(int clientID, int taskID) {
        super(clientID, taskID);
    }

    @Override
    public String toString() {
        return "TaskA {" + "Client = " + getClientID() + ", Task = " + getTaskID() + '}';
    }
}
