package sim.master.cmcomms;

import sim.master.Master;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class will listen for clients that are looking to connect given server socket. Upon establishing a connection,
 * a {@link ClientHandler} will be created and placed within the master's Map of clients.
 */
public class ClientListener extends Thread {

    private ServerSocket host;

    /**
     * Listens for client sockets as they try to connect. Will immediately create and update the master with a new
     * {@link ClientHandler} to handle connection between the master and the newly connected client.
     */
    @Override
    public void run() {

            try (Socket incomingClient = host.accept();
                 DataInputStream dataIn = new DataInputStream(incomingClient.getInputStream()))
            {
                int clientID = dataIn.readInt(); // Request clientID upon first connecting with it
                ClientHandler handler = new ClientHandler(clientID, incomingClient);
                handler.start();
                Master.getClients().put(clientID, handler);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Sets the server socket that new clients will connect to
     * @param serverSocket the server socket through which clients will connect
     */
    public void setHost(ServerSocket serverSocket) {
        this.host = serverSocket;
    }
}
