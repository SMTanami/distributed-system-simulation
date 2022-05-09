package sim.conductor;


import sim.conductor.comms.WorkerHandler;
import sim.observer.Observer;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static sim.component.COMPONENT_TYPE.AWORKER;

public class WorkerTracker implements Observer {

    private final ArrayList<WorkerHandler> occupiedAWorkers = new ArrayList<>();
    private final ArrayList<WorkerHandler> occupiedBWorkers = new ArrayList<>();
    private final BlockingQueue<WorkerHandler> availableAWorkers = new ArrayBlockingQueue<>(25);
    private final BlockingQueue<WorkerHandler> availableBWorkers = new ArrayBlockingQueue<>(25);

    public void add(WorkerHandler workerHandler) {
        if (workerHandler.getComponentID().component_type() == AWORKER)
            availableAWorkers.add(workerHandler);

        else availableBWorkers.add(workerHandler);
    }

    public boolean isAFree() {
        return availableAWorkers.peek() != null;
    }

    public boolean isBFree() { return availableBWorkers.peek() != null; }

    public int aCount() {
        return availableAWorkers.size() + occupiedAWorkers.size();
    }

    public int bCount() {
        return availableBWorkers.size() + occupiedBWorkers.size();
    }

    public WorkerHandler getAHandler() {

        try {
            WorkerHandler handler = availableAWorkers.take();
            occupiedAWorkers.add(handler);
            return handler;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public WorkerHandler getBHandler() {

        try {
            WorkerHandler handler = availableBWorkers.take();
            occupiedBWorkers.add(handler);
            return handler;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Object o) {
        if (o instanceof WorkerHandler handler)
        {
            if (handler.getComponentID().component_type() == AWORKER) {
                occupiedAWorkers.remove(handler);
                availableAWorkers.add(handler);
            }

            else{
                occupiedBWorkers.remove(handler);
                availableBWorkers.add(handler);
            }
        }
    }
}
