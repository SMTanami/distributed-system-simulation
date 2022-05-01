package sim.main.cmcomms;

import sim.main.Main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class will listen for clients and workers that are looking to connect to the given server socket. Upon establishing a connection,
 * a {@link ClientHandler} or a {@link WorkerHandler} will be created and placed within the master's Map of clients or Map of workers.
 */
public class Listener extends Thread {

    private final ServerSocket host;

    public MasterListener(ServerSocket host) {
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
                Socket incoming = host.accept();
                BufferedReader receiveID = new BufferedReader(new InputStreamReader(incoming.getInputStream()));

                String[] ID = receiveID.readLine().split(" ");
                String IDNum = ID[1];

                if (ID[0].equals("c")) {
                    // Create, store, and start a new ClientHandler
                    ClientHandler handler = new ClientHandler(IDNum, incoming, Main.getCollectedTasks());
                    Main.getClients().put(IDNum, handler);
                    handler.start();
                }

                else {
                    WorkerHandler handler = new WorkerHandler(IDNum, incoming);

                    if (ID[0].equals("a"))
                        Main.getAWorkers().put(IDNum, handler);

                    else
                        Main.getBWorkers().put(IDNum, handler);

                    handler.start();
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
