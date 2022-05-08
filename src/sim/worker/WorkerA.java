package sim.worker;

import sim.task.Task;
import sim.task.TaskA;
import sim.component.Component;
import sim.component.ComponentID;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Random;

import static sim.component.COMPONENT_TYPE.AWORKER;

public class WorkerA implements Component, Worker {

    private transient final Socket myWorkerSocket;
    private transient ObjectOutputStream objOut;
    private final ComponentID COMPONENT_ID;

    public WorkerA(Socket workerSocket) {
        myWorkerSocket = workerSocket;
        COMPONENT_ID = new ComponentID(AWORKER, new Random().nextInt());
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
                    System.out.println(COMPONENT_ID.component_type() + "This task should take 2 seconds.");
                }

                else {
                    Thread.sleep(3000);
                    System.out.println("This task should take 3 seconds.");
                }

                System.out.println("Completed: " + task);
                objOut.writeObject(task);
            }
        }

        catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyConductor() {
        try {
            objOut.writeObject(COMPONENT_ID);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("WorkerA: Could not send component ID to conductor");
        }
    }

    public static void main(String[] args) throws IOException {

//        if (args.length != 2) {
//            System.err.println("Usage: java Client <hostName> <portNumber>");
//            System.exit(1);
//        }
//
//        String hostName = args[0];
//        int portNumber = Integer.parseInt(args[1]);

        WorkerA a = new WorkerA(new Socket("127.0.0.1", 30121));
        a.begin();
    }
}
