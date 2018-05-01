package searchclient;

/**
 * Created by olafurorn on 5/1/18.
 */
public class WallBuilder {

    /**
     * This is a type of Relaxed problem, more specific: Adding edges relaxed problem.
     * Walls or edges are added to decrease the size of the level, but has rules to
     * prevent that the level won't become unsolvable
     *
     * Cons:
     * 		- Length of the solution can be longer, e.g. in SAsoko3_48.lvl
     * 		- Level which are not squared, have many free cells, and therefore can
     * 		  become unsolvable.
     */
    public static void buildWallsSafely(Node state) {
        System.err.println("----------------------------------------");
        System.err.println("Walls before adding walls safely");
        System.err.println(state.toString());

        // we will start adding walls in free cells which are outside the level
        // lets start with adding from the left side

        for (int i = 0; i < Node.MAX_ROW; i++) {
            for (int j = 0; j < Node.MAX_COL; j++) {
                if (Node.WALLS.contains(new Coordinate(i, j))) {
                    break;
                } else {
                    Node.WALLS.add(new Coordinate(i,j));
                }
            }
        }
        // then add from the right side
        for (int i = 0; i < Node.MAX_ROW; i++) {
            for (int j = Node.MAX_COL - 1; j >= 0 ; j--) {
                if (Node.WALLS.contains(new Coordinate(i,j))) {
                    break;
                } else {
                    Node.WALLS.add(new Coordinate(i,j));
                }
            }
        }

        // count how many free cells compared to number of agents+boxes
        int numberOfNewBoxesThisRound = 1000; // initialized as some number above 0
        int iterations = 0;

        while (numberOfNewBoxesThisRound > 0) {
            numberOfNewBoxesThisRound = 0;
            for (int i = 0; i < Node.MAX_ROW; i++) {
                for (int j = 0; j < Node.MAX_COL; j++) {
                    if (state.cellIsFreeAndNoGoalOrAgent(i,j)) {
                        // now the cell is free, so we check if there is no
                        // goal, box or agent around that cell
                        boolean topFree = state.cellIsFreeOfGoalBoxAndAgent(i - 1, j);
                        boolean topRightFree = state.cellIsFreeOfGoalBoxAndAgent(i - 1, j + 1);
                        boolean rightFree = state.cellIsFreeOfGoalBoxAndAgent(i, j + 1);
                        boolean bottomRighFree = state.cellIsFreeOfGoalBoxAndAgent(i + 1, j + 1);
                        boolean bottomFree = state.cellIsFreeOfGoalBoxAndAgent(i + 1, j);
                        boolean leftBottomFree = state.cellIsFreeOfGoalBoxAndAgent(i + 1, j - 1);
                        boolean leftFree = state.cellIsFreeOfGoalBoxAndAgent(i, j - 1);
                        boolean leftTopFree = state.cellIsFreeOfGoalBoxAndAgent(i - 1, j - 1);

                        if (topFree && topRightFree && rightFree && bottomRighFree && bottomFree && leftBottomFree && leftFree && leftTopFree) {
                            // now there is not goal, box or agent near this cell

                            boolean wallTop = Node.WALLS.contains(new Coordinate(i-1, j));
                            boolean wallTopRight = Node.WALLS.contains(new Coordinate(i-1, j+1));
                            boolean wallRight = Node.WALLS.contains(new Coordinate(i, j+1));
                            boolean wallBottomRight = Node.WALLS.contains(new Coordinate(i + 1, j+1));
                            boolean wallBottom = Node.WALLS.contains(new Coordinate(i + 1, j));
                            boolean wallLeftBottom = Node.WALLS.contains(new Coordinate(i + 1, j - 1));
                            boolean wallLeft = Node.WALLS.contains(new Coordinate(i,j - 1));
                            boolean wallLeftTop = Node.WALLS.contains(new Coordinate(i-1, j-1));

							/*
								Wall is said to be touching a cell, if the wall is on the left, right, top or bottom.
								Wall is said to be nearby a cell, if the wall is on the top-right, right-bottom, bottom-left or left-top
							 */
                            int numberOfWallsTouching = 0;

                            if (wallTop) numberOfWallsTouching++;
                            if (wallRight) numberOfWallsTouching++;
                            if (wallBottom) numberOfWallsTouching++;
                            if (wallLeft) numberOfWallsTouching++;

                            Coordinate coordinate = new Coordinate(i,j);


                            // 4 or 3 WALLS TOUCHING
                            if (numberOfWallsTouching == 4 || numberOfWallsTouching == 3) {
                                // we can safely add a wall
                                Node.WALLS.add(coordinate);
                                numberOfNewBoxesThisRound++;
                            }

                            // 2 WALLS TOUCHING
                            if (numberOfWallsTouching == 2) {
                                // we exclude if the cell is part of a "tunnel", i.e.
                                // - if the touching walls are opposite, i.e. walltop and wallbottom or wallright and wallleft
                                // - if the opposite nearby cell to the two touching walls, is a wall

                                // WALLS are top and left
                                if (wallTop && wallLeft && !wallBottomRight) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }

                                // walls are top and right
                                if (wallTop && wallRight && !wallLeftBottom) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }

                                // walls are right and bottom
                                if (wallRight && wallBottom && !wallLeftTop) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }

                                // walls are bottom and left
                                if (wallBottom && wallLeft && !wallTopRight) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }
                            }

                            // 1 WALL TOUCHING
                            if (numberOfWallsTouching == 1) {
                                // we exclude if the cell is part of a "tunnel", i.e.
                                // - if opposite nearby cell to the one touching wall, is a wall
                                if (wallTop && !wallBottomRight && !wallLeftBottom) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }

                                if (wallRight && !wallLeftTop && !wallLeftBottom) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }

                                if (wallBottom && !wallTopRight && !wallLeftTop) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }

