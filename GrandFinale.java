import java.util.Stack;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class GrandFinale {
    /** Relative directions for Traversal */
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    /**
     * Store the info of how many times the robot has been to this route
     */
    private Remark remarkMap;
    /** Recording junction and crossroad information */
    private RobotData robotData;
    /** Counter that increment each time robot face a new direction */
    private int pollRun = 0;
    private static boolean startMode; // turn off when it get out from the first deadend

    public void controlRobot(IRobot robot) {
        if (pollRun == 0 && robot.getRuns() == 0) {
            robotData = new RobotData();
            remarkMap = new Remark();
        }
        if (pollRun == 0) {
            startMode = true;
        }
        robot.face(exploreControl(robot));
        if (startMode)
            remarkMap.markCurrentBlock(robot, 2);
        pollRun++;
        robotData.printJunctionsInfo();
    }

    /**
     * Decide which relative heading the robot should turn to
     *
     * @param robot that youre trying to guide
     * @return relatice heading
     */
    private int exploreControl(IRobot robot) {
        if (numExits(3, robot) == 1)
            return deadEnd(robot);
        else if (numExits(3, robot) == 2)
            return corridor(robot);
        else
            return junction(robot);
    }

    /**
     * Interfacing the reset button on maze GUI
     */
    public void reset() {
        remarkMap.resetRemarkMap();
        robotData.junctionsInfoToArray();
        robotData.resetJunctionsInfo();
        robotData.printJunctionArrRecord();
    }

    /**
     * Determine whether the current squre is a corridor or not (not corner)
     *
     * @param robot that youre trying to guide
     * @return ture if it is a corridor, false if it is anything else
     */
    private Boolean isRoute(IRobot robot) { // mark physical corridor (not corner)
        if (numExits(3, robot) == 2) {
            for (int i = 0; i < directions.length; i++) {
                if (robot.look(directions[i]) != IRobot.WALL
                        && robot.look(MazeUtil.reverseDirection(directions[i])) != IRobot.WALL)
                    return true;
            }
        }
        return false;
    }

    /**
     * @param type  0 - number of no mark exits 1 - number of single mark exits 2 -
     *              number of all exits 3 - number of physical exits
     * @param robot
     * @return number of different type of exits
     */
    private int numExits(int type, IRobot robot) {
        int[] result = new int[4];
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.PASSAGE)
                result[0]++;
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) == 1)
                result[1]++;
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) < 2)
                result[2]++;
            if (robot.look(direction) != IRobot.WALL)
                result[3]++;
        }
        return result[type];
    }

    /**
     * Find out all the exits that the robot can go to in different types
     *
     * @param type  0 - number of no mark exits 1 - number of single mark exits 2 -
     *              number of all exits 3 - number of physical exits
     * @param robot that youre trying to guide
     * @return an array of exits in different types that robot can turn to
     */
    private int[] exits(int type, IRobot robot) {
        int[] exits = new int[numExits(type, robot)];
        int i = 0;
        for (int direction : directions) {
            if (type == 1 && robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) == 1)
                exits[i++] = direction;
            else if (type == 0 && robot.look(direction) == IRobot.PASSAGE)
                exits[i++] = direction;
            else if (type == 2 && robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) < 2)
                exits[i++] = direction;
            else if (type == 3 && robot.look(direction) != IRobot.WALL)
                exits[i++] = direction;
        }
        return exits;
    }

    /**
     * Use when there's 1 physical exits Mark the deadends with 2 Turn behind except
     * at the start of the maze
     *
     * @param robot that youre trying to guide
     * @return relative heading the robot should go when in a deadend
     */
    private int deadEnd(IRobot robot) {
        remarkMap.markCurrentBlock(robot, 2);
        if (robot.look(IRobot.BEHIND) != IRobot.BEENBEFORE) {
            int[] exits = exits(2, robot);
            return exits[0];
        }
        return IRobot.BEHIND;
    }

    /**
     * Use when there's 2 physical exits Turn to the exit that isn't behind
     *
     * @param robot
     * @return relative heading the robot should go when in a corridor
     */
    private int corridor(IRobot robot) {
        if (isRoute(robot))
            remarkMap.markCurrentBlock(robot);
        // exception for at the beginning of the graph
        if (numExits(2, robot) == 0) {
            for (int direction : directions) {
                if (direction != IRobot.BEHIND && robot.look(direction) != IRobot.WALL)
                    return direction;
            }
        }
        int[] exits = exits(2, robot);
        for (int exit : exits) {
            if (exit != IRobot.BEHIND)
                return exit;
        }
        return exits[0];
    }

    /**
     * Select the lesser marked route to go
     *
     * @param robot that youre trying to guide
     * @return the heading that will lead to less marked exit
     */
    private int hierarchySelectExit(IRobot robot) {
        int[] nmExits = exits(0, robot);
        int[] smExits = exits(1, robot);
        int[] exits = exits(2, robot);
        if (nmExits.length != 0)
            return MazeUtil.chooseRandomHeading(nmExits);
        else if (smExits.length != 0)
            return MazeUtil.chooseRandomHeading(smExits);
        else
            return MazeUtil.chooseRandomHeading(exits);
    }

    /**
     * Use when there's more than 2 physical exits Always select the lesser marked
     * heading exit
     *
     * @param robot that youre trying to guide
     * @return relative heading turn to when in a junction or crossroad
     */
    private int junction(IRobot robot) {
        startMode = false;
        int[] exits = exits(2, robot);
        int index = robotData.searchJunctionArr(robot.getLocation().x, robot.getLocation().y);
        if (numExits(0, robot) == numExits(3, robot) - 1) // first time encounter this junction
            robotData.addJunctionsInfo(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
        // turn around when all exits are single mark exits
        if (numExits(1, robot) == numExits(2, robot) && remarkMap.lookRemark(robot, IRobot.BEHIND) < 2)
            return IRobot.BEHIND;
        else if (numExits(0, robot) != 0 && index != -1 && !robotData.getJunctionArrRecordUsed(index)){
            robot.setHeading(robotData.getJunctionArrRecordHeading(index));
            robotData.setJunctionArrRecordUsed(index);
            if (robot.look(IRobot.AHEAD) != IRobot.WALL){
                robotData.peekJunctionsInfo().setLeaveHeading(robotData.getJunctionArrRecordHeading(index));
                return IRobot.AHEAD;
            }
            else {
                robotData.rmJunctionArrRecord(index);
                return junction(robot);
            }
        }
        else {
            int heading = 0;
            heading = hierarchySelectExit(robot);
            robotData.peekJunctionsInfo().setLeaveHeading(MazeUtil.relativeToAbs(robot, heading));
            if (numExits(0, robot) == 0)
                robotData.popJunctionsInfo();
            return heading;
        }
    }
}

