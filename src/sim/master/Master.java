package sim.master;

import sim.task.Task;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Master {

    private static final Map<Integer, TaskConfirmer> CONFIRMER_MAP = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Integer, TaskCollector> COLLECTOR_MAP = Collections.synchronizedMap(new HashMap<>());
    public static final BlockingQueue<Task> COMPLETED_TASK_LIST = new ArrayBlockingQueue<>(100);

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
        } while (COLLECTOR_MAP.size() > 0);
    }

    /**
     * To be used privately by the master, this method listens for the first client to connect to the master.
     * Once that initial connection is established, the master creates and starts a collector and confirmer for the
     * received client.
     * <p>
     * Now that an initial connection has been made, the master creates and starts a ClientHandler that will oversee the
     * responsibility of handinling oncoming clients moving forward.
     * @param portNumber port number of the server retrieved from CL args
     * @throws IOException
     */
    private static void start(int portNumber) throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket newClient = serverSocket.accept();
            DataInputStream dataIn = new DataInputStream(newClient.getInputStream()))
        {
            int clientID = dataIn.readInt(); // Request clientID upon first connecting with it
            TaskConfirmer confirmer = new TaskConfirmer(clientID, new DataOutputStream(newClient.getOutputStream()));
            TaskCollector collector = new TaskCollector(clientID, confirmer, new ObjectInputStream(newClient.getInputStream()));
            storeAndStartCollectorConfirmer(collector, confirmer);
            ClientHandler clientHandler = new ClientHandler();
            clientHandler.setHost(serverSocket);
            clientHandler.start();
        }
    }

    /**
     * Stores the given Collector and Confirmer and starts them.
     * @param collector the new Collector Thread to be spun up and started
     * @param confirmer the new Confirmer Thread to be spun up and started
     */
    public static void storeAndStartCollectorConfirmer(TaskCollector collector, TaskConfirmer confirmer) {

        CONFIRMER_MAP.put(confirmer.getClientID(), confirmer);
        COLLECTOR_MAP.put(collector.getClientID(), collector);

        collector.start();
        confirmer.start();
    }

    /**
     * To be used privately by the master, this method iterates over stored Collector Threads that have been started
     * and removes them from the collection if they have been terminated.
     */
    private static void cleanTerminatedClients() {

        int index = 0;
        for (TaskCollector collector : COLLECTOR_MAP.values())
        {
            if (!collector.isAlive())
            {
                COLLECTOR_MAP.remove(collector.getClientID());
                CONFIRMER_MAP.remove(collector.getClientID());
            }
        }
    }
}
