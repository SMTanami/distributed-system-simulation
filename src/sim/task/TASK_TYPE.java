package sim.task;

import sim.client.Client;

/**
 * There are two different types of {@link Task} that can be requested by a {@link Client}. In order to identify which
 * type of Task is being requested, each Task contains a type.
 */
public enum TASK_TYPE {
    A,
    B
}
