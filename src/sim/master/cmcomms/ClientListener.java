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

    private final ServerSocket host;

    public ClientListener(ServerSocket host) {
        this.host = host;
    }

    /**
     * Listens for client sockets as they try to connect. Will immediately create and update the master with a new
     * {@link ClientHandler} to handle connection between the master and the newly connected client.
     */
    @Override
    public void run() {

        while (!host.isClosed())
        {
            try{
                // Receive incoming client socket
                Socket incomingClient = host.accept();

                // Get ClientID from client and close input stream used to do so
                DataInputStream dataIn = new DataInputStream(incomingClient.getInputStream());
                int clientID = dataIn.readInt();
                dataIn.close();

                // Create, store, and start a new ClientHandler
                ClientHandler handler = new ClientHandler(clientID, incomingClient, Master.getCollectedTasks());
                Master.getClients().put(clientID, handler);
                handler.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
