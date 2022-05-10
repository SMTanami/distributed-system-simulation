package sim.conductor;


import sim.conductor.comms.WorkerHandler;
import sim.observer.Observer;
import sim.task.TASK_TYPE;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static sim.component.COMPONENT_TYPE.WORKER;
import static sim.task.TASK_TYPE.A;

/**
 * This class is used by the conductor to track all connected Workers via their respective WorkerHandlers. Whenever
 * a WorkerHandler of a specific type connects, it is added to the WorkerTracker and stored in a collection representing
 * the connected Worker's worker type: A or B.
 * <p>
 * When a WorkerHandler is assigned a task by the Conductor, the tracker moves it to an occupied list. When that WorkerHandler
 * completes its task, it notifies the WorkerTracker and in response the tracker then removes the WorkerHandler from the
 * occupied list and places it within one of the available Worker queues. Each queue representing either A and B workers.
 */
public class WorkerTracker implements Observer {

    private final BlockingQueue<WorkerHandler> availableAWorkers = new ArrayBlockingQueue<>(25);
    private final BlockingQueue<WorkerHandler> availableBWorkers = new ArrayBlockingQueue<>(25);
    private boolean AWorkerExists = false;
    private boolean BWorkerExists = false;

    /**
     * Adds a WorkerHandler to the tracker. The tracker will deal with the specific type of WorkerHandler that is being added.
     * @param workerHandler the WorkerHandler to add to the tracker
     */
    public void add(WorkerHandler workerHandler) {
        if (workerHandler.getWorkerType() == A) {
            availableAWorkers.add(workerHandler);
            AWorkerExists = true;
        }

        else {
            availableBWorkers.add(workerHandler);
            BWorkerExists = true;
        }
    }

    /**
     * @return true if a {@link sim.worker.Worker} of type A has connected to the system.
     */
    public boolean isAConnected() { return AWorkerExists; }

    /**
     * @return true if a {@link sim.worker.Worker} of type B has connected to the system.
     */
    public boolean isBConnected() { return BWorkerExists; }

    /**
     * Checks the head of the availableWorker queue to see if an AWorker is available.
     * @return true if an AWorker is available, false otherwise
     */
    public boolean isAFree() {
        return availableAWorkers.peek() != null;
    }

    /**
     * Checks the head of the availableWorker queue to see if an BWorker is available.
     * @return true if an BWorker is available, false otherwise
     */
    public boolean isBFree() { return availableBWorkers.peek() != null; }

    /**
     * @return the amount of AWorkers being tracked by the tracker. Includes occupied and available workers into the count.
     */
    public synchronized int aCount() {
        return availableAWorkers.size();
    }

    /**
     * @return the amount of BWorkers being tracked by the tracker. Includes occupied and available workers into the count.
     */
    public synchronized int bCount() {
        return availableBWorkers.size();
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
