package searchclient;


import java.util.HashMap;
import java.util.Set;

/**
 * Created by olafurorn on 5/12/18.
 */
public class AgentCommunications {

    private HashMap<Coordinate, Box> touchedBoxesOfOtherColor = new HashMap<Coordinate, Box>();
    private Coordinate originalCoordiante;

    public enum Type {
        TOUCHING_BOX_OF_OTHER_COLOR
    }

    public AgentCommunications() {

    }

    public Box getTouchedBox() {

        return touchedBoxesOfOtherColor.get(originalCoordiante);
    }

    public void removeBox() {
        touchedBoxesOfOtherColor.remove(originalCoordiante);
    }

    public Coordinate getOriginalCoordinate() {
        return originalCoordiante;
   }
    public void onBoxWithOtherColorTouched(Coordinate coordinate, Box box) {
        this.originalCoordiante = coordinate;
        touchedBoxesOfOtherColor.put(coordinate, box);
    }
}
