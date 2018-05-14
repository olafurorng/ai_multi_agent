package searchclient;

import java.lang.*;

public class Box {
    private char character;
    private int assign;
    private final ColorHelper.Color color;
    private Coordinate coordinate;

    public Box(char character, int assign, Coordinate coordinate) {
        this.character = character;
        this.assign = assign;
        this.coordinate = coordinate;
        
        String colorAsString = SearchClient.colorsMap.get(character);
        if (colorAsString != null) {
            color = ColorHelper.getColorFromString(colorAsString);
        } else {
            color = ColorHelper.Color.BLUE;
        }
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public int getAssign() {
        return assign;
    }

    public void setAssign(int assign) {
        this.assign = assign;
    }

    public ColorHelper.Color getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (character != box.character) return false;
        if (assign != box.assign) return false;
        return color == box.color;
    }

    @Override
    public int hashCode() {
        int result = (int) character;
        result = 31 * result + assign;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}