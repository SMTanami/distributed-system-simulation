/*
 *  This class is used by the Master and its threads to keep track of each Slave's occupied status.
 */

public class isSlaveOccupied {
    boolean isOccupied;

    public isSlaveOccupied(boolean isOccupied) {
        this.isOccupied = isOccupied;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public synchronized void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }
}
