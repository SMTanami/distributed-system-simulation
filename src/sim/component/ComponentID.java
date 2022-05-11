package sim.component;

import sim.task.TASK_TYPE;

import java.io.Serializable;

/**
 * When a component is created, a unique ComponentID is always created alongside it so that the Conductor can
 * pick the specific component out of a collection of many. Make sure to instantiate a ComponentID automatically for all
 * components.
 * @param component_type the type of component being created
 * @param refID the unique ID number that will be used to identify the unique component among many of the same
 *              component type
 */
public record ComponentID(COMPONENT_TYPE component_type, int refID) implements Serializable {

    @Override
    public String toString() {
        return
                "ComponentID: type="
                + component_type
                + ", refID="+ refID;
    }
}
