import uk.ac.warwick.dcs.maze.logic.IRobot;

/*
initialization and reset
Can't see whether the backtracking is working or not
Worst case analysis: explore every places on the map
static or not for exploremode?
*/
public class Ex3_v3 {
    private int pollRun = 0; // Incremented after each pass
    private Remark remarkMap;
    private int mapLength = 35;
    private int mapWidth = 35;
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    public void controlRobot(IRobot robot) {
        int direction;
        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            remarkMap = new Remark(mapLength, mapWidth);
        }
        int num = 0;
        int been = 0;
        for (int heading : directions) {
            if (robot.look(heading) != IRobot.WALL)
                num++;
            if (robot.look(heading) == IRobot.BEENBEFORE)
                been++;
        }
        if (num > 2 && num == been ){
            remarkMap.markCurrentBlock(robot);
            remarkMap.markCurrentBlock(robot);
        }
        direction = exploreControl(robot);
        pollRun++;
        remarkMap.printMarks(robot);
        robot.face(direction); // face the robot to chosen direction
    }

    /**
     * @param robot that you are trying to guide
     * @return the number of non-WALL squares around the robot
     */
    private int numOfExits(IRobot robot) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL && remarkMap.checkCanGo(robot, direction))
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
            if (remarkMap.lookRemark(robot, direction) == 0)
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
            if (remarkMap.lookRemark(robot, direction) == 1)
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
     */
    private int deadEnd(IRobot robot) {
        int heading = IRobot.BEHIND;
        if (robot.look(heading) != IRobot.BEENBEFORE)
            heading = exitsCanGo(robot)[0];
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
        int[] exits = exitsCanGo(robot).clone();
        int[] psExits = new int[passageExits(robot)];
        int j = 0;
        if (passageExits(robot) == 0)
            return chooseRandomHeading(exits);
        else {
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
     * that will not cause it crash into the wall
     */
    private int[] exitsCanGo(IRobot robot) {
        int[] exits = new int[numOfExits(robot)];
        for (int i = 0, j = 0; i < directions.length; i++) {
            if (robot.look(directions[i]) != IRobot.WALL && remarkMap.checkCanGo(robot, directions[i]))
                exits[j++] = directions[i];
        }
        return exits;
    }

    /**
     * reset the junctionCounter with the reset button
     */
    public void reset() {
        remarkMap.resetRemarkMap(mapLength, mapLength);
    }

    /**
     * @param robot the robot trying to guide
     * @return the relative heading the robot should go to in exploring mode
     */
    public int exploreControl(IRobot robot) {
        int numOfExits = numOfExits(robot);
        if (numOfExits == 1){
            return deadEnd(robot);
        }
        else if (numOfExits == 2){
            if (isRoute(robot)){
                remarkMap.markCurrentBlock(robot);
            }
            return corridor(robot);
        }
        else {
            return junction(robot);
        }
    }

    private Boolean isRoute(IRobot robot) { // mark physical corridor (not corner)
        // check for physical number of exits
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL)
                numOfExits++;
        }
        if (numOfExits == 2){
            for (int i = 0; i < directions.length; i++) {
                if (robot.look(directions[i]) != IRobot.WALL && robot.look(reverseDirection(directions[i])) != IRobot.WALL)
                    return true;
            }
        }// end if
        return false;
    }
    /**
     * @param direction the direction needed to be reversed
     * @return the reversed direction
     */
    public int reverseDirection(int direction) {
        int standard;
        if (direction - IRobot.NORTH < 4)
            standard = IRobot.NORTH;
        else
            standard = IRobot.AHEAD;

        if ((direction - standard) < 2)
            return direction + 2;
        else
            return direction - 2;
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

    public Boolean checkCanGo(IRobot robot, int direction) {
        int absHeading = relativeToAbs(robot, direction);
        int temp;
        if (absHeading == IRobot.NORTH)
            temp = remarkMap[robot.getLocation().x][robot.getLocation().y - 1];
        else if (absHeading == IRobot.EAST)
            temp = remarkMap[robot.getLocation().x + 1][robot.getLocation().y];
        else if (absHeading == IRobot.SOUTH)
            temp = remarkMap[robot.getLocation().x][robot.getLocation().y + 1];
        else
            temp = remarkMap[robot.getLocation().x - 1][robot.getLocation().y];

        if (temp < 3)
            return true;
        else
            return false;
    }
}

