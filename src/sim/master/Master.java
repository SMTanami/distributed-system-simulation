package sim.master;

import sim.task.Task;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Master {

    private static final Acceptor ACCEPTOR = new Acceptor();
    private static final Map<Integer, TaskConfirmer> CONFIRMER_MAP = Collections.synchronizedMap(new HashMap<>());
    private static final List<Task> COMPLETED_TASK_LIST = new LinkedList<>();

    //Where does confirmer live? - confirmer map
    //How do we identify it? - its client ID key value
    //How do we tell it what task is done? - we'll create a class for that

    public static void main(String[] args) throws IOException {

        // Get port number from CL args
        if (args.length != 1){
            System.out.println("Server args: <portNumber>");
            System.exit(1);
        }

        // Instantiate a server socket and give it to the acceptor so it can begin to listen for clients
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        ACCEPTOR.setHost(serverSocket);
        ACCEPTOR.start();
    }

    public static Map<Integer, TaskConfirmer> getConfirmerMap() {
        return CONFIRMER_MAP;
    }
}
