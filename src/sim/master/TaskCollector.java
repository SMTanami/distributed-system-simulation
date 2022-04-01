package sim.master;

import sim.task.Task;

import java.io.IOException;
import java.io.ObjectInputStream;

public class TaskCollector extends Thread {
    
    private final ObjectInputStream myTaskInputStream;
    private final TaskConfirmer myConfirmer;
    private final int clientID;
    
    public TaskCollector(int clientID, TaskConfirmer confirmer, ObjectInputStream taskInputStream) {
        this.clientID = clientID;
        this.myConfirmer = confirmer;
        this.myTaskInputStream = taskInputStream;
    }
    
    @Override
    public void run() {
        
        Task taskToComplete;
        while ((taskToComplete = inStreamTask()).getClientID() != 0)
        {
            //TODO put into a collection for processing?
        }

        // Close the given input stream
        try {
            myTaskInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Notify conformer of completion
    }

    public int getClientID() {
        return clientID;
    }

    private Task inStreamTask() {
        
        try {
            return (Task) myTaskInputStream.readObject();
        } 
        
        catch (IOException | ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
        
        return null;
    }

    private void terminateConfirmer() {
        myConfirmer.terminate();
    }
}
