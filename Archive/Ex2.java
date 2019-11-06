import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.Stack;
/*
initialization and reset
Can't see whether the backtracking is working or not
Worst case analysis: explore every places on the map
*/
public class Ex2 {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private boolean explorerMode; // true for explore, false for backtrack
    private int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    public void controlRobot(IRobot robot) {
        int direction;
        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            robotData = new RobotData(); //reset the data store
            explorerMode = true; // set to Explore-mode at the very beginning
        }
        if (explorerMode)
            direction = exploreControl(robot);
        else
            direction = backtrackControl(robot, robotData);
        pollRun++;
        // if the current location is an unencountered junction then record the data and print out infos
        if (nonwallExits(robot) >= 3 && beenbeforeExits(robot) <= 1){
            robotData.addArrivedHeading(robot.getHeading());
            // robotData.printRecordedData();
        }
        robot.face(direction); // face the robot to chosen direction
        // System.out.println(chooseRandomHeading(directions));
    }

    /**
     * @param robot that you are trying to guide
     * @return the number of non-WALL squares around the robot
     */
    private int nonwallExits(IRobot robot) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL)
                numOfExits++;
        }
        return numOfExits;
    } // end nonwallExits()

    /**
     * @param robot that you are trying to guide
     * @return the number of PASSAGE squares around the robot
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
     * @param robot that you are trying to guide
     * @return the number of BEENBEFORE squares adjacent to the robot
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
     * numOfExits = 1
     * @param robot that you are trying to guide
     * if the robot isn't at the start
     * if the robot is at the start
     * @return the direction that isn't a wall
     * otherwise return behind
     */
    private int deadEnd(IRobot robot) {
        int heading = IRobot.BEHIND;
        if (robot.look(heading) != IRobot.BEENBEFORE) {
            explorerMode = true; // if it's still the very first step the robot took
            if (exitsCanGoExBehind(robot).length == 0)
                heading = IRobot.BEHIND;
            heading = exitsCanGoExBehind(robot)[0];
        }
        return heading;
    }
    /**
     * numOfExits = 2
     * @param robot that you are trying to guide
     * @return the direction that haven't been before if there is one
     * otherwise just choose one that doesn't make the robot to go back on itself
     */
    private int corridor(IRobot robot) {
        int[] exits = exitsCanGoExBehind(robot);
        if (exits[0] == IRobot.BEHIND)
            return exits[1];
        return exits[0];
    }
    /**
     * numOfExits = 3
     * @param robot that you are trying to guide
     * @return PASSAGE exits if exist
     * if theres more than 1 PASSAGE exits, return random one between them
     * otherwise return random direction that doesn’t cause a collision.
     */
    private int junction(IRobot robot) {
        int[] exits = exitsCanGoExBehind(robot);
        if (passageExits(robot) == 0)
            return chooseRandomHeading(exits);
        else {
            int[] psExits = new int[passageExits(robot)];
            int j = 0;
            for (int exit : exits) {
                if (robot.look(exit) == IRobot.PASSAGE)
                    psExits[j++] = exit;
            }
            return chooseRandomHeading(psExits);
        }
    }
    /**
     * numOfExits = 4
     * @param robot that you are trying to guide
     * @return PASSAGE exits if exist
     * if theres more than 1 PASSAGE exits, return random one between them
     * otherwise return random direction that doesn’t cause a collision.
     */
    private int crossroads(IRobot robot) {
        return junction(robot);
    }

    /**
     * @param directionsChooseFrom
     * @return a random direction that was choosed from given array
     * And if there is only one value in the given array, that 1 will be returned
     */
    private int chooseRandomHeading(int[] directionsChooseFrom) {
        if (directionsChooseFrom.length == 1)
            return directionsChooseFrom[0];
        // Generate number from 0-length (exclusive the length)
        Double temp = Math.random()*(directionsChooseFrom.length);
        /* intValue() truncat the digits after decimal
            Value of randno can only be 0, 1, 2 ... length-1 */
        int randno = temp.intValue();
        return directionsChooseFrom[randno];
    }

    /**
     * @return array with all the exits that robot can go
     * (except go behind) that will not cause it crash into the wall
     */
    private int[] exitsCanGoExBehind(IRobot robot) {
        int numOfExitsExBehind;
        if (robot.look(IRobot.BEHIND) == IRobot.WALL)
            numOfExitsExBehind = nonwallExits(robot);
        else
            numOfExitsExBehind = nonwallExits(robot) - 1;
        int[] exits = new int[numOfExitsExBehind];
        for (int i = 0, j = 0; i < directions.length - 1; i++) {
            if (robot.look(directions[i]) != IRobot.WALL)
                exits[j++] = directions[i];
        }
        return exits;
    }

    /**
     * reset the junctionCounter with the reset button
     */
    public void reset() {
        robotData.resetJunctionCounter();
    }

    /**
     * @param robot the robot trying to guide
     * @return the relative heading the robot should go to in exploring mode
     */
    public int exploreControl(IRobot robot) {
        // System.out.println("Explore Mode Started");
        int numOfExits = nonwallExits(robot);
        if (numOfExits == 1){
            explorerMode = false; // start backtracking mode
            return deadEnd(robot);
        }
        else if (numOfExits == 2)
            return corridor(robot);
        else if (numOfExits == 3)
            return junction(robot);
        else
            return crossroads(robot);
    }

    /**
     * @param robot the robot trying to guide
     * @return the relative heading the robot should go to in backtracking mode
     * when encounter no passage exit junctions:
     * exit the junction the opposite way to which it FIRST entered the junction
     */
    public int backtrackControl(IRobot robot, RobotData robotData) {
        // System.out.println("Backtracking Mode Started");
        int numOfExits = nonwallExits(robot);
        if (numOfExits == 1)
            return deadEnd(robot);
        else if (numOfExits == 2)
            return corridor(robot);
        else {
            if (passageExits(robot) != 0){ // there's still passage exits
                explorerMode = true;
                return exploreControl(robot);  // switch back into explorer mode
            }
            else { // robot has been to this junction and there is no passage exit
                robot.setHeading(reverseAbsDirection(robotData.popLastJunctionHeading()));
                return IRobot.AHEAD;
            } // end else
        }// end else for exit >= 3
    } // end backtrackControl()

    /**
     * @param absDirection the absolute direction needed to be reversed
     * @return the reversed absolute direction
     */
    public int reverseAbsDirection(int absDirection) {
        if ((absDirection - IRobot.NORTH) < 2)
            return absDirection + 2;
        else
            return absDirection - 2;
    }
}

