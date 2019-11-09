import java.util.ArrayList;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex3 {
    /** Relative directions for Traversal */
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    /** Counter that increment each time robot face a new direction */
    private int pollRun = 0;
    /**
     * Store the info of how many times the robot has been to this route
     */
    private Remark remarkMap;
    /**
     * True at very beginning of each game, turn into False as soon as it reaches
     * the first junction
     */
    private boolean startMode; // turn off when it get out from the first deadend

    public void controlRobot(IRobot robot) {
        int direction = 0;
        if (pollRun == 0) {
            remarkMap = new Remark();
            startMode = true;
        }
        robot.face(exploreControl(robot));
        if (startMode)
            remarkMap.markCurrentBlock(robot, 2);
        pollRun++;
    }

    /**
     * Interfacing the reset button on maze
     */
    public void reset() {
        remarkMap.printRemarkMap();
        remarkMap.resetRemarkMap();
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
     * Determine whether the current squre is a corridor or not (not corner)
     *
     * @param robot that youre trying to guide
     * @return ture if it is a corridor, false if it is anything else
     */
    private Boolean isRoute(IRobot robot) { // mark physical corridor (not corner)
        if (numExits(3, robot) == 2) {
            for (int i = 0; i < directions.length; i++) {
                if (robot.look(directions[i]) != IRobot.WALL
                        && robot.look(reverseDirection(directions[i])) != IRobot.WALL)
                    return true;
            }
        } // end if
        return false;
    }

    /**
     * @param type  0 - number of no mark exits 1 - number of single mark exits 2 -
     *              number of all exits 3 - number of physical exits
     * @param robot that youre trying to guide
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
        remarkMap.markCurrentBlock(robot);
        remarkMap.markCurrentBlock(robot);
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
            return chooseRandomHeading(nmExits);
        else if (smExits.length != 0)
            return chooseRandomHeading(smExits);
        else
            return chooseRandomHeading(exits);
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
        // turn around when all exits are single mark exits
        if (numExits(1, robot) == numExits(2, robot) && remarkMap.lookRemark(robot, IRobot.BEHIND) < 2)
            return IRobot.BEHIND;

        return hierarchySelectExit(robot);
    }

    /**
     * Reverse a relative or absolute direction e.g. North -> South
     *
     * @param direction needed to be reversed
     * @return reversed direction
     */
    public int reverseDirection(int direction) {
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
        int randno = temp.intValue();
        return directionsChooseFrom[randno];
    }
}

/**
 * Class that record and location and how many times that the robot has been to
 * each route (corridor)
 */
class Remark {
    private ArrayList<Block> remarkMap;

    /**
     * Initialize the arraylist
     */
    public Remark() {
        remarkMap = new ArrayList<Block>();
    }

    /**
     * Clear the current remarkMap list
     */
    public void resetRemarkMap() {
        remarkMap.clear();
    }

    /**
     * Transfer a relative heading to robot current heading to absolute heading
     *
     * @param robot that youre trying to guide
     * @param relativeHeading
     * @return an absolute heading
     */
    public int relativeToAbs(IRobot robot, int relativeHeading) {
        return ((robot.getHeading() - IRobot.NORTH) + (relativeHeading - IRobot.AHEAD)) % 4 + IRobot.NORTH;
    }

    /**
     * @param heading relative one that robot needed to look into
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

    /**
     * Search a blocks information using its location
     *
     * @param x
     * @param y
     * @return index of the block in remarkMap arraylist
     */
    public int searchRemark(int x, int y) {
        for (int i = remarkMap.size() - 1; i >= 0; i--)
            if (remarkMap.get(i).getX() == x && remarkMap.get(i).getY() == y)
                return i;
        // finished the loop and there still isn't any match
        return -1;
    }

    /**
     * Mark the current block if there isn't a record, create one and set the times
     * to 1 if there is a record, then the times++
     *
     * @param robot that youre trying to guide
     */
    public void markCurrentBlock(IRobot robot) {
        int index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        if (index == -1) {
            Block b = new Block(robot.getLocation().x, robot.getLocation().y);
            remarkMap.add(b);
        }
        index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        remarkMap.get(index).addTimes();
    }

    /**
     * Mark the current block if there isn't a record, create one and set the times
     * to num passed in if there is a record, then the just set the times to num
     *
     * @param robot that youre trying to guide
     * @param num   times the robot has been there it needed to set to
     */
    public void markCurrentBlock(IRobot robot, int num) {
        int index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        if (index == -1) {
            Block b = new Block(robot.getLocation().x, robot.getLocation().y);
            remarkMap.add(b);
        }
        remarkMap.get(searchRemark(robot.getLocation().x, robot.getLocation().y)).setTimes(num);
    }

    /**
     * Print all the records that are stored in the remarkMap
     */
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

    public void setTimes(int times) {
        this.times = times;
    }

    public void addTimes() {
        times++;
    }

    /** print the block infos in readable format */
    public void printBlock() {
        System.out.println("[" + getX() + ", " + getY() + "] -> " + getTimes());
    }

}