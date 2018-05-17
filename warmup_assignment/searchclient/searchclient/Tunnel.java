package searchclient;

import java.util.*;

/**
 * Created by olafurorn on 5/13/18.
 */
public class Tunnel {

    // Containing coordinates of cells in the tunnel and the position of them
    // within the cell, starting from 1 in the deepest cell
    private Map<Coordinate, Integer> tunnelCells = new HashMap<Coordinate, Integer>();

    // Lists of goals, their position and their coordinates in the tunnel where the first item
    // in the list is the goal which is the deepest in the tunnel
    private Map<Goals, Integer> goalsList = new HashMap<Goals, Integer>();
    private Map<Integer, Coordinate> goalsCoordinates = new HashMap<Integer, Coordinate>();

    private boolean tunnelEndDetected = false;

    public Tunnel() {

    }

    public void onTunnelEndDetected() { // TODO: call this method
        tunnelEndDetected = true;
        tunnelCells = sortByValue(tunnelCells);
        goalsList = sortByValue(goalsList);
    }

    public void addCellToTunnel(/* Not Null */Coordinate cellCoordinate, /* Nullable*/ Goals goal) {
        if (tunnelEndDetected) {
            throw new IllegalStateException("Can't add new cells to tunnel as the tunnel end has been detected");
        }
        if (tunnelCells.containsKey(cellCoordinate)) {
            throw new IllegalStateException("Coordinate already exist in the tunnel");
        }

        tunnelCells.put(cellCoordinate, tunnelCells.size() + 1);

        if (goal != null) {
            int position = tunnelCells.size();
            goalsList.put(goal, position);
            goalsCoordinates.put(position, cellCoordinate);
        }
    }

    public boolean goalInTunnel(Goals goal) {
        return goalsList.containsKey(goal);
    }

    public int getTunnelLength() {
        return tunnelCells.size() + 1;
    }


    /*****************
     *
     * HELPER METHODS
     */

    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;

    }

    /*****************
     *
     * GET METHODS
     */

    public Map<Coordinate, Integer> getTunnelCells() {
        return tunnelCells;
    }

    public Map<Goals, Integer> getGoalsList() {
        return goalsList;
    }

    public Map<Integer, Coordinate> getGoalsCoordinates() {
        return goalsCoordinates;
    }

    @Override
    public String toString() {
        return "Tunnel{" +
                "tunnelCells=" + tunnelCells +
                ", goalsList=" + goalsList +
                ", goalsCoordinates=" + goalsCoordinates +
                ", tunnelEndDetected=" + tunnelEndDetected +
                '}';
    }
}
