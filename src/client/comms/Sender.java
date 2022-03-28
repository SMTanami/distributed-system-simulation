package client.comms;

import client.tracking.Tracker;

import java.net.Socket;

public class Sender extends Thread {

    protected final Tracker tracker;
    protected final Socket clientSocket;

    public Sender(Tracker tracker, Socket clientSocket) {
        this.tracker = tracker;
        this.clientSocket = clientSocket;
    }

    @Override public void run() {
        /*
        Task t;
        while((t = tracker.take) != null) {
            client socket output stream yadda yadda
        }
         */
    }


}
