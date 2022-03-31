package sim.master;

import sim.task.Task;

import java.io.DataOutputStream;
import java.util.ArrayDeque;
import java.util.Queue;

public class TaskConfirmer extends Thread {

    private final int clientID;
    private final DataOutputStream myDataOutputStream;
    private final Queue<Integer> tasksToSend;

    public TaskConfirmer(int clientID, DataOutputStream dataOutputStream) {
        this.clientID = clientID;
        this.myDataOutputStream = dataOutputStream;
        tasksToSend = new ArrayDeque<>();
    }

    public void add(int taskID) {
        tasksToSend.add(taskID);
    }

    public int getClientID() {
        return clientID;
    }

    @Override
    public void run() {
        //TODO while there is stuff to return, return them to the proper clients
    }

}
