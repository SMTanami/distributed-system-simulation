package sim.observer;

/**
 * Any class that wishes to Observe other observable objects should implement this class.
 * Observable objects will update any concrete observer via this interfaces {@code update()}
 * method.
 * <p>
 * In this case, the implementation is such that it follows the "push" model, in which
 * updates come along with an object for the concrete observer to use in some way if needed.
 */
public interface Observer {

    /**
     * Updates the concrete observer with the given object.
     * @param o the object that should be pushed to the observing object
     */
    void update(Object o);

}
