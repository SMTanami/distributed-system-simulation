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
     * @throws InterruptedException if thread is interrupted while asleep
     */
    public void execute(Task task) throws InterruptedException {
        if (task instanceof TaskA)
            if (isTypeA)
                Thread.sleep(2_000);
            else Thread.sleep(10_000);

        else
        if (isTypeA)
            Thread.sleep(10_000);
        else Thread.sleep(2_000);

    }

}

