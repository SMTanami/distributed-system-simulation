package sim.master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class will listen for clients that are looking to connect to the connected master and will add them to a shared
 * set whenever they connect.
 */
public class ClientHandler extends Thread {

    private ServerSocket host;

    /**
     * Listens for client sockets as they try to connect. Will immediately update the master with the new client socket
     * once received.
     */
    @Override
    public void run() {

            try (Socket newClient = host.accept();
                 DataInputStream dataIn = new DataInputStream(newClient.getInputStream()))
            {
                int clientID = dataIn.readInt(); // Request clientID upon first connecting with it
                TaskConfirmer confirmer = new TaskConfirmer(clientID, new DataOutputStream(newClient.getOutputStream()));
                TaskCollector collector = new TaskCollector(clientID, confirmer, new ObjectInputStream(newClient.getInputStream()));
                Master.storeAndStartCollectorConfirmer(collector, confirmer);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Use this method to set the server socket that new clients will connect to
     * @param serverSocket the server socket through which clients will connect
     */
    public void setHost(ServerSocket serverSocket) {
        this.host = serverSocket;
    }
}
