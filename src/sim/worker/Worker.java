package sim.worker;

import sim.task.TASK_TYPE;
import sim.task.Task;
import sim.component.ComponentID;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import static sim.component.COMPONENT_TYPE.WORKER;

public class Worker {

    private final Socket myWorkerSocket;
    private final ComponentID COMPONENT_ID;
    private final TASK_TYPE workerType;
    private ObjectOutputStream objOut;

    public Worker(Socket workerSocket, TASK_TYPE workerType) {
        this.myWorkerSocket = workerSocket;
        this.workerType = workerType;
        COMPONENT_ID = new ComponentID(WORKER, new Random().nextInt());
        initializeStreams();
    }

    private void initializeStreams() {
        try {
            objOut = new ObjectOutputStream(myWorkerSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void begin() {

        notifyConductor();
        try(ObjectInputStream objIn = new ObjectInputStream(myWorkerSocket.getInputStream()))
        {
            Task task;
            while ((task = (Task) objIn.readObject()) != null) {
                System.out.println(COMPONENT_ID + ": received " + task);

                if (task.type() == workerType) {
                    Thread.sleep(2000);
                    System.out.println(COMPONENT_ID.component_type() + "This task should take 2 seconds.");
                }

                else {
                    Thread.sleep(10000);
                    System.out.println("This task should take 10 seconds.");
                }

                System.out.println("Completed: " + task);
                objOut.writeObject(task);
            }
        }

        catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void notifyConductor() {
        try {
            objOut.writeObject(COMPONENT_ID);
            objOut.writeObject(workerType);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Worker: Could not send component ID to conductor");
        }
    }

    public ComponentID getCOMPONENT_ID() {
        return COMPONENT_ID;
    }

    public TASK_TYPE getWorkerType() {
        return workerType;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println("Usage: java Client <hostName> <portNumber> <worker type>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        TASK_TYPE workerType = TASK_TYPE.valueOf(args[2]);

        Worker worker = new Worker(new Socket(hostName, portNumber), workerType);
        worker.begin();
    }
}
