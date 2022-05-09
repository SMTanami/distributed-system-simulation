package sim.component;

/**
 * There are several types of components within this simulation. In order to identify which type of component connects
 * to the Conductor, they each pass a COMPONENT_TYPE value to their own {@link ComponentID}'s so that the COnductor
 * can identify what is what upon connection.
 */
public enum COMPONENT_TYPE {
    CLIENT,
    AWORKER,
    BWORKER;
}
