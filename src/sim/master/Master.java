package sim.master;

import sim.master.cmcomms.ClientHandler;
import sim.master.cmcomms.ClientListener;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Master {

    private static final Map<Integer, ClientHandler> CLIENTS = Collections.synchronizedMap(new HashMap<>());

    /**
     * Initiate the collection of clients and retrival and processing of their tasks
     * @param args list of CL arguments. Should only contain a port number.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // Get port number from CL args
        if (args.length != 1){
            System.out.println("Server args: <portNumber>");
            System.exit(1);
        }

        // Instantiate a server socket and give it to the acceptor, so it can begin to listen for clients
        int portNumber = Integer.parseInt(args[0]);

        // Place first client into collection and initiate ClientHandler thread to assume that responsibility moving forward
        start(portNumber);

        // While the collector map is not empty, clear any collectors that have terminated
        do {
            cleanTerminatedClients();
        } while (CLIENTS.size() > 0);
    }

    /**
     * To be used privately by the master, this method listens for the first client to connect to.
     * Once that initial connection is established, the master creates and starts a ClientHandler for the
     * received client.
     * <p>
     * Now that an initial connection has been made and a ClientHandler has been started, the master creates and starts
     * a {@link ClientListener} that will oversee the responsibility of listening for oncoming clients moving forward.
     * @param portNumber port number of the server retrieved from CL args
     * @throws IOException the stream has been closed and the contained input stream does not support reading after close,
     * or another I/O error occurs.
     */
    private static void start(int portNumber) throws IOException {
        // Accept initial client socket
        try(ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket incomingClient = serverSocket.accept();
            DataInputStream dataIn = new DataInputStream(incomingClient.getInputStream()))
        {
            // Create initial ClientHandler
            int clientID = dataIn.readInt(); // Request clientID upon first connecting with it
            ClientHandler handler = new ClientHandler(clientID, incomingClient);
            // Start the handler and add it to the CLIENTS map
            handler.start();
            CLIENTS.put(handler.getClientID(), handler);
            // Start ClientListener to continue to listen for new clients
            ClientListener clientListener = new ClientListener();
            clientListener.setHost(serverSocket);
            clientListener.start();
        }
    }

    /**
     * To be used privately by the master, this method iterates over stored Collector Threads that have been started
     * and removes them from the collection if they have been terminated.
     */
    private static void cleanTerminatedClients() {

        for (ClientHandler handler : CLIENTS.values())
        {
            if (handler.isTerminated()) {
                CLIENTS.remove(handler.getClientID());
            }
        }
    }

    /**
     * @return this Masters map of Client's ({@link ClientHandler}s)
     */
    public static Map<Integer, ClientHandler> getClients() {
        return CLIENTS;
    }
}
