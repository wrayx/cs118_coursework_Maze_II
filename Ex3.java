import uk.ac.warwick.dcs.maze.logic.IRobot;
/*
initialization and reset
Can't see whether the backtracking is working or not
Worst case analysis: explore every places on the map
*/
public class Ex3 {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private static boolean explorerMode; // true for explore, false for backtrack
    private Remark remarkMap;
    private int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private int[] absHeading = { IRobot.NORTH, IRobot.EAST, IRobot.SOUTH, IRobot.WEST };
    public void controlRobot(IRobot robot) {
        int direction;
        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            robotData = new RobotData(); //reset the data store
            explorerMode = true; // set to Explore-mode at the very beginning
            remarkMap = new Remark(100, 100);
        }
        if (explorerMode)
            direction = exploreControl(robot, remarkMap);
        else
            direction = backtrackControl(robot, robotData, remarkMap);
        pollRun++;
        // if the current location is an unencountered junction then record the data and print out infos
        if (nonwallExits(robot, remarkMap) >= 3 && beenbeforeExits(robot, remarkMap) <= 1){
            robotData.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            robotData.printJunction();
        }
        robot.face(direction); // face the robot to chosen direction
        // System.out.println(chooseRandomHeading(directions));
    }

    /**
     * @param robot that you are trying to guide
     * @return the number of non-WALL squares around the robot
     */
    private int nonwallExits(IRobot robot, Remark remarkMap) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) < 3)
                numOfExits++;
        }
        return numOfExits;
    } // end nonwallExits()

    /**
     * @param robot that you are trying to guide
     * @return the number of PASSAGE squares around the robot
     */
    private int passageExits(IRobot robot, Remark remarkMap) {
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
    private int beenbeforeExits(IRobot robot, Remark remarkMap) {
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
    private int deadEnd(IRobot robot, Remark remarkMap) {
        int heading = IRobot.BEHIND;
        if (robot.look(heading) != IRobot.BEENBEFORE) {
            explorerMode = true; // if it's still the very first step the robot took
            if (exitsCanGoExBehind(robot, remarkMap).length == 0)
                heading = IRobot.BEHIND;
            heading = exitsCanGoExBehind(robot, remarkMap)[0];
        }
        return heading;
    }
    /**
     * numOfExits = 2
     * @param robot that you are trying to guide
     * @return the direction that haven't been before if there is one
     * otherwise just choose one that doesn't make the robot to go back on itself
     */
    private int corridor(IRobot robot, Remark remarkMap) {
        int[] exits = exitsCanGoExBehind(robot, remarkMap);
        return exits[0];
    }
    /**
     * numOfExits = 3
     * @param robot that you are trying to guide
     * @return PASSAGE exits if exist
     * if theres more than 1 PASSAGE exits, return random one between them
     * otherwise return random direction that doesn’t cause a collision.
     */
    private int junction(IRobot robot, Remark remarkMap) {
        int[] exits = exitsCanGoExBehind(robot, remarkMap);
        if (passageExits(robot, remarkMap) == 0)
            return chooseRandomHeading(exits);
        else {
            int[] psExits = new int[passageExits(robot, remarkMap)];
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
    private int crossroads(IRobot robot, Remark remarkMap) {
        return junction(robot, remarkMap);
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
    private int[] exitsCanGoExBehind(IRobot robot, Remark remarkMap) {
        int numOfExitsExBehind;
        if (robot.look(IRobot.BEHIND) == IRobot.WALL || remarkMap.lookRemark(robot, IRobot.BEHIND) >= 3)
            numOfExitsExBehind = nonwallExits(robot, remarkMap);
        else
            numOfExitsExBehind = nonwallExits(robot, remarkMap) - 1;
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
    }

    /**
     * @param robot the robot trying to guide
     * @return the relative heading the robot should go to in exploring mode
     */
    public int exploreControl(IRobot robot, Remark remarkMap) {
        // System.out.println("Explore Mode Started");
        int numOfExits = nonwallExits(robot, remarkMap);
        if (numOfExits == 1){
            explorerMode = false; // start backtracking mode
            return deadEnd(robot, remarkMap);
        }
        else if (numOfExits == 2)
            return corridor(robot, remarkMap);
        else if (numOfExits == 3)
            return junction(robot, remarkMap);
        else
            return crossroads(robot, remarkMap);
    }

    /**
     * @param robot the robot trying to guide
     * @return the relative heading the robot should go to in backtracking mode
     * when encounter no passage exit junctions:
     * exit the junction the opposite way to which it FIRST entered the junction
     */
    public int backtrackControl(IRobot robot, RobotData robotData, Remark remarkMap) {
        // System.out.println("Backtracking Mode Started");
        int numOfExits = nonwallExits(robot, remarkMap);
        if (numOfExits == 1)
            return deadEnd(robot, remarkMap);
        else if (numOfExits == 2)
            return corridor(robot, remarkMap);
        else {
            if (passageExits(robot, remarkMap) != 0){
                explorerMode = true;
                return exploreControl(robot, remarkMap);  // switch back into explorer mode
            }
            else {
                int preDirection = robotData.searchJunction(robot.getLocation().x, robot.getLocation().y);
                if (preDirection != -1){
                    // exit the junction the opposite way to which it FIRST entered the junction
                    robot.setHeading(reverseAbsDirection(preDirection));
                    remarkMap.mark(robot, IRobot.AHEAD);
                    return IRobot.AHEAD;
                } else { // the first time robot encounter this junction
                    explorerMode = true;
                    return junction(robot, remarkMap);
                } // end if (preDirection != -1)
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
    // todo: change to modular
    /**
     * Convert absolute heading to relative heading
     * @param robot
     * @param heading an absolute heading needed to convert to relative one
     * @return a relative heading (to the robot)
     */
    private int absToRelative(IRobot robot, int heading) {
        if (heading == robot.getHeading())
             return IRobot.AHEAD;
        else if (heading == robot.getHeading() - 1 || heading == robot.getHeading() + 3)
             return IRobot.LEFT;
        else if (heading == robot.getHeading() + 1 || heading == robot.getHeading() - 3)
             return IRobot.RIGHT;
        else
             return IRobot.BEHIND;
   }

    private int relativeToAbs(IRobot robot, int relativeHeading) {
        if (robot.getHeading() == IRobot.NORTH) {
            if (relativeHeading == IRobot.AHEAD)
                return robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                return robot.getHeading()+1;
            else if (relativeHeading == IRobot.BEHIND)
                return robot.getHeading()+2;
            else
                return robot.getHeading()+3;
        }else if (robot.getHeading() == IRobot.EAST) {
            if (relativeHeading == IRobot.AHEAD)
                return robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                return robot.getHeading()+1;
            else if (relativeHeading == IRobot.BEHIND)
                return robot.getHeading()+2;
            else
                return robot.getHeading()-1;
        }else if (robot.getHeading() == IRobot.SOUTH) {
            if (relativeHeading == IRobot.AHEAD)
                return robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                return robot.getHeading()+1;
            else if (relativeHeading == IRobot.BEHIND)
                return robot.getHeading()-2;
            else
                return robot.getHeading()-1;
        }else {
            if (relativeHeading == IRobot.AHEAD)
                return robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                return robot.getHeading()-3;
            else if (relativeHeading == IRobot.BEHIND)
                return robot.getHeading()-2;
            else
                return robot.getHeading()-1;
        }
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

    public void mark(IRobot robot, int relativeHeading) {
        int absHeading = relativeToAbs(robot, relativeHeading);
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
        int absHeading;
        if (robot.getHeading() == IRobot.NORTH) {
            if (relativeHeading == IRobot.AHEAD)
                absHeading = robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                absHeading = robot.getHeading()+1;
            else if (relativeHeading == IRobot.BEHIND)
                absHeading = robot.getHeading()+2;
            else
                absHeading = robot.getHeading()+3;
        }else if (robot.getHeading() == IRobot.EAST) {
            if (relativeHeading == IRobot.AHEAD)
                absHeading = robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                absHeading = robot.getHeading()+1;
            else if (relativeHeading == IRobot.BEHIND)
                absHeading = robot.getHeading()+2;
            else
                absHeading = robot.getHeading()-1;
        }else if (robot.getHeading() == IRobot.SOUTH) {
            if (relativeHeading == IRobot.AHEAD)
                absHeading = robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                absHeading = robot.getHeading()+1;
            else if (relativeHeading == IRobot.BEHIND)
                absHeading = robot.getHeading()-2;
            else
                absHeading = robot.getHeading()-1;
        }else {
            if (relativeHeading == IRobot.AHEAD)
                absHeading = robot.getHeading();
            else if (relativeHeading == IRobot.LEFT)
                absHeading = robot.getHeading()-3;
            else if (relativeHeading == IRobot.BEHIND)
                absHeading = robot.getHeading()-2;
            else
                absHeading = robot.getHeading()-1;
        }
        return absHeading;
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
}