package searchclient;

/**
 * Created by olafurorn on 5/13/18.
 */
public class TunnelDetection {

    public static boolean isGoalInEndOfTunnel(Coordinate goalCoordinate) {
        int i = goalCoordinate.getX();
        int j = goalCoordinate.getY();
        boolean wallTop = Node.WALLS.contains(new Coordinate(i-1, j));
        boolean wallRight = Node.WALLS.contains(new Coordinate(i, j+1));
        boolean wallBottom = Node.WALLS.contains(new Coordinate(i + 1, j));
        boolean wallLeft = Node.WALLS.contains(new Coordinate(i,j - 1));

        int numberOfWallsTouching = 0;

        if (wallTop) numberOfWallsTouching++;
        if (wallRight) numberOfWallsTouching++;
        if (wallBottom) numberOfWallsTouching++;
        if (wallLeft) numberOfWallsTouching++;

        return numberOfWallsTouching == 3;
    }

    public static boolean isInMiddleOfTunnel(Coordinate coordinate) {
        int i = coordinate.getX();
        int j = coordinate.getY();
        boolean wallTop = Node.WALLS.contains(new Coordinate(i-1, j));
        boolean wallRight = Node.WALLS.contains(new Coordinate(i, j+1));
        boolean wallBottom = Node.WALLS.contains(new Coordinate(i + 1, j));
        boolean wallLeft = Node.WALLS.contains(new Coordinate(i,j - 1));

        int numberOfWallsTouching = 0;

        if (wallTop) numberOfWallsTouching++;
        if (wallRight) numberOfWallsTouching++;
        if (wallBottom) numberOfWallsTouching++;
        if (wallLeft) numberOfWallsTouching++;


        if (numberOfWallsTouching == 3) {
            throw new IllegalStateException("The tunnel can't be closed in both ends");
        }
        if (numberOfWallsTouching == 4) {
            throw new IllegalStateException("The tunnel can't have 4 walls");
        }

        if (numberOfWallsTouching == 2) {
            // still in a tunnel
            return true;
        }

        if (numberOfWallsTouching == 1 || numberOfWallsTouching == 0) {
            // the tunnel has ended
            return false;
        }


        throw new IllegalStateException("numberOfWallsTouching: " + numberOfWallsTouching + " is illegal number");
    }



    public static Coordinate findNextCoordinateInTunnel(Coordinate thisCoordinate, Coordinate lastCoordinate) {
        int i = thisCoordinate.getX();
        int j = thisCoordinate.getY();
        boolean wallTop = Node.WALLS.contains(new Coordinate(i-1, j));
        boolean wallRight = Node.WALLS.contains(new Coordinate(i, j+1));
        boolean wallBottom = Node.WALLS.contains(new Coordinate(i + 1, j));
        boolean wallLeft = Node.WALLS.contains(new Coordinate(i,j - 1));

        System.err.println("Finding next coordinate ..");


        if (!wallTop) {
            Coordinate newCoordinate = new Coordinate(i-1, j);
            if (lastCoordinate == null || !newCoordinate.equals(lastCoordinate)) {
                return newCoordinate;
            }
        }

        if (!wallRight) {
            Coordinate newCoordinate = new Coordinate(i, j+1);
            if (lastCoordinate == null || !newCoordinate.equals(lastCoordinate)) {
                return newCoordinate;
            }
        }

        if (!wallBottom){
            Coordinate newCoordinate = new Coordinate(i + 1, j);
            if (lastCoordinate == null || !newCoordinate.equals(lastCoordinate)) {
                return newCoordinate;
            }
        }

        if (!wallLeft) {
            Coordinate newCoordinate = new Coordinate(i,j - 1);
            if (lastCoordinate == null || !newCoordinate.equals(lastCoordinate)) {
                return newCoordinate;
            }
        }


        throw new IllegalStateException("Last coordinate doesn't have an open end");
    }
}
