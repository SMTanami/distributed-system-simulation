package sim.worker;

import sim.conductor.Conductor;
import sim.task.TASK_TYPE;
import sim.task.Task;
import sim.component.ComponentID;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import static sim.component.COMPONENT_TYPE.WORKER;

/**
 * This class acts as a Worker in a distributed system. It receives {@link Task} to complete from the {@link Conductor},
 * performs the task (by sleeping for a certain amount of time) and returns the completed task to the Conductor.
 * <p>
 * Once the Worker has been started, it immediately notifies the Conductor of its request to connect to it. Once that
 * connection is established, the Worker will be able to perform tasks for the Conductor. The amount of time to perform
 * any particular task is dependent on the Worker's worker type. If the worker type matches the particular task's task
 * type, then the Worker will take 2 seconds to complete the task. Otherwise, the Worker will take 10 seconds to
 * complete the task.
 * <p>
 * To use this class, start it from the command line by passing in a host name, port number, and the worker type.
 */
public class Worker {

    private final Socket myWorkerSocket;
    private final ComponentID componentID;
    private final TASK_TYPE workerType;
    private ObjectOutputStream objOut;

    /**
     * @param workerSocket the socket that the Worker will use to send and receive tasks from the Conductor
     * @param workerType the type of Worker
     */
    public Worker(Socket workerSocket, TASK_TYPE workerType) {
        this.myWorkerSocket = workerSocket;
        this.workerType = workerType;
        componentID = new ComponentID(WORKER, new Random().nextInt());
        initializeStreams();
    }

    /**
     * This method is internally used by the Worker. It uses the worker socket used by this worker instance to
     * instantiate an output stream to communicate with the Conductor.
     */
    private void initializeStreams() {
        try {
            objOut = new ObjectOutputStream(myWorkerSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs all of the Task work. Reads every task sent by the Conductor, performs the task (causing the Worker to
     * sleep for a specified amount of time), and then writes the completed task back to the Conductor.
     */
    public void begin() {

        notifyConductor();
        try(ObjectInputStream objIn = new ObjectInputStream(myWorkerSocket.getInputStream()))
        {
            Task task;
            while ((task = (Task) objIn.readObject()) != null) {
                System.out.printf("WORKER(%s) %d: Received task of type %s\n", workerType, componentID.refID(), task.type());

                if (task.type() == workerType) {
                    System.out.printf("WORKER(%s) %d: This task should take %d seconds\n", workerType, componentID.refID(), 2);
                    Thread.sleep(2000);
                }

                else {
                    System.out.printf("WORKER(%s) %d: This task should take %d seconds\n", workerType, componentID.refID(), 10);
                    Thread.sleep(10000);
                }

                System.out.printf("WORKER(%s) %d: Completed task %s\n", workerType, componentID.refID(), task);
                objOut.writeObject(task);
            }
        }

        catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the Worker's {@link ComponentID} and workerType to the Conductor to let the Conductor prepare for its
     * oncoming connection.
     */
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

    /**
     * @return componentID
     */
    public ComponentID getComponentID() {
        return componentID;
    }

    /**
     * @return workerType
     */
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
