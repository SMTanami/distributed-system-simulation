package sim.master;

import sim.task.Task;

import java.io.IOException;
import java.io.ObjectInputStream;

public class TaskCollector extends Thread {
    
    private final int clientID;
    private final ObjectInputStream myTaskInputStream;
    
    public TaskCollector(int clientID, ObjectInputStream taskInputStream) {
        this.clientID = clientID;
        this.myTaskInputStream = taskInputStream;
    }
    
    @Override
    public void run() {
        
        Task taskToComplete;
        while ((taskToComplete = getTask()) != null)
        {
            //TODO put into a collection for processing?
        }
        
    }
    
    private Task getTask() {
        
        try {
            return (Task) myTaskInputStream.readObject();
        } 
        
        catch (IOException | ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
