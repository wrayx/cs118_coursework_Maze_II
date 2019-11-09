import java.util.Stack;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex2 {
    /** Relative directions for Traversal */
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    /** Recording junction and crossroad information */
    private RobotData robotData;
    /** Counter that increment each time robot face a new direction */
    private int pollRun = 0;

    public void controlRobot(IRobot robot) {
        if (pollRun == 0 && robot.getRuns() == 0) {
            robotData = new RobotData();
        }
        robot.face(exploreControl(robot));
        pollRun++;
    }

    /**
     * Interfacing the reset button on maze GUI
     */
    public void reset() {
        robotData.resetRobotData();
    }

    /**
     * Decide which relative heading the robot should turn to
     *
     * @param robot that youre trying to guide
     * @return relatice heading
     */
    private int exploreControl(IRobot robot) {
        if (numOfExits(robot) == 1)
            return deadEnd(robot);
        else if (numOfExits(robot) == 2)
            return corridor(robot);
        else
            return junction(robot);
    }

    /**
     * Count number of all exits
     *
     * @param robot
     * @return int number of all exits
     */
    private int numOfExits(IRobot robot) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL)
                numOfExits++;
        }
        return numOfExits;
    }

    /**
     * Count number of passage exits
     *
     * @param robot
     * @return number of passage exits
     */
    private int passageExits(IRobot robot) {
        int numOfPsExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.PASSAGE)
                numOfPsExits++;
        }
        return numOfPsExits;
    }

    /**
     * Count number of beenbefore exits
     *
     * @param robot
     * @return number of beenbefore exits
     */
    private int beenbeforeExits(IRobot robot) {
        int numOfBeenBefExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.BEENBEFORE)
                numOfBeenBefExits++;
        }
        return numOfBeenBefExits;
    }

    /**
     * Use when there's 1 physical exits Mark the deadends with 2 Turn behind except
     * at the start of the maze
     *
     * @param robot that youre trying to guide
     * @return relative heading the robot should go when in a deadend
     */
    private int deadEnd(IRobot robot) {
        int heading = IRobot.BEHIND;
        if (robot.look(heading) != IRobot.BEENBEFORE) {
            if (exitsCanGo(robot).length == 0)
                heading = IRobot.BEHIND;
            heading = exitsCanGo(robot)[0];
        }
        return heading;
    }

    /**
     * Use when there's 2 physical exits Turn to the exit that isn't behind
     *
     * @param robot
     * @return relative heading the robot should go when in a corridor
     */
    private int corridor(IRobot robot) {
        int[] exits = exitsCanGo(robot);
        if (exits[0] == IRobot.BEHIND)
            return exits[1];
        return exits[0];
    }

    /**
     * Use when there's more than 2 physical exits Always select the lesser marked
     * heading exit
     *
     * @param robot that youre trying to guide
     * @return relative heading turn to when in a junction or crossroad
     */
    private int junction(IRobot robot) {
        int[] exits = exitsCanGo(robot);
        if (passageExits(robot) == numOfExits(robot) - 1) // first time encounter this junction
            robotData.addArrivedHeading(robot.getHeading());
        if (passageExits(robot) != 0) { // pick random passage to go
            int[] psExits = new int[passageExits(robot)];
            int j = 0;
            for (int exit : exits) {
                if (robot.look(exit) == IRobot.PASSAGE)
                    psExits[j++] = exit;
            }
            return chooseRandomHeading(psExits);
        } else {
            robot.setHeading(reverseAbsDirection(robotData.popArrivedHeading()));
            return IRobot.AHEAD;
        }
    }

    /**
     * Reverse an absolute direction e.g. North -> South
     *
     * @param absDirection needed to be reversed
     * @return reversed absolute direction
     */
    public int reverseAbsDirection(int absDirection) {
        if ((absDirection - IRobot.NORTH) < 2)
            return absDirection + 2;
        else
            return absDirection - 2;
    }

    /**
     * Choose a random direction in an array
     *
     * @param directionsChooseFrom array of directions for the method to choose from
     * @return randomly chosen direction
     */
    private int chooseRandomHeading(int[] directionsChooseFrom) {
        if (directionsChooseFrom.length == 1)
            return directionsChooseFrom[0];
        // Generate number from 0-length (exclusive the length)
        Double temp = Math.random() * (directionsChooseFrom.length);
        /*
         * intValue() truncat the digits after decimal Value of randno can only be 0, 1,
         * 2 ... length-1
         */
        int randno = temp.intValue();
        return directionsChooseFrom[randno];
    }

    /**
     * Find out all the exits robot can go to
     *
     * @param robot
     * @return array of the exits robot can go to
     */
    private int[] exitsCanGo(IRobot robot) {
        int[] exits = new int[numOfExits(robot)];
        for (int i = 0, j = 0; i < directions.length; i++) {
            if (robot.look(directions[i]) != IRobot.WALL)
                exits[j++] = directions[i];
        }
        return exits;
    }
}

/** recording of junction and crossroad information */
class RobotData {
    /** stack of the heading that robot arrived at this junction */
    private Stack<Integer> arrivedHeading;

    /** Constructor */
    public RobotData() {
        arrivedHeading = new Stack<Integer>();
    }

    /** Clear the headings in current Heading stack */
    public void resetRobotData() {
        arrivedHeading.clear();
    }

    /**
     * pop off the last stored heading
     *
     * @return last stored heading
     */
    public int popArrivedHeading() {
        return arrivedHeading.pop();
    }

    /**
     * get the last stored heading
     *
     * @return last stored heading
     */
    public int peekArrivedHeading() {
        return arrivedHeading.peek();
    }

    /**
     * add a new arrived heading
     *
     * @param heading arrived heading
     */
    public void addArrivedHeading(int heading) {
        arrivedHeading.push(heading);
    }
}
