package sim.component;

import java.io.Serializable;

public record ComponentID(COMPONENT_TYPE component_type, int refID) implements Serializable {}