/** Utils for maze environment */
class MazeUtil {
    public static int chooseRandomHeading(int[] directionsChooseFrom) {
        if (directionsChooseFrom.length == 1)
            return directionsChooseFrom[0];
        // Generate number from 0-length (exclusive the length)
        Double temp = Math.random() * (directionsChooseFrom.length);
        int randno = temp.intValue();
        return directionsChooseFrom[randno];
    }

    public static int reverseDirection(int direction) {
        int standard;
        if (direction - IRobot.NORTH < 4 && direction - IRobot.NORTH >= 0)
            standard = IRobot.NORTH;
        else
            standard = IRobot.AHEAD;

        if ((direction - standard) < 2)
            return direction + 2;
        else
            return direction - 2;
    }

    public static int relativeToAbs(IRobot robot, int relativeHeading) {
        return ((robot.getHeading() - IRobot.NORTH) + (relativeHeading - IRobot.AHEAD)) % 4 + IRobot.NORTH;
    }
}

class JunctionRecord {
    private int leaveHeading;
    private int juncX;
    private int juncY;
    private boolean used;

    public JunctionRecord(int juncX, int juncY, int leaveHeading) {
        this.juncX = juncX;
        this.juncY = juncY;
        this.leaveHeading = leaveHeading;
        this.used = false;
    }

