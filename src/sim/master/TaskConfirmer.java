package sim.master;

import java.io.DataOutputStream;
import java.io.IOException;

public class TaskConfirmer extends Thread {

    private final DataOutputStream myDataOutputStream;
    private boolean terminate = false;
    private final int clientID;

    public TaskConfirmer(int clientID, DataOutputStream dataOutputStream) {
        this.myDataOutputStream = dataOutputStream;
        this.clientID = clientID;
    }

    @Override
    public void run() {

        while (!terminate)
        {
            //TODO write ints to client from collection of completed tasks
        }

        // Close OutputStream
        try {
            myDataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getClientID() {
        return clientID;
    }

    public void terminate() {
        terminate = true;
    }
}
