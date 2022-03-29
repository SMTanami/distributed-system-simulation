package test;

import sim.task.Task;
import sim.task.TaskA;

/**
 * A Mock class to mock slaves. This class can be instantiated as a Type A Slave or
 * Type B Slave. It will execute tasks as if thwy were given to real Slaves.
 */
public class SlaveMock {

    private final boolean isTypeA;

    /**
     * @param isA Should be true if the SlaveMock instance should mock a Slave of type A.
     *            Should be false if the SlaveMock instance should mock a Slave of type B.
     */
    public SlaveMock(boolean isA) {
        this.isTypeA = isA;
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

        Thread.sleep(sleepAmount);
        return sleepAmount / 1000;
    }
}