    public int getLeaveHeading() {
        return leaveHeading;
    }

    public int getJuncX() {
        return juncX;
    }

    public int getJuncY() {
        return juncY;
    }

    public boolean getUsed() {
        return used;
    }

    public void setLeaveHeading(int heading) {
        leaveHeading = heading;
    }

    public void setUsed() {
        used = true;
    }

    public void setUsed(boolean a) {
        used = a;
    }

    /**
     * Print out the junction details in readable format e.g. Junction 1 (x=3,y=3)
     * heading SOUTH
     */
    public void printJunction() {
        System.out.println("(x=" + getJuncX() + ",y=" + getJuncY() + ")" + " heading " + getArrivedStr());
    }

    /**
     * get the absolute direction when then robot first arrived in this junction
     *
     * @return arrived absolute direction in string format e. g 'NORTH'
     */
    public String getArrivedStr() {
        int i = 0;
        String[] headingStr = { "NORTH", "EAST", "SOUTH", "WEST" };
        int[] headings = { IRobot.NORTH, IRobot.EAST, IRobot.SOUTH, IRobot.WEST };
        while (leaveHeading != headings[i])
            i++;
        return headingStr[i];
    }

    @Override
    public boolean equals(Object o) {

        if (o == this)
            return true;
        if (!(o instanceof JunctionRecord)) {
            return false;
        }
        JunctionRecord junc = (JunctionRecord) o;
        return juncX == junc.juncX && juncY == junc.juncY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(juncX, juncY);
    }
}

/** recording of junction and crossroad information */
class RobotData {
    /** Stack of junctions info in junctioninfo format */
    private Stack<JunctionRecord> junctionsInfo;
    private static ArrayList<JunctionRecord> preJunctionsInfoArr;

    public RobotData() {
        junctionsInfo = new Stack<JunctionRecord>();
        preJunctionsInfoArr = new ArrayList<JunctionRecord>();
    }

    public void resetJunctionsInfo() {
        junctionsInfo.clear();
    }

    /**
     * pop off the last stored junctionsInfo
     *
     * @return last stored junctionsInfo
     */
    public JunctionRecord popJunctionsInfo() {
        return junctionsInfo.pop();
    }

    /**
     * get the last stored junctionsInfo
     *
     * @return last stored junctionsInfo
     */
    public JunctionRecord peekJunctionsInfo() {
        return junctionsInfo.peek();
    }

    /**
     * add a new junctionsinfo
     *
     * @param juncX
     * @param juncY
     * @param leaveHeading
     */
    public void addJunctionsInfo(int juncX, int juncY, int leaveHeading) {
        JunctionRecord junc = new JunctionRecord(juncX, juncY, leaveHeading);
        junctionsInfo.push(junc);
    }

    public void printJunctionsInfo() {
        System.out.println("-junctionsInfo-");
        junctionsInfo.forEach(data -> data.printJunction());
    }

    public void junctionsInfoToArray() {
        preJunctionsInfoArr.clear();
        preJunctionsInfoArr.forEach(e -> e.setUsed(false));
        junctionsInfo.forEach(e -> preJunctionsInfoArr.add(e));
    }

    public int searchJunctionArr(int junctionX, int junctionY) {
        JunctionRecord temp = new JunctionRecord(junctionX, junctionY, 0);
        return preJunctionsInfoArr.indexOf(temp);
    }

    public void rmJunctionArrRecord(int i) {
        preJunctionsInfoArr.remove(i);
    }

    public int getJunctionArrRecordHeading(int i) {
        return preJunctionsInfoArr.get(i).getLeaveHeading();
    }

    public void setJunctionArrRecordUsed(int i) {
        preJunctionsInfoArr.get(i).setUsed();
    }

