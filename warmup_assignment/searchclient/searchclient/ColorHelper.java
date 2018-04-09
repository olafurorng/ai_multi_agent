package searchclient;

import java.lang.*;

public class ColorHelper {
    public static enum Type {
        Move, Push, Pull
    }

    public static enum Color {
        BLUE,
        RED,
        GREEN,
        CYAN,
        MAGENTA,
        ORANGE,
        PINK,
        YELLOW
    }

    public static Color getColorFromString(String colorString) {
        if (colorString.equals("blue")) {
            return Color.BLUE;
        }
        if (colorString.equals("red")) {
            return Color.RED;
        }
        if (colorString.equals("green")) {
            return Color.GREEN;
        }
        if (colorString.equals("cyan")) {
            return Color.CYAN;
        }
        if (colorString.equals("magenta")) {
            return Color.MAGENTA;
        }
        if (colorString.equals("orange")) {
            return Color.ORANGE;
        }
        if (colorString.equals("pink")) {
            return Color.PINK;
        }
        if (colorString.equals("yellow")) {
            return Color.YELLOW;
        }

        throw new IllegalArgumentException("Color " + colorString + " unhandled");
    }
}