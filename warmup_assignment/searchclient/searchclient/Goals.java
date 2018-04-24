package searchclient;

import java.lang.*;

public class Goals {
    private char character;
    private Boolean state;
    private int priority;

    public Goals(char character, Boolean state, int priority) {
        this.character = character;
        this.state = state;
        this.priority = priority;
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

    public void setPriority(int priority) {
        this.priority = priority;
    }
}