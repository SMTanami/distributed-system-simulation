package sim;

import sim.client.Client;
import sim.conductor.Conductor;
import sim.task.TASK_TYPE;
import sim.worker.Worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static sim.task.TASK_TYPE.A;
import static sim.task.TASK_TYPE.B;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(30121);
        Conductor conductor = new Conductor(serverSocket);
        Client clientOne = new Client(new Socket("127.0.0.1", 30121), 10);
        Client clientTwo = new Client(new Socket("127.0.0.1", 30121), 10);
        Worker workerA = new Worker(new Socket("127.0.0.1", 30121), A);
        Worker workerB = new Worker(new Socket("127.0.0.1", 30121), B);

        Thread conductorThread = new Thread(conductor::begin);
        Thread workerAThread = new Thread(workerA::begin);
        Thread workerBThread = new Thread(workerB::begin);
        Thread clientOneThread = new Thread(clientOne::begin);
        Thread clientTwoThread = new Thread(clientTwo::begin);

        conductorThread.start();
        workerAThread.start();
        workerBThread.start();
        clientOneThread.start();
        clientTwoThread.start();
    }
}
