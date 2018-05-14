package searchclient;


import java.util.HashMap;
import java.util.Set;

/**
 * Created by olafurorn on 5/12/18.
 */
public class AgentCommunications {

    private HashMap<Coordinate, Box> touchedBoxesOfOtherColor = new HashMap<Coordinate, Box>();
    private Coordinate originalCoordiante;
    private int pullCounter;

    public enum Type {
        TOUCHING_BOX_OF_OTHER_COLOR
    }

    public AgentCommunications() {

    }

    public Box getTouchedBox() {

        return touchedBoxesOfOtherColor.get(originalCoordiante);
    }

    public void setPullCounter(int pullCounter) {

        this.pullCounter = pullCounter;
    }
    public int getPullCounter() {

        return pullCounter;
    }

    public void removeBox() {
        touchedBoxesOfOtherColor.remove(originalCoordiante);
    }

    public Coordinate getOriginalCoordinate() {
        return originalCoordiante;
   }
    public void onBoxWithOtherColorTouched(Coordinate coordinate, Box box) {
        this.originalCoordiante = coordinate;
        this.pullCounter = 10;
        touchedBoxesOfOtherColor.put(coordinate, box);
    }
}
