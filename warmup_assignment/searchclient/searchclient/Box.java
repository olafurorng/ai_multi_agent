package searchclient;

import java.lang.*;

public class Box {
    private char character;
    private int priority;

    public Box(char character, int priority) {
        this.character = character;
        this.priority = priority;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}