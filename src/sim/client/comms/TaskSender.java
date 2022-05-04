package sim.client.comms;

import sim.client.tracking.Tracker;
import sim.task.Task;
import sim.task.TaskA;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This class is used by the Client to send Tasks to a {@link sim.conductor.Conductor} program. It extends {@link Thread}
 * and will run concurrently along with this class's counterpart, {@link TaskReceiver}.
 */
public class TaskSender extends Thread {

    protected final Tracker tracker;
    protected final Socket clientSocket;

    /**
     * @param tracker the {@link Tracker} that both this sender and it's receiver counterpart will use
     * @param clientSocket the sim.client socket with which to use to interact with the master program
     */
    public TaskSender(Tracker tracker, Socket clientSocket) {
        this.tracker = tracker;
        this.clientSocket = clientSocket;
    }

    @Override public void run() {

        try(ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream()))
        {
            Task t;
            while ((t = tracker.take()) != null) {
                outStream.writeObject(t); // Unblock
            }

            //Write a Task to the Master with clientID and taskNum set to 0 to indicate client completion
            outStream.writeObject(new TaskA(0, 0));
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
