package sim.component;

import sim.conductor.Conductor;

/**
 * There are two main components used within this simulation: Client and Worker. In order to identify which type of
 * component connects to the {@link Conductor}, all components will have a {@link ComponentID} which will contain a reference to
 * a component type. That ComponentID is then send to the conductor upon connection for identification.
 */
public enum COMPONENT_TYPE {
    CLIENT,
    WORKER
}
