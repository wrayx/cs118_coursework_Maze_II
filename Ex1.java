import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex1 {
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
            heading = exitsCanGo(robot)[0];
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
        int[] exits = exitsCanGo(robot);
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
        int[] exits = exitsCanGo(robot);
        int[] psExits = new int[passageExits(robot)];
        int j = 0;
        if (passageExits(robot) == nonwallExits(robot) - 1){
            robotData.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            robotData.printJunction();
        }
        for (int exit : exits) {
            if (robot.look(exit) == IRobot.PASSAGE)
                psExits[j++] = exit;
        }
        return chooseRandomHeading(psExits);
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
     * that will not cause it crash into the wall
     */
    private int[] exitsCanGo(IRobot robot) {
        int[] exits = new int[nonwallExits(robot)];
        for (int i = 0, j = 0; i < directions.length; i++) {
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
        else if (numOfExits == 3 && passageExits(robot) != 0)
            return junction(robot);
        else if (numOfExits == 3) {
            explorerMode = false;
            return exploreControl(robot);  // switch back into explorer mode
        }
        else if (numOfExits == 4 && passageExits(robot) != 0)
            return crossroads(robot);
        else {
            explorerMode = false;
            return exploreControl(robot);  // switch back into explorer mode
        }
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
            if (passageExits(robot) != 0){
                explorerMode = true;
                return exploreControl(robot);  // switch back into explorer mode
            }
            else {
                    // exit the junction the opposite way to which it FIRST entered the junction
                    robot.setHeading(reverseAbsDirection(robotData.searchJunction(robot.getLocation().x, robot.getLocation().y)));
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
    private static int maxJunctions = 10000; // Max number likely to occur
    private static int junctionCounter; // No. of junctions stored
    private JunctionRecorder[] junctionsInfo;

    /**
     * Constructor
     * reset value of junctionCounter and create new JunctionRecorder array
     */
    public RobotData() {
        junctionCounter = 0;
        junctionsInfo = new JunctionRecorder[maxJunctions];
    }

    /** Reset Value of JunctionCounter and data stored in array */
    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    /**
     * Record Junction's data and store them into an JunctionRecorder Array
     * @param juncionX the 'x' axis of the junction
     * @param junctionY the 'y' axit of the junction
     * @param heading the heading direction when robot arrived
     */
    public void recordJunction(int junctionX, int junctionY, int heading) {
        junctionsInfo[junctionCounter] = new JunctionRecorder(junctionX, junctionY, heading);
        junctionCounter++;
    }

    /**
     * Print out the junction details in readable format
     * e.g. Junction 1 (x=3,y=3) heading SOUTH
     */
    public void printJunction() {
        System.out.println("Junction " + junctionCounter +
                         " (x=" + junctionsInfo[junctionCounter - 1].getJuncX() +
                         ",y=" + junctionsInfo[junctionCounter - 1].getJuncY() + ")" +
                         " heading " + junctionsInfo[junctionCounter - 1].getArrivedStr());
    }

    /**
     * @param junctionX x axis of the junction needed to be searched
     * @param junctionY y axis of the junction needed to be searched
     * @return The robot’s heading when it ﬁrst encountered this
     * particular junction
     */
    public int searchJunction(int junctionX, int junctionY) {
        for (JunctionRecorder junctionRecord : junctionsInfo) {
            // if there is a matched (x,y) junction
            if (junctionRecord.getJuncX() == junctionX && junctionRecord.getJuncY() == junctionY)
                return junctionRecord.getArrived();
        }
        // finished the loop and there still isn't any match
        return -1;
    }
}

/**
 * Each JunctionRecoder will store the x- and y-coordinates
 * (to uniquely identify it) and the absolute direction which
 * the robot arrived from when it ﬁrst encountered this junction.
 */
class JunctionRecorder {
    private int juncX; // X-coordinates of the junctions
    private int juncY; // Y-coordinates of the junctions
    private int arrived; // Heading the robotfirst arrived from

    /** Constructor */
    public JunctionRecorder(int juncX, int juncY, int arrived) {
        this.juncX = juncX;
        this.juncY = juncY;
        this.arrived = arrived;
    }
    /**
     * getter for juncX value
     * @return junction's x axis
    */
    public int getJuncX(){
        return juncX;
    }
    /**
     * getter for juncY value
     * @return junction's y axis
    */
    public int getJuncY(){
        return juncY;
    }
    /**
     * getter for arrived value
     * @return the absolute direction which the robot arrived from
     * when it ﬁrst encountered this junction
    */
    public int getArrived(){
        return arrived;
    }
    /**
     * get the absolute direction when then robot first arrived in this junction
     * @return arrived absolute direction in string format e. g 'NORTH'
     */
    public String getArrivedStr(){
        int i = 0;
        String[] headingStr = { "NORTH", "EAST", "SOUTH", "WEST" };
        int[] headings = { IRobot.NORTH, IRobot.EAST, IRobot.SOUTH, IRobot.WEST };
        while (arrived != headings[i])
            i++;
        return headingStr[i];
    }
}