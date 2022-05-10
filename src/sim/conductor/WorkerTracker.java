package sim.conductor;


import sim.conductor.comms.WorkerHandler;
import sim.observer.Observer;
import sim.task.Task;
import sim.worker.Worker;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static sim.task.TASK_TYPE.A;

/**
 * This class is used by the conductor to track all connected {@link Worker}s via their respective {@link WorkerHandler}.
 * Whenever a Worker of a specific type connects,a WorkerHandler is created, and it is added to the WorkerTracker and stored in a
 * {@link BlockingQueue} representing the connected Worker's worker type: A or B.
 * <p>
 * When a WorkerHandler is assigned a {@link Task} by the Conductor, it is removed form the internal blocking queue.
 * When that WorkerHandler completes its task, it notifies the WorkerTracker and in response the tracker returns it
 * within one of the Worker queues (depending on type, of course).
 */
public class WorkerTracker implements Observer {

    private final AtomicInteger aCount = new AtomicInteger(0);
    private final AtomicInteger bCount = new AtomicInteger(0);
    private final BlockingQueue<WorkerHandler> availableAWorkers = new ArrayBlockingQueue<>(25);
    private final BlockingQueue<WorkerHandler> availableBWorkers = new ArrayBlockingQueue<>(25);

    /**
     * Adds a WorkerHandler to the tracker. The tracker will deal with the specific type of WorkerHandler that is being added.
     * Additionally, increments a counter that is used to maintain the amount of WorkerHandlers of that type that have connected to the
     * {@link Conductor}.
     */
    public void add(WorkerHandler workerHandler) {
        if (workerHandler.getWorkerType() == A) {
            availableAWorkers.add(workerHandler);
            aCount.getAndIncrement();
        }

        else {
            availableBWorkers.add(workerHandler);
            bCount.getAndIncrement();
        }
    }

    /**
     * @return true if a {@link Worker} of type A has connected to the system, false otherwise.
     */
    public boolean isAConnected() { return aCount.get() > 0; }

    /**
     * @return true if a {@link Worker} of type B has connected to the system, false otherwise.
     */
    public boolean isBConnected() { return bCount.get() > 0; }

    /**
     * Checks the queue containing A workers to see if at least one is available.
     * @return true if an AWorker is available, false otherwise
     */
    public boolean isAFree() {
        return availableAWorkers.size() > 0;
    }

    /**
     * Checks the queue containing B workers to see if at least one is available.
     * @return true if an BWorker is available, false otherwise
     */
    public boolean isBFree() { return availableBWorkers.size() > 0; }

    /**
     * @return the amount of AWorkers being tracked by the tracker. Includes occupied and available workers into the count.
     */
    public int aCount() {
        return aCount.get();
    }

    /**
     * @return the amount of BWorkers being tracked by the tracker. Includes occupied and available workers into the count.
     */
    public int bCount() {
        return bCount.get();
    }

    /**
     * @return a WorkerHandler that is associated with an AWorker
     */
    public WorkerHandler getAHandler() {
        try {
            return availableAWorkers.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return a WorkerHandler that is associated with a BWorker
     */
    public WorkerHandler getBHandler() {

        try {
            return availableBWorkers.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the tracker in order to inform it of the given WorkerHandlers now availability.
     * @param o the WorkerHandler that is completed with its task
     */
    @Override
    public void update(Object o) {
        if (o instanceof WorkerHandler handler) {

            if (handler.getWorkerType() == A)
                availableAWorkers.add(handler);

            else availableBWorkers.add(handler);

        }
    }
}
