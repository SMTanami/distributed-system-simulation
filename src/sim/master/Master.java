package sim.master;

import sim.master.cmcomms.ClientHandler;
import sim.master.cmcomms.ClientListener;
import sim.task.Task;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Master {

    private static final Map<Integer, ClientHandler> CLIENTS = Collections.synchronizedMap(new HashMap<>());
    private static final BlockingQueue<Task> COLLECTED_TASKS = new ArrayBlockingQueue<>(100);

    /**
     * Initiate the collection of clients and retrival and processing of their tasks
     * @param args list of CL arguments. Should only contain a port number.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // Ensure a single argument is used when using this program
        if (args.length != 1){
            System.out.println("Server args: <portNumber>");
            System.exit(1);
        }

        // Get port number from CL args
        int portNumber = Integer.parseInt(args[0]);

        // Instantiate ServerSocket called host, set the ClientListener's host to it, and start the listener
        try(ServerSocket host = new ServerSocket(portNumber)) {
            ClientListener listener = new ClientListener(host);
            listener.start();
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

    public static BlockingQueue<Task> getCollectedTasks() { return COLLECTED_TASKS; }
}
