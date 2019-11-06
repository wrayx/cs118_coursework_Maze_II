import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.Stack;

/**
 * @version with array that contains location of junctions
initialization and reset
Can't see whether the backtracking is working or not
Worst case analysis: explore every places on the map
static or not for exploremode?
 */
public class Ex3_v2 {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private boolean explorerMode; // true for explore, false for backtrack
    private Remark remarkMap;
    private int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private int mapLength = 200;
    private int mapWidth = 200;
    public void controlRobot(IRobot robot) {
        int direction;
        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            robotData = new RobotData(); //reset the data store
            remarkMap = new Remark(mapLength, mapWidth);
            explorerMode = true; // set to Explore-mode at the very beginning
        }
        // do {
            if (explorerMode)
                direction = exploreControl(robot);
            else
                direction = backtrackControl(robot);
        // } while (robot.look(direction) == IRobot.WALL);
        pollRun++;
        // if the current location is an unencountered junction then record the data and print out infos
        if (nonwallExits(robot) >= 3 && beenbeforeExits(robot) <= 1){
            robotData.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            System.out.println("("+robot.getLocation().x+", "+robot.getLocation().y+") added: ");
            robotData.printJunction();
        }
        if (isRoute(robot))
            remarkMap.markCurrentBlock(robot);

        remarkMap.printMarks(robot);
        robot.face(direction); // face the robot to chosen direction
    }

    /**
     * @param robot that you are trying to guide
     * @return the number of non-WALL squares around the robot
     */
    private int nonwallExits(IRobot robot) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) < 3)
                numOfExits++;
        }
        return numOfExits;
    } // end nonwallExits()

    private Boolean isRoute(IRobot robot) {
        // check for physical number of exit
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL)
                numOfExits++;
        }
        if (numOfExits < 3){
            for (int i = 0; i < directions.length; i++) {
                if (robot.look(directions[i]) != IRobot.WALL && robot.look(reverseRelativeDirection(directions[i])) != IRobot.WALL)
                    return true;
            }
        }// end if
        return false;
    }

    /**
     * @param robot that you are trying to guide
     * @return the number of PASSAGE squares around the robot
     */
    private int passageExits(IRobot robot) {
        int numOfPsExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.PASSAGE && remarkMap.lookRemark(robot, direction) < 3)
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
            if (robot.look(direction) == IRobot.BEENBEFORE && remarkMap.lookRemark(robot, direction) < 3)
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
        if (robot.look(IRobot.BEHIND) == IRobot.WALL || remarkMap.lookRemark(robot, IRobot.BEHIND) >= 3)
            numOfExitsExBehind = nonwallExits(robot);
        else
            numOfExitsExBehind = nonwallExits(robot) - 1;
        int[] exits = new int[numOfExitsExBehind];
        for (int i = 0, j = 0; i < directions.length - 1; i++) {
            if (robot.look(directions[i]) != IRobot.WALL && remarkMap.lookRemark(robot, directions[i]) < 3)
                exits[j++] = directions[i];
        }
        return exits;
    }

    /**
     * reset the junctionCounter with the reset button
     */
    public void reset() {
        robotData.resetJunctionCounter();
        remarkMap.resetRemarkMap(mapLength, mapLength);
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
    public int backtrackControl(IRobot robot) {
        // System.out.println("Backtracking Mode Started");
        int numOfExits = nonwallExits(robot);
        if (numOfExits == 1)
            return deadEnd(robot);
        else if (numOfExits == 2)
            return corridor(robot);
        else { // numofexits > 2 junction or crossroads
            if (passageExits(robot) != 0){ // there's passage exits
                explorerMode = true; // switch back into explorer mode
                return exploreControl(robot);
            }
            else { // no passage exit
                if (robotData.searchJunction(robot.getLocation().x, robot.getLocation().y) != -1){
                    robot.setHeading(reverseAbsDirection(robotData.searchJunction(robot.getLocation().x, robot.getLocation().y)));
                    return IRobot.AHEAD;
                } else {
                    explorerMode = true; // switch back into explorer mode
                    return exploreControl(robot);
                }

            }
        }// end else for exit > 2
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

    public int reverseRelativeDirection(int relativeDirection) {
        if ((relativeDirection - IRobot.AHEAD) < 2)
            return relativeDirection + 2;
        else
            return relativeDirection - 2;
    }
}


/**
 * Class contains needed variables and methods to record unencountered junction
 * and crossroad information
 */
class RobotData {
    private static int maxJunctions = 10000; // Max number likely to occur
    private static int junctionCounter; // No. of junctions stored
    // i-th freshly unencountered junction will be stored in the i-th elements of the arrays
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
        junctionsInfo = new JunctionRecorder[maxJunctions];
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

class Remark {
    private int[][] remarkMap;

    public Remark(int length, int width) {
        remarkMap = new int[length][width];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                remarkMap[i][j] = 0;
            }
        }
    }

    public void resetRemarkMap(int mapLength, int mapWidth){
        remarkMap = new int[mapLength][mapWidth];
        for (int i = 0; i < mapLength; i++) {
            for (int j = 0; j < mapWidth; j++) {
                remarkMap[i][j] = 0;
            }
        }
    }

    public void mark(IRobot robot, int relativeHeading) {
        int absHeading = relativeToAbs(robot, relativeHeading);
        System.out.println("Junction marked");
        if (absHeading == IRobot.NORTH)
            remarkMap[robot.getLocation().x][robot.getLocation().y - 1]++;
        else if (absHeading == IRobot.EAST)
            remarkMap[robot.getLocation().x + 1][robot.getLocation().y]++;
        else if (absHeading == IRobot.SOUTH)
            remarkMap[robot.getLocation().x][robot.getLocation().y + 1]++;
        else
            remarkMap[robot.getLocation().x - 1][robot.getLocation().y]++;
    }

    public int relativeToAbs(IRobot robot, int relativeHeading) {
        int[] headings = { IRobot.AHEAD, IRobot.LEFT, IRobot.BEHIND, IRobot.RIGHT };
        int i = 0;
        while (relativeHeading != headings[i]) {
            i++;
        }
        return ((robot.getHeading() - IRobot.NORTH + i) % 4) + IRobot.NORTH;
    }
    /**
     * @param heading
     * @return 0 for never been here before, 1 for been here once, 2 for been here twice
     */
    public int lookRemark(IRobot robot, int relativeHeading) {
        int absHeading = relativeToAbs(robot, relativeHeading);
        if (absHeading == IRobot.NORTH)
            return remarkMap[robot.getLocation().x][robot.getLocation().y - 1];
        else if (absHeading == IRobot.EAST)
            return remarkMap[robot.getLocation().x + 1][robot.getLocation().y];
        else if (absHeading == IRobot.SOUTH)
            return remarkMap[robot.getLocation().x][robot.getLocation().y + 1];
        else
            return remarkMap[robot.getLocation().x - 1][robot.getLocation().y];
    }

    public void markCurrentBlock(IRobot robot) {
        System.out.println("Route marked");
        remarkMap[robot.getLocation().x][robot.getLocation().y]++;
    }

    public void printMarks(IRobot robot) {
        System.out.println("("+robot.getLocation().x+", "+robot.getLocation().y+") - " + remarkMap[robot.getLocation().x][robot.getLocation().y]);
    }
}