/**
 * Class contains needed variables and methods to record unencountered junction
 * and crossroad information
 */
class RobotData {
    Stack<Integer> arrivedDirectionStack;

    /**
     * Constructor
     * reset value of junctionCounter and create new JunctionRecorder array
     */
    public RobotData() {
        arrivedDirectionStack = new Stack<Integer>();
    }

    /** Reset Value of JunctionCounter and data stored in array */
    public void resetJunctionCounter() {
        arrivedDirectionStack = new Stack<Integer>();
    }

    /**
     * Record Junction's data and store them into an JunctionRecorder Array
     * @param heading the heading direction when robot arrived
     */
    public void addArrivedHeading(int heading) {
        arrivedDirectionStack.push(heading);
    }

    public int popLastJunctionHeading() {
        return arrivedDirectionStack.pop();
    }

    /** testing methods */
    public int getLastJunctionHeading() {
        return arrivedDirectionStack.peek();
    }

    public String getLastJunctionHeadingStr() {
        int i = 0;
        String[] headingStr = { "NORTH", "EAST", "SOUTH", "WEST" };
        int[] headings = { IRobot.NORTH, IRobot.EAST, IRobot.SOUTH, IRobot.WEST };
        while (arrivedDirectionStack.peek() != headings[i])
            i++;
        return headingStr[i];
    }
    public void printRecordedData() {
        System.out.println("Heading Recorded: " + getLastJunctionHeadingStr());
    }

}
