package task;

public class TaskB extends Task {

    public TaskB(char client, int taskNum) {
        super(client, taskNum);
    }

    @Override
    public String toString() {
        return "TaskB {" + "Client = " + client + ", Task# = " + taskNum + '}';
    }
}
