package searchclient;

import java.lang.*;

public class Box {
    private char character;
    private int assign;

    public Box(char character, int assign) {
        this.character = character;
        this.assign = assign;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public int getAssign() {
        return assign;
    }

    public void setAssign(int assign) {
        this.assign = assign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        if (character != box.character) return false;
        return assign == box.assign;
    }

    @Override
    public int hashCode() {
        int result = (int) character;
        result = 31 * result + assign;
        return result;
    }
}