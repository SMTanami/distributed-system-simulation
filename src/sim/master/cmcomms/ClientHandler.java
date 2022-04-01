package sim.master;

import sim.task.Task;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class ClientHandler {

    private final int myClientID;
    private boolean isTerminated = false;
    private final TaskCollector myCollector;
    private final TaskConfirmer myConfirmer;
    private final Queue<Task> collectedTasks;
    private final BlockingQueue<Task> completedTasks;

    public ClientHandler(TaskCollector collector, TaskConfirmer confirmer) {
        myClientID = collector.getClientID();
        myCollector = collector;
        myConfirmer = confirmer;

        collectedTasks = collector.getCollectedTasks();
        completedTasks = confirmer.getCompletedTasks();
    }

    public void start() {
        myCollector.start();
        myConfirmer.start();
    }

    public void terminate() throws IOException {
        myCollector.getClientSocket().close();
        myConfirmer.getClientSocket().close();
        isTerminated = true;
    }

    public int getClientID() {
        return myClientID;
    }

    public boolean isTerminated() {
        return isTerminated;
    }
}
