import java.util.ArrayList;
import uk.ac.warwick.dcs.maze.logic.IRobot;
// arraylist version
public class test {
    private int pollRun = 0;
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private Remark remarkMap;
    private boolean startMode; // turn off when it get out from the first deadend
    public void controlRobot(IRobot robot) {
        int direction = 0;
        if (pollRun == 0) {
            remarkMap = new Remark();
            startMode = true;
        }
        robot.face(exploreControl(robot));
        // testing
        remarkMap.printMarks(robot);
        if (startMode)
            remarkMap.markCurrentBlock(robot, 2);
        pollRun++;
    }

    public void reset() {
        remarkMap.printRemarkMap();
        remarkMap.resetRemarkMap();
    }

    private int exploreControl(IRobot robot) {
        if (numOfPhyExits(robot) == 1)
            return deadEnd(robot);
        else if (numOfPhyExits(robot) == 2)
            return corridor(robot);
        else
            return junction(robot);
    }

    private int hierarchySelectExit(IRobot robot) {
        int[] nmExits = noMarkExits(robot);
        int[] smExits = singleMarkExits(robot);
        int[] exits = exits(robot);
        if (nmExits.length != 0)
            return chooseRandomHeading(nmExits);
        else if (smExits.length != 0)
            return chooseRandomHeading(smExits);
        else
            return chooseRandomHeading(exits);
    }

    private Boolean isRoute(IRobot robot) { // mark physical corridor (not corner)
        if (numOfPhyExits(robot) == 2){
            for (int i = 0; i < directions.length; i++) {
                if (robot.look(directions[i]) != IRobot.WALL && robot.look(reverseDirection(directions[i])) != IRobot.WALL)
                    return true;
            }
        }// end if
        return false;
    }

    private int numOfPhyExits(IRobot robot) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL)
                numOfExits++;
        }
        return numOfExits;
    } // end nonwallExits()

    private int numOfExits(IRobot robot) {
        int numOfExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) < 2)
                numOfExits++;
        }
        return numOfExits;
    } // end nonwallExits()

    private int[] exits(IRobot robot) {
        int[] exits = new int[numOfExits(robot)];
        int i = 0;
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) < 2)
                exits[i++] = direction;
        }
        return exits;
    }

    private int numOfNoMarkExits(IRobot robot) {
        int num = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.PASSAGE)
                num++;
        }
        return num;
    }

    private int[] noMarkExits(IRobot robot) {
        int[] nmExits = new int[numOfNoMarkExits(robot)];
        int i = 0;
        for (int direction : directions){
            if (robot.look(direction) == IRobot.PASSAGE)
                nmExits[i++] = direction;
        }
        return nmExits;
    }

    private int numOfSingleMarkExits(IRobot robot) {
        int num = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) == 1)
                num++;
        }
        return num;
    }

    private int[] singleMarkExits(IRobot robot) {
        int[] smExits = new int[numOfSingleMarkExits(robot)];
        int i = 0;
        for (int direction : directions){
            if (robot.look(direction) != IRobot.WALL && remarkMap.lookRemark(robot, direction) == 1)
                smExits[i++] = direction;
        }
        return smExits;
    }

    private int deadEnd(IRobot robot) {
        remarkMap.markCurrentBlock(robot, 2);
        if (robot.look(IRobot.BEHIND) != IRobot.BEENBEFORE) {
            int[] exits = exits(robot);
            return exits[0];
        }
        return IRobot.BEHIND;
    }

    private int corridor(IRobot robot) {
        if (isRoute(robot))
            remarkMap.markCurrentBlock(robot);
        // exception for at the beginning of the graph
        if (numOfExits(robot) == 0) {
            for (int direction : directions) {
                if (direction != IRobot.BEHIND && robot.look(direction) != IRobot.WALL)
                    return direction;
            }
        }
        int[] exits = exits(robot);
        for (int exit : exits) {
            if (exit != IRobot.BEHIND)
                return exit;
        }
        return exits[0];
    }

    private int junction(IRobot robot) {
        startMode = false;
        // turn around when all exits are single mark exits
        if (numOfSingleMarkExits(robot) == numOfExits(robot) && remarkMap.lookRemark(robot, IRobot.BEHIND) < 2)
            return IRobot.BEHIND; // numOfSingleMarkExits(robot) == numOfExits(robot) && robot.look(IRobot.BEHIND) != IRobot.WALL &&

        return hierarchySelectExit(robot);
    }

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

    private int chooseRandomHeading(int[] directionsChooseFrom) {
        if (directionsChooseFrom.length == 1)
            return directionsChooseFrom[0];
        // Generate number from 0-length (exclusive the length)
        Double temp = Math.random()*(directionsChooseFrom.length);
        int randno = temp.intValue();
        return directionsChooseFrom[randno];
    }
}

class Remark {
    private ArrayList<Block> remarkMap;

    /**
     * Initialize table
     * @param ylength
     * @param xlength
     */
    public Remark() {
        remarkMap = new ArrayList<Block>();
    }

    public void resetRemarkMap(){
        remarkMap.clear();
    }

    public int relativeToAbs(IRobot robot, int relativeHeading) {
        return ( (robot.getHeading() - IRobot.NORTH) + (relativeHeading - IRobot.AHEAD) ) % 4 + IRobot.NORTH;
    }
    /**
     * @param heading
     * @return 0 for never been here before, 1 for been here once, 2 for been here twice
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
        if (index == -1){
            Block b = new Block(robot.getLocation().x, robot.getLocation().y);
            remarkMap.add(b);
        }
        index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        remarkMap.get(index).addTimes();
    }

    public void markCurrentBlock(IRobot robot, int num) {
        int index = searchRemark(robot.getLocation().x, robot.getLocation().y);
        if (index == -1){
            Block b = new Block(robot.getLocation().x, robot.getLocation().y);
            remarkMap.add(b);
        }
        remarkMap.get(searchRemark(robot.getLocation().x, robot.getLocation().y)).setTimes(num);
    }

    public void printMarks(IRobot robot) {
        if (searchRemark(robot.getLocation().x, robot.getLocation().y) != -1)
            System.out.println("[++"+robot.getLocation().y+", "+robot.getLocation().x+"++] - " + remarkMap.get(searchRemark(robot.getLocation().x, robot.getLocation().y)).getTimes());
        else
        System.out.println("Recording");

    }

    public void printRemarkMap() {
        System.out.println("-- Marked Places --");
        remarkMap.forEach(e -> e.printBlock());
    }
}

class Block {
    private int x;
    private int y;
    private int times;

    public Block(int x, int y) {
        this.x = x;
        this.y = y;
        this.times = 0;
    }

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

    public void printBlock() {
        System.out.println("["+getX()+", "+getY()+"] -> " + getTimes());
    }

}

