package searchclient;

import java.lang.*;

public class Goals {
    private char character;
    private Boolean state;

    public Goals(char character, Boolean state) {
        this.character = character;
        this.state = state;
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
}