package sim.obersver;

import sim.conductor.WorkerTracker;
import sim.conductor.cwcomms.WorkerHandler;

public interface Observable {

    void register(WorkerTracker tracker);

    void unregister(WorkerTracker tracker);

    void notifyObservers();
}
