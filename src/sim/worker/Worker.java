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
    private final ComponentID componentID;
    private final TASK_TYPE workerType;
    private ObjectOutputStream objOut;

    public Worker(Socket workerSocket, TASK_TYPE workerType) {
        this.myWorkerSocket = workerSocket;
        this.workerType = workerType;
        componentID = new ComponentID(WORKER, new Random().nextInt());
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
                System.out.printf("WORKER(%s) %d: Received task of type %s\n", workerType, componentID.refID(), task.type());

                if (task.type() == workerType) {
                    Thread.sleep(2000);
                    System.out.printf("WORKER(%s) %d: This task should take %d seconds\n", workerType, componentID.refID(), 2);
                }

                else {
                    Thread.sleep(10000);
                    System.out.printf("WORKER(%s) %d: This task should take %d seconds\n", workerType, componentID.refID(), 10);
                }

                System.out.printf("WORKER(%s) %d: Completed task %s\n", workerType, componentID.refID(), task);
                objOut.writeObject(task);
            }
        }

        catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void notifyConductor() {
        try {
            objOut.writeObject(componentID);
            objOut.writeObject(workerType);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Worker: Could not send component ID to conductor");
        }
    }

    public ComponentID getComponentID() {
        return componentID;
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
