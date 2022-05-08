package sim.task;

import java.io.Serializable;

public class TaskA extends Task implements Serializable {

    public TaskA(int clientID, int taskID) {
        super(clientID, taskID);
    }

    @Override
    public String toString() {
        return "TaskA {" + "Client = " + getClientID() + ", Task = " + getTaskID() + '}';
    }
}
