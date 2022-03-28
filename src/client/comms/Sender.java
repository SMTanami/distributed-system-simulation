package client.comms;

import client.tracking.Tracker;

import java.net.Socket;

/**
 * This class is used by the Client to send Tasks to a Master program. It extends {@link Thread}
 * and will run concurrently along with this class's counterpart, {@link Receiver}.
 */
public class Sender extends Thread {

    protected final Tracker tracker;
    protected final Socket clientSocket;

    /**
     * @param tracker the {@link Tracker} that both this sender and it's receiver counterpart will use
     * @param clientSocket the client socket with which to use to interact with the master program
     */
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
