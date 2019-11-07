import java.util.Stack;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class GrandFinale {
    private int pollRun = 0;
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private RobotData robotData;
    public void controlRobot(IRobot robot) {
        if (pollRun == 0 && robot.getRuns() == 0){
            robotData = new RobotData();
        }
        if (robot.getRuns() != 0) {}
        robot.face(exploreControl(robot));
        pollRun++;
        robotData.printRobotData();
    }

    private int exploreControl(IRobot robot) {
        if (numOfExits(robot) == 1)
            return deadEnd(robot);
        else if (numOfExits(robot) == 2)
            return corridor(robot);
        else
            return junction(robot);
    }

    public void reset() {
        robotData.resetRobotData();
    }

    private int numOfExits(IRobot robot) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL)
                numOfExits++;
        }
        return numOfExits;
    } // end nonwallExits()

    private int passageExits(IRobot robot) {
        int numOfPsExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.PASSAGE)
                numOfPsExits++;
        }
        return numOfPsExits;
    }

    private int beenbeforeExits(IRobot robot) {
        int numOfBeenBefExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.BEENBEFORE)
                numOfBeenBefExits++;
        }
        return numOfBeenBefExits;
    }

    private int deadEnd(IRobot robot) {
        int heading = IRobot.BEHIND;
        if (robot.look(heading) != IRobot.BEENBEFORE) {
            if (exitsCanGo(robot).length == 0)
                heading = IRobot.BEHIND;
            heading = exitsCanGo(robot)[0];
        }
        return heading;
    }

    private int corridor(IRobot robot) {
        int[] exits = exitsCanGo(robot);
        for (int exit : exits) {
            if (exit != IRobot.BEENBEFORE)
                return exit;
        }
        return exits[0];
    }

    private int junction(IRobot robot) {
        int[] exits = exitsCanGo(robot);
        if (passageExits(robot) == numOfExits(robot) - 1) // first time encounter this junction
            robotData.addRobotData(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
        if (passageExits(robot) != 0) { // pick random passage to go
            int[] psExits = new int[passageExits(robot)];
            int j = 0;
            for (int exit : exits) {
                if (robot.look(exit) == IRobot.PASSAGE)
                    psExits[j++] = exit;
            }
            return chooseRandomHeading(psExits);
        }
        else {
            robot.setHeading(reverseAbsDirection(robotData.popRobotData().getArrivedHeading()));
            return IRobot.AHEAD;
        }
    }

    public int reverseAbsDirection(int absDirection) {
        if ((absDirection - IRobot.NORTH) < 2)
            return absDirection + 2;
        else
            return absDirection - 2;
    }

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

    private int[] exitsCanGo(IRobot robot) {
        int[] exits = new int[numOfExits(robot)];
        for (int i = 0, j = 0; i < directions.length; i++) {
            if (robot.look(directions[i]) != IRobot.WALL)
                exits[j++] = directions[i];
        }
        return exits;
    }
}

class JunctionRecord {
    private int arrivedHeading;
    private int juncX;
    private int juncY;

    public JunctionRecord (int juncX, int juncY, int arrivedHeading) {
        this.juncX = juncX;
        this.juncY = juncY;
        this.arrivedHeading = arrivedHeading;
    }

    public int getArrivedHeading() {
        return arrivedHeading;
    }

    public int getJuncX() {
        return juncX;
    }

    public int getJuncY() {
        return juncY;
    }

    /**
     * Print out the junction details in readable format
     * e.g. Junction 1 (x=3,y=3) heading SOUTH
     */
    public void printJunction() {
        System.out.println("(x=" + getJuncX() + ",y=" + getJuncY() + ")" + " heading " + getArrivedStr());
    }

    /**
     * get the absolute direction when then robot first arrived in this junction
     * @return arrived absolute direction in string format e. g 'NORTH'
     */
    public String getArrivedStr(){
        int i = 0;
        String[] headingStr = { "NORTH", "EAST", "SOUTH", "WEST" };
        int[] headings = { IRobot.NORTH, IRobot.EAST, IRobot.SOUTH, IRobot.WEST };
        while (arrivedHeading != headings[i])
            i++;
        return headingStr[i];
    }
}

class RobotData {
    private Stack<JunctionRecord> robotData;
    private static JunctionRecord[] preRobotDataArr;

    public RobotData() {
        robotData = new Stack<JunctionRecord>();
    }

    public void resetRobotData() {
        robotData.clear();
    }

    public JunctionRecord popRobotData() {
        return robotData.pop();
    }

    public JunctionRecord peekRobotData() {
        return robotData.peek();
    }

    public void addRobotData(int juncX, int juncY, int arrivedHeading) {
        JunctionRecord junc = new JunctionRecord(juncX, juncY, arrivedHeading);
        robotData.push(junc);
    }

    public void robotDataToArray() {
        preRobotDataArr = new JunctionRecord[robotData.size()];
        preRobotDataArr = robotData.toArray(preRobotDataArr);
    }

    public void printRobotData() {
        System.out.println("-RobotData-");
        robotData.forEach(data -> data.printJunction());
    }
}
