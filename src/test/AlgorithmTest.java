package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.slave.SlaveA;
import sim.task.Task;
import sim.task.TaskA;
import sim.task.TaskB;

import java.io.IOException;
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

    @Test
    public void timeTakenToAssignTest() throws InterruptedException {

        Task[] tasks = initializeTasks(new Integer[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});

        //If A is available
        //Give Task 2 to SlaveA if and only if the following five tasks are of type 2
        //Otherwise, just give Task 2 to SlaveB
        //Continue

        int taskIndex = 0;
        int slaveATime = 0;
        int slaveBTime = 0;
        slaveA.start();
        slaveB.start();
        for (Task task : tasks) {

            if (task instanceof TaskA) {
                if (slaveA.isAvailable()) {
                    slaveA.setCurrentTask(task);
                    System.out.println("SlaveA given: " + task);
                }
                else {
                    if (isNextFiveTheSame(tasks, taskIndex, true) && slaveB.isAvailable()) {
                        slaveBTime += 10;
                        slaveB.setCurrentTask(task);
                        System.out.println("SlaveB given: " + task);
                    }
                    else {
                        while (!slaveA.isAvailable());
                        slaveATime += 2;
                        slaveA.setCurrentTask(task);
                        System.out.println("SlaveA given: " + task);
                    }
                }
            }

            else {
                if (slaveB.isAvailable()) {
                    slaveB.setCurrentTask(task);
                    System.out.println("SlaveB given: " + task);
                }
                else {
                    if (isNextFiveTheSame(tasks, taskIndex, false) && slaveA.isAvailable()) {
                        slaveATime += 10;
                        slaveA.setCurrentTask(task);
                        System.out.println("SlaveA given: " + task);
                    }
                    else {
                        while (!slaveB.isAvailable());
                        slaveBTime += 2;
                        slaveB.setCurrentTask(task);
                        System.out.println("SlaveB given: " + task);
                    }
                }
            }

            taskIndex++;
        }

        slaveA.setNeeded(false);
        slaveB.setNeeded(false);
        slaveA.join();
        slaveB.join();

        System.out.println("Slave A Time: " + slaveATime + "\nSlave B Time: " + slaveBTime);

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

    private Task[] initializeTasks(Integer[] order) {

        if (order.length != 15)
            throw new IllegalArgumentException("Array must of length 10");

        int clientID = 12903847;

        Task[] tasks = new Task[15];
        for (int i = 0; i < tasks.length; i++) {
            if (order[i] == 1)
                tasks[i] = new TaskA(clientID, i);
            else tasks[i] = new TaskB(clientID, i);
        }

        return tasks;
    }

    private boolean isNextFiveTheSame(Task[] tasks, int taskIndex, boolean isTaskA) {

        if (taskIndex >= tasks.length - 5)
            return false;

        else {
            int startIndex = taskIndex + 1;

            for (int i = startIndex; i < startIndex + 5; i++) {
                if (!(tasks[i].getClass() == tasks[taskIndex].getClass()))
                    return false;
            }

            return true;
        }
    }

}
