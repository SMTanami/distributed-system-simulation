package test;

import sim.task.Task;
import sim.task.TaskA;

/**
 * A Mock class to mock slaves. This class can be instantiated as a Type A Slave or
 * Type B Slave. It will execute tasks as if thwy were given to real Slaves.
 */
public class SlaveMock extends Thread {

    private final boolean isTypeA;
    private boolean isOccupied = false;
    private boolean isNeeded = true;
    private Task currentTask;

    /**
     * @param isA Should be true if the SlaveMock instance should mock a Slave of type A.
     *            Should be false if the SlaveMock instance should mock a Slave of type B.
     */
    public SlaveMock(boolean isA) {
        this.isTypeA = isA;
    }

    @Override
    public void run() {

        while (isNeeded) {
            if (currentTask != null)
            {
                try {
                    isOccupied = true;
                    execute(currentTask);
                    currentTask = null;
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            isOccupied = false;
        }

    }

    public synchronized void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    /**
     * @param task task to execute
     * @return the amount of seconds the slave needed to complete the task
     * @throws InterruptedException if thread is interrupted while asleep
     */
    public int execute(Task task) throws InterruptedException {

        int sleepAmount = 0;

        if (task instanceof TaskA)
            if (isTypeA)
                sleepAmount = 2_000;
            else sleepAmount = 10_000;

        else
        if (isTypeA)
            sleepAmount = 10_000;
        else sleepAmount = 2000;

        sleep(sleepAmount);
        if (isTypeA)
            System.out.println("Slave A completed: " + currentTask);
        else System.out.println("Slave B completed: " + currentTask);
        return sleepAmount / 1000;
    }

    /**
     * @return true if the slave is available, false otherwise
     */
    public boolean isAvailable() {
        return !isOccupied;
    }

    public synchronized void setNeeded(boolean needed) {
        isNeeded = needed;
    }
}

