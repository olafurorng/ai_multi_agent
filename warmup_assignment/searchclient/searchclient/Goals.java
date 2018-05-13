package searchclient;

import java.lang.*;

public class Goals {
    private char character;
    private Boolean state;
    private int assign;
    private int priority;
    private int numberOfFinished;
    private final ColorHelper.Color color;

    public Goals(char character, Boolean state, int assign) {
        this.character = character;
        this.state = state;
        this.assign = assign;
        this.priority = 1;
        
        String colorAsString = SearchClient.colorsMap.get(Character.toUpperCase(character));
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

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public int getPriority() {
        return priority;
    }

    public void setFinished(int numberOfFinished) {
        this.numberOfFinished = numberOfFinished;
    }

    public int getFinished() {
        return numberOfFinished;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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
    public String toString() {
        return "Goals{" +
                "character=" + character +
                '}';
    }


}