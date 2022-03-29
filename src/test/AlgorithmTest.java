package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.task.Task;
import sim.task.TaskA;
import sim.task.TaskB;

import java.util.Random;

public class AlgorithmTest {

    SlaveMock slaveA = new SlaveMock(true);
    SlaveMock slaveB = new SlaveMock(false);
    Task[] tasks = initializeTasks();

    //-----Test Methods-----

    @Test
    public void somethingTest() throws InterruptedException {

        int timeTaken = 0;
        for (Task t : tasks) {
            timeTaken += slaveA.execute(t);
            System.out.println("Executed task " + t + ", current time: " + timeTaken);
        }

        System.out.printf("Time taken to complete all tasks: %d seconds", timeTaken);
        Assertions.assertTrue(timeTaken < 50);
    }

    //-----Utility Methods-----

    /**
     * A utility method for use of the Algorithm test class to generate an array of Tasks.
     * @return an array of Tasks of both Type A and Type B, all with the same clientID
     */
    private Task[] initializeTasks() {

        Task[] tasks = new Task[15];
        Random random = new Random();
        int clientID = random.nextInt();

        for (int i = 0; i < 15; i++) {

            if (random.nextDouble() > 0.50)
                tasks[i] = new TaskA(clientID, i);

            else tasks[i] = new TaskB(clientID, i);

        }

        return tasks;
    }

}
