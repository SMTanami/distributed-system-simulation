package task;

public class TaskA extends Task {

    public TaskA(char client, int taskNum) {
        super(client, taskNum);
    }

    @Override
    public String toString() {
        return "TaskA {" + "Client = " + client + ", Task# = " + taskNum + '}';
    }
}
