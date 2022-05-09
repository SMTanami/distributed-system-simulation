package sim.worker;

import sim.task.Task;
import sim.task.TaskA;
import sim.component.ComponentID;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import static sim.component.COMPONENT_TYPE.BWORKER;

public class WorkerB {

    private final Socket myWorkerSocket;
    private final ComponentID COMPONENT_ID;
    private ObjectOutputStream objOut;

    public WorkerB(Socket workerSocket) {
        myWorkerSocket = workerSocket;
        COMPONENT_ID = new ComponentID(BWORKER, new Random().nextInt());
        initializeStreams();
    }

    private void initializeStreams() {
        try {
            objOut = new ObjectOutputStream(myWorkerSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("WorkerA: Could not instantiate streams");
        }
    }

    public void begin() {

        notifyConductor();
        try(ObjectInputStream objIn = new ObjectInputStream(myWorkerSocket.getInputStream()))
        {
            Task task;
            while ((task = (Task) objIn.readObject()) != null) {
                System.out.println(COMPONENT_ID + ": received " + task);

                if (task.getClass() == TaskA.class) {
                    Thread.sleep(2000);
                    System.out.println("This task should take 2 seconds.");
                }

                else {
                    Thread.sleep(3000);
                    System.out.println(COMPONENT_ID.component_type() + "This task should take 3 seconds.");
                }

                System.out.println(COMPONENT_ID + ": completed " + task);
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
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("WorkerB: Could not send component ID to conductor");
        }
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("Usage: java Client <hostName> <portNumber>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        WorkerB b = new WorkerB(new Socket(hostName, portNumber));
        b.begin();
    }
}
