import java.util.Stack;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex2 {
    private static int cnt = 0;
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private RobotData robotData;
    public void controlRobot(IRobot robot) {
        int direction = 0;
        if (cnt == 0){
            robotData = new RobotData();
        }
        int numOfExits = numOfExits(robot);
        if (numOfExits == 1)
            direction = deadEnd(robot);
        else if (numOfExits == 2)
            direction = corridor(robot);
        else
            direction = junction(robot);
        cnt++;

        robot.face(direction);
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
        int j = 0;
        if (passageExits(robot) == numOfExits(robot) - 1){
            robotData.addArrivedHeading(robot.getHeading());
            for (int exit : exits) {
                // add to stack of the heading waiting to explore
                if (exit != IRobot.BEHIND)
                    robotData.addUnexploredHeading(exit);
            }
        }
        robot.setHeading(robotData.peekArrivedHeading());
        if (passageExits(robot) == 0) {
            robot.setHeading(reverseAbsDirection(robotData.popArrivedHeading()));
            return IRobot.AHEAD;
        }
        return robotData.popUnexploredHeading();
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

class RobotData {
    private Stack<Integer> headingUnexplored;
    private Stack<Integer> arrivedHeading;

    public RobotData() {
        headingUnexplored = new Stack<Integer>();
        arrivedHeading = new Stack<Integer>();
    }

    public void resetRobotData() {
        headingUnexplored.clear();
        arrivedHeading.clear();
    }

    public int popArrivedHeading() {
        return arrivedHeading.pop();
    }

    public int peekArrivedHeading() {
        return arrivedHeading.peek();
    }

    public int addArrivedHeading(int heading) {
        return arrivedHeading.push(heading);
    }

    public int popUnexploredHeading() {
        return headingUnexplored.pop();
    }

    public int peekUnexploredHeading() {
        return headingUnexplored.peek();
    }

    public int addUnexploredHeading(int unexploredHeading) {
        return headingUnexplored.push(unexploredHeading);
    }
}
