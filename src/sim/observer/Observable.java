package sim.observer;

/**
 * This interface should be implemented by any class that would like to be observable by other classes.
 * Any class that is observable can have {@link Observer} instances register onto it. All registered observers
 * will be notified when an observable decides to update it.
 */
public interface Observable {

    /**
     * @param observer the observer that would like to register and observe this observable.
     */
    void register(Observer observer);

    /**
     * @param observer the observer that would no longer wish to observe this observable instance.
     */
    void unregister(Observer observer);

    /**
     * Calls each registered observers {@code update()} method.
     */
    void notifyObservers();
}
