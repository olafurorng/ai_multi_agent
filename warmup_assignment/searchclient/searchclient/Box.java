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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (character != box.character) return false;
        return priority == box.priority;
    }

    @Override
    public int hashCode() {
        int result = (int) character;
        result = 31 * result + priority;
        return result;
    }
}