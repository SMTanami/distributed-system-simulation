package Master;

import Task.Task;
import Task.TaskA;
import java.util.Map;
import java.util.Queue;

public class AssignmentAlgorithm {
    private final Queue<Task> collectedTasks;
    private final Map<String, WorkerHandler> AWorkers;
    private final WorkerHandler[] aArray;
    private final Map<String, WorkerHandler> BWorkers;
    private final WorkerHandler[] bArray;

    public AssignmentAlgorithm(Queue<Task> collectedTasks, Map<String, WorkerHandler> AWorkers, Map<String, WorkerHandler> BWorkers) {
        this.collectedTasks = collectedTasks;
        this.AWorkers = AWorkers;
        aArray = AWorkers.values().toArray(new WorkerHandler[0]);
        this.BWorkers = BWorkers;
        bArray = BWorkers.values().toArray(new WorkerHandler[0]);
    }
/*
    public WorkerHandler assignWorker(Task task) {
        if (task.getClass() == TaskA.class) {
            if (!isAOccupied.isOccupied()) {
                return 'a';
            }

            else if (isBOccupied.isOccupied() || collectedTasks.size() < 5 || !areNextFiveSame(collectedTasks, task)) {
                while (isAOccupied.isOccupied());
                return 'a';
            }

            else {
                return 'b';
            }
        }

        else {
            if (!isBOccupied.isOccupied()) {
                return 'b';
            }

            else if (isAOccupied.isOccupied() || collectedTasks.size() < 5 || !areNextFiveSame(collectedTasks, task)) {
                while (isBOccupied.isOccupied());
                return 'b';
            }

            else {
                return 'a';
            }
        }
    }

    public static boolean areNextFiveSame(Queue<Task> taskQueue, Task task) {
        Task[] taskArray = taskQueue.toArray(new Task[0]);
        for (int i = 0; i < 5; i++) {
            if (taskArray[i].getClass() != task.getClass()) {
                return false;
            }
        }
        return true;
    }
*/
}
