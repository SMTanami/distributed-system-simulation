package sim;

import sim.client.Client;
import sim.conductor.Conductor;
import sim.worker.WorkerA;
import sim.worker.WorkerB;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(30121);
        Conductor conductor = new Conductor(serverSocket);
        Client clientOne = new Client(new Socket("127.0.0.1", 30121), 10);
        Client clientTwo = new Client(new Socket("127.0.0.1", 30121), 10);
        WorkerA workerA = new WorkerA(new Socket("127.0.0.1", 30121));
        WorkerB workerB = new WorkerB(new Socket("127.0.0.1", 30121));

        Thread conductorThread = new Thread(() -> {
            try
            {
                conductor.begin();
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
        Thread workerAThread = new Thread(workerA::begin);
        Thread workerBThread = new Thread(workerB::begin);
        Thread clientOneThread = new Thread(() -> {
            try
            {
                clientOne.begin();
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
        Thread clientTwoThread = new Thread(() -> {
            try
            {
                clientTwo.begin();
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });

        conductorThread.start();
        workerAThread.start();
        workerBThread.start();
        clientOneThread.start();
        clientTwoThread.start();

        clientTwoThread.join();
    }

}
