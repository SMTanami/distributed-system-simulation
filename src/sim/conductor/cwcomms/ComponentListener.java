package sim.conductor.cwcomms;

import sim.client.Client;
import sim.component.ComponentID;
import sim.conductor.Conductor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class will listen for clients and workers that are looking to connect to the given server socket. Upon establishing a connection,
 * a {@link ClientHandler} or a {@link WorkerHandler} will be created and placed within the master's Map of clients or Map of workers.
 */
public class ComponentListener extends Thread {

    private final ServerSocket host;

    public ComponentListener(ServerSocket host) {
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
            try {
                Socket incomingComponent = host.accept();
                ObjectInputStream objIn = new ObjectInputStream(incomingComponent.getInputStream());
                ComponentID componentID = (ComponentID) objIn.readObject();

                if (componentID.component() instanceof Client) {
                    ClientHandler clientHandler = new ClientHandler(componentID.refID(), incomingComponent);
                    clientHandler.setTaskCollection();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try{
                // Receive incoming client socket
                Socket incoming = host.accept();
                BufferedReader receiveID = new BufferedReader(new InputStreamReader(incoming.getInputStream()));

                String[] ID = receiveID.readLine().split(" ");
                String IDNum = ID[1];

                if (ID[0].equals("c")) {
                    // Create, store, and start a new ClientHandler
                    ClientHandler handler = new ClientHandler(IDNum, incoming, Conductor.getCollectedTasks());
                    Conductor.getClients().put(Integer.valueOf(IDNum), handler);
                    handler.start();
                }

                else {
                    WorkerHandler handler = new WorkerHandler(IDNum, incoming);

                    if (ID[0].equals("a"))
                        Conductor.getAWorkers().put(IDNum, handler);

                    else
                        Conductor.getBWorkers().put(IDNum, handler);

                    handler.start();
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
