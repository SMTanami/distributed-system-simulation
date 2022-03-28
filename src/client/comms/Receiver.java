package client.comms;

import client.tracking.Tracker;

import java.net.Socket;

public class Receiver extends Thread {

    protected final Tracker tracker;
    protected final Socket clientSocket;

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
