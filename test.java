import java.util.Stack;
import java.util.Objects;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class test {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };

    public void controlRobot(IRobot robot) {
        int direction;
        if ((robot.getRuns() == 0) && (pollRun == 0)) {
            robotData = new RobotData();
        }
        robot.face(exploreControl(robot));
        pollRun++;
        robotData.printJunctionRecords();
    }

    /**
     * reset the data stored in robotdata with the reset button
     */
    public void reset() {
        robotData.resetRobotData();
    }

    /**
     * @param robot the robot trying to guide
     * @return the relative heading the robot should go to in exploring mode
     */
    public int exploreControl(IRobot robot) {
        int numOfExits = numExits(1, robot);
        if (numOfExits == 1) {
            return deadEnd(robot);
        } else if (numOfExits == 2)
            return corridor(robot);
        else if (numOfExits == 3)
            return junction(robot);
        else
            return crossroads(robot);
    }

    /**
     * @param type
     * 0 - number of passage exits
     * 1 - number of all non-wall exits
     * @param robot
     * @return number of different type of exits
     */
    private int numExits(int type, IRobot robot) {
        int result = 0;
        for (int direction : directions) {
            if (type == 0 && robot.look(direction) == IRobot.PASSAGE)
                result++;
            if (type == 1 && robot.look(direction) != IRobot.WALL)
                result++;
        }
        return result;
    }

    /**
     * Find out all the exits that the robot can go to in different types
     *
     * @param type
     * 0 - passage exits
     * 1 - all non-wall exits
     * @param robot that youre trying to guide
     * @return an array of exits in different types that robot can turn to
     */
    private int[] exits(int type, IRobot robot) {
        int[] exits = new int[numExits(type, robot)];
        int i = 0;
        for (int direction : directions) {
            if (type == 0 && robot.look(direction) == IRobot.PASSAGE)
                exits[i++] = direction;
            else if (type == 1 && robot.look(direction) != IRobot.WALL)
                exits[i++] = direction;
        }
        return exits;
    }

    /**
     * numOfExits = 1
     *
     * @param robot that you are trying to guide if the robot isn't at the start if
     *              the robot is at the start
     * @return the direction that isn't a wall otherwise return behind
     */
    private int deadEnd(IRobot robot) {
        int heading = IRobot.BEHIND;
        if (robot.look(heading) != IRobot.BEENBEFORE) {
            // explorerMode = true; // if it's still the very first step the robot took
            heading = exits(1, robot)[0];
        }
        return heading;
    }

    /**
     * numOfExits = 2
     *
     * @param robot that you are trying to guide
     * @return the direction that haven't been before if there is one otherwise just
     *         choose one that doesn't make the robot to go back on itself
     */
    private int corridor(IRobot robot) {
        int[] exits = exits(1, robot);
        if (exits[0] == IRobot.BEHIND)
            return exits[1];
        return exits[0];
    }

    /**
     * numOfExits = 3
     *
     * @param robot that you are trying to guide
     * @return PASSAGE exits if exist if theres more than 1 PASSAGE exits, return
     *         random one between them otherwise return random direction that
     *         doesn’t cause a collision.
     */
    private int junction(IRobot robot) {
        if (!robotData.containsJunctionRecord(robot.getLocation().x, robot.getLocation().y)) {
            robotData.addJunctionRecords(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            return chooseRandomHeading(exits(0, robot));
        }else {
            if (robot.getLocation().x == robotData.getLastUnvisitedRecord().getJuncX() && robot.getLocation().y == robotData.getLastUnvisitedRecord().getJuncY()){
                if (numExits(0, robot) != 0) {
                    return chooseRandomHeading(exits(0, robot));
                }
                robot.setHeading(((robotData.getLastUnvisitedRecord().getArrived() - IRobot.NORTH + 2) % 4) + IRobot.NORTH);
                robotData.transportRecord();
                return IRobot.AHEAD;
            }
            return deadEnd(robot);
        }
    }

    private int crossroads(IRobot robot) {
        return junction(robot);
    }

    /**
     * @param directionsChooseFrom
     * @return a random direction that was choosed from given array And if there is
     *         only one value in the given array, that 1 will be returned
     */
    private int chooseRandomHeading(int[] directionsChooseFrom) {
        Double temp = Math.random() * (directionsChooseFrom.length);
        int randno = temp.intValue();
        return directionsChooseFrom[randno];
    }
}

class JunctionRecorder {
    private int juncX;
    private int juncY;
    private int arrived;

    public JunctionRecorder(int juncX, int juncY, int arrived) {
        this.juncX = juncX;
        this.juncY = juncY;
        this.arrived = arrived;
    }

    public int getJuncX() {
        return this.juncX;
    }

    public void setJuncX(int juncX) {
        this.juncX = juncX;
    }

    public int getJuncY() {
        return this.juncY;
    }

    public void setJuncY(int juncY) {
        this.juncY = juncY;
    }

    public int getArrived() {
        return this.arrived;
    }

    public void setArrived(int arrived) {
        this.arrived = arrived;
    }

    public String getArrivedStr() {
        int i = 0;
        String[] headingStr = { "NORTH", "EAST", "SOUTH", "WEST" };
        int[] headings = { IRobot.NORTH, IRobot.EAST, IRobot.SOUTH, IRobot.WEST };
        while (arrived != headings[i])
            i++;
        return headingStr[i];
    }

    public void printJunctionRecord() {
        System.out.println(" (x=" + juncX + ",y=" + juncY + ")" + " heading " + getArrivedStr());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof JunctionRecorder)) {
            return false;
        }
        JunctionRecorder junc = (JunctionRecorder) o;
        return juncX == junc.juncX && juncY == junc.juncY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(juncX, juncY);
    }
}

class RobotData {
    private Stack<JunctionRecorder> junctionRecords;
    private Stack<JunctionRecorder> visitedJunctionRecords;

    public RobotData() {
        junctionRecords = new Stack<JunctionRecorder>();
        visitedJunctionRecords = new Stack<JunctionRecorder>();
    }

    public void resetRobotData() {
        junctionRecords.clear();
        visitedJunctionRecords.clear();
    }

    public void addJunctionRecords(int juncX, int juncY, int arrived) {
        JunctionRecorder temp = new JunctionRecorder(juncX, juncY, arrived);
        junctionRecords.add(temp);
    }

    public JunctionRecorder getLastUnvisitedRecord() {
        return junctionRecords.peek();
    }

    public void transportRecord() {
        visitedJunctionRecords.push(junctionRecords.pop());
    }

    public void printJunctionRecords() {
        System.out.println("++Junction Stack++");
        junctionRecords.forEach(e -> e.printJunctionRecord());
    }

    public boolean containsJunctionRecord(int juncX, int juncY) {
        JunctionRecorder temp = new JunctionRecorder(juncX, juncY, 0);
        return visitedJunctionRecords.contains(temp) || junctionRecords.contains(temp);
    }
}