    public void setJunctionArrRecordUsed(int i, boolean a) {
        preJunctionsInfoArr.get(i).setUsed(a);
    }

    public boolean getJunctionArrRecordUsed(int i) {
        return preJunctionsInfoArr.get(i).getUsed();
    }

    public void printJunctionArrRecord() {
        System.out.println("-preJunctionsInfoArr-");
        preJunctionsInfoArr.forEach(e -> e.printJunction());
    }
}

class Remark {
    private ArrayList<Block> remarkMap;

    /**
     * Initialize table
     *
     * @param ylength
     * @param xlength
     */
    public Remark() {
        remarkMap = new ArrayList<Block>();
    }

    public void resetRemarkMap() {
        remarkMap.clear();
    }

    public int relativeToAbs(IRobot robot, int relativeHeading) {
        return ((robot.getHeading() - IRobot.NORTH) + (relativeHeading - IRobot.AHEAD)) % 4 + IRobot.NORTH;
    }

    /**
     * @param heading
     * @return 0 for never been here before, 1 for been here once, 2 for been here
     *         twice
     */
    public int lookRemark(IRobot robot, int relativeHeading) {
        int absHeading = relativeToAbs(robot, relativeHeading);
        int index;
        if (absHeading == IRobot.NORTH)
            index = searchRemark(robot.getLocation().x, robot.getLocation().y - 1);
        else if (absHeading == IRobot.EAST)
            index = searchRemark(robot.getLocation().x + 1, robot.getLocation().y);
        else if (absHeading == IRobot.SOUTH)
            index = searchRemark(robot.getLocation().x, robot.getLocation().y + 1);
        else
            index = searchRemark(robot.getLocation().x - 1, robot.getLocation().y);

        if (index == -1) // that place that haven't been stored in the arraylist
            return 0;
        return remarkMap.get(index).getTimes();
    }

    public int searchRemark(int x, int y) {
        for (int i = remarkMap.size() - 1; i >= 0; i--)
            if (remarkMap.get(i).getX() == x && remarkMap.get(i).getY() == y)
                return i;
        // finished the loop and there still isn't any match
        return -1;
    }

    public void markCurrentBlock(IRobot robot) {
        int index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        if (index == -1) {
            Block b = new Block(robot.getLocation().x, robot.getLocation().y);
            remarkMap.add(b);
        }
        index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        remarkMap.get(index).addTimes();
    }

    public void markCurrentBlock(IRobot robot, int num) {
        int index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        if (index == -1) {
            Block b = new Block(robot.getLocation().x, robot.getLocation().y);
            remarkMap.add(b);
        }
        remarkMap.get(searchRemark(robot.getLocation().x, robot.getLocation().y)).setTimes(num);
    }

    public void printMarks(IRobot robot) {
        if (searchRemark(robot.getLocation().x, robot.getLocation().y) != -1)
            System.out.println("[++" + robot.getLocation().y + ", " + robot.getLocation().x + "++] - "
                    + remarkMap.get(searchRemark(robot.getLocation().x, robot.getLocation().y)).getTimes());
        else
            System.out.println("Invalide for Marking");

    }

    public void printRemarkMap() {
        System.out.println("-- Marked Places --");
        remarkMap.forEach(e -> e.printBlock());
    }
}

/**
 * Infomation about the blocks
 */
class Block {
    /**
     * location of the block
     */
    private int x;
    private int y;
    /**
     * How many times the robot has been to this block
     */
    private int times;

    /**
     * Constructors
     *
     * @param x
     * @param y
     */
    public Block(int x, int y) {
        this.x = x;
        this.y = y;
        this.times = 0;
    }

    /** getters and setters */
    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getTimes() {
        return this.times;
    }

    public void addTimes() {
        times++;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    /** print the block infos in readable format */
    public void printBlock() {
        System.out.println("[" + getX() + ", " + getY() + "] -> " + getTimes());
    }
}