                                if (wallLeft && !wallBottomRight && !wallTopRight) {
                                    Node.WALLS.add(coordinate);
                                    numberOfNewBoxesThisRound++;
                                }
                            }

                        }
                    }
                }
            }

            iterations++;
        }
        System.err.println("----------------------------------------");
        System.err.println("Adding walls as possible is done, iterations: " + iterations);
        System.err.println(state.toString());
        System.err.println("----------------------------------------");
    }


    /**
     * This is a type of Relaxed problem, more specific: Adding edges relaxed problem.
     * Walls or edges are added to decrease the size of the level and to make it easier
     * to detect a surrounded goal.
     *
     *
     * If a cell is free and has 3 touching walls and 1 goal touching it is filled with
     * a wall.
     *
     * Example:
     *   ++++++              ++++++
     *   + a  A        ==>   ++a  A
     *   ++++++              ++++++
     *
     * What we benefit from adding a wall in the example is then it is easier to detect
     * if goal 'a' is surrounded. By detecting it is surrounded we can put high priority
     * finishing that goal to prevent we are locking the goal with e.g. a box B. Here is
     * example of possiblity of locking a goal 'a' with box B:
     *
     *   +++++++++++++++++
     *   ++a    bB 0    A+
     *   ++++++++++++ ++++
     *   +++++++++++++++++
     *
     *   The reason for why we don't add an wall in a cell if it has 3 touching walls and
     *   no goal touching is because then we can exclude possible necessary space to store
     *   boxes temporately. E.g. the example above would become un-solve able, see:
     *
     *   +++++++++++++++++
     *   ++a    bB 0    A+
     *   +++++++++++++++++
     *   +++++++++++++++++
     */
    public static void buildWallInEmptyCellWith3TouchingWallAnd1TouchingGoal(Node state) {
        System.err.println("----------------------------------------");
        System.err.println("Walls before adding wall in empty surrounded cells");
        System.err.println(state.toString());

        // we will start adding walls in free cells which are outside the level
        // lets start with adding from the left side

        for (int i = 0; i < Node.MAX_ROW; i++) {
            for (int j = 0; j < Node.MAX_COL; j++) {
                if (Node.WALLS.contains(new Coordinate(i, j))) {
                    break;
                } else {
                    Node.WALLS.add(new Coordinate(i,j));
                }
            }
        }
        // then add from the right side
        for (int i = 0; i < Node.MAX_ROW; i++) {
            for (int j = Node.MAX_COL - 1; j >= 0 ; j--) {
                if (Node.WALLS.contains(new Coordinate(i,j))) {
                    break;
                } else {
                    Node.WALLS.add(new Coordinate(i,j));
                }
            }
        }

        // count how many free cells compared to number of agents+boxes
        int numberOfNewBoxesThisRound = 1000; // initialized as some number above 0
        int iterations = 0;

        while (numberOfNewBoxesThisRound > 0) {
            numberOfNewBoxesThisRound = 0;
            for (int i = 0; i < Node.MAX_ROW; i++) {
                for (int j = 0; j < Node.MAX_COL; j++) {
                    if (state.cellIsFreeAndNoGoalOrAgent(i,j)) {
                        // now the cell is free

                        boolean wallTop = Node.WALLS.contains(new Coordinate(i-1, j));
                        boolean wallRight = Node.WALLS.contains(new Coordinate(i, j+1));
                        boolean wallBottom = Node.WALLS.contains(new Coordinate(i + 1, j));
                        boolean wallLeft = Node.WALLS.contains(new Coordinate(i,j - 1));

                        boolean goalTop = Node.GOALS.containsKey(new Coordinate(i-1, j));
                        boolean goalRight = Node.GOALS.containsKey(new Coordinate(i, j+1));
                        boolean goalBottom = Node.GOALS.containsKey(new Coordinate(i + 1, j));
                        boolean goalLeft = Node.GOALS.containsKey(new Coordinate(i,j - 1));

							/*
								Wall is said to be touching a cell, if the wall is on the left, right, top or bottom.
								Wall is said to be nearby a cell, if the wall is on the top-right, right-bottom, bottom-left or left-top
							 */
                        int numberOfWallsTouching = 0;
                        int numberOfGoalTouching = 0;

                        if (wallTop) numberOfWallsTouching++;
                        if (wallRight) numberOfWallsTouching++;
                        if (wallBottom) numberOfWallsTouching++;
                        if (wallLeft) numberOfWallsTouching++;

                        if (goalTop) numberOfGoalTouching++;
                        if (goalRight) numberOfGoalTouching++;
                        if (goalBottom) numberOfGoalTouching++;
                        if (goalLeft) numberOfGoalTouching++;

                        Coordinate coordinate = new Coordinate(i,j);


                        // 4 or 3 WALLS TOUCHING
                        if (numberOfWallsTouching == 3 & numberOfGoalTouching == 1) {
                            // we can safely add a wall
                            Node.WALLS.add(coordinate);
                            numberOfNewBoxesThisRound++;
                        }
                    }
                }
            }

            iterations++;
        }


        System.err.println("----------------------------------------");
        System.err.println("Adding walls as possible is done, iterations: " + iterations);
        System.err.println(state.toString());
        System.err.println("----------------------------------------");
    }
}
