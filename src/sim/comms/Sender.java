package sim.comms;

/**
 * This interface is to be implemented by any class that is meant to send messages to another component.
 * Given that so many classes required some sort of "sender" module, this interface was created to establish a
 * level of structure as well as for testing purposes.
 */
public interface Sender {

    /**
     * Sends outgoing data to the connected component
     */
    void send();

}
