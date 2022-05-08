package sim.task;

import java.io.Serializable;

public class TaskB extends Task implements Serializable {

    public TaskB(int clientID, int taskID) {
        super(clientID, taskID);
    }

    @Override
    public String toString() {
        return "TaskB {" + "Client = " + getClientID() + ", Task = " + getTaskID() + '}';
    }
}
