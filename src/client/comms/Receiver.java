package client.comms;

import client.tracking.Tracker;
import task.Task;

import java.net.Socket;

/**
 * This class is used by the Client to receive {@link Task}s from a Master program. It extends {@link Thread}
 * and will run concurrently along with this class's counterpart, {@link Sender}.
 */
public class Receiver extends Thread {

    protected final Tracker tracker;
    protected final Socket clientSocket;

    /**
     * @param tracker the {@link Tracker} that both this sender and it's receiver counterpart will use
     * @param clientSocket the client socket with which to use to interact with the master program
     */
    public Receiver(Tracker tracker, Socket clientSocket) {
        this.tracker = tracker;
        this.clientSocket = clientSocket;
    }

    @Override public void run() {
        /*
        Somehow receive a task?
        tracker.give(taskID); //let the receiver know a task has completed
         */
    }
}
