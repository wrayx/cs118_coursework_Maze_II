import java.util.ArrayList;
import uk.ac.warwick.dcs.maze.logic.IRobot;
// arraylist version
public class Ex3 {
    private static int cnt = 0;
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private Remark remarkMap;
    private int ylength = 32;
    private int xlength = 32;
    private RobotData robotData;
    public void controlRobot(IRobot robot) {
        int direction = 0;
        if (cnt == 0) {
            remarkMap = new Remark(ylength, xlength);
            robotData = new RobotData();
        }

        robot.face(exploreControl(robot));

        // testing
        remarkMap.printMarks(robot);
        remarkMap.printArray();

        cnt++;
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
        for (int a : nmExits){
            System.out.print(a);
            System.out.print(' ');
        }
        System.out.println();
        int[] smExits = singleMarkExits(robot);
        for (int a : smExits){
            System.out.print(a);
            System.out.print(' ');
        }
        System.out.println();
        int[] exits = exits(robot);
        for (int a : exits){
            System.out.print(a);
            System.out.print(' ');
        }
        System.out.println();
        if (nmExits.length != 0)
            return chooseRandomHeading(nmExits);
        else if (smExits.length != 0)
            return chooseRandomHeading(smExits);
        else
            return chooseRandomHeading(exits);
    }

    public void reset() {
        remarkMap.resetRemarkMap(ylength, xlength);
        robotData.resetRobotData();
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

    private Boolean isDeadend(IRobot robot) { // mark physical deadend
        if (numOfPhyExits(robot) == 1)
            return true;
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
        remarkMap.markCurrentBlock(robot);
        remarkMap.markCurrentBlock(robot);
        if (robot.look(IRobot.BEHIND) != IRobot.BEENBEFORE) {
            int[] exits = exits(robot);
            return exits[0];
        }
        return IRobot.BEHIND;
    }

    private int corridor(IRobot robot) {
        if (isRoute(robot))
            remarkMap.markCurrentBlock(robot);
        int[] exits = exits(robot);
        for (int exit : exits) {
            if (exit != IRobot.BEHIND)
                return exit;
        }
        return exits[0];
    }

    private int junction(IRobot robot) {
        // first time been to this junction
        if (numOfNoMarkExits(robot) == numOfPhyExits(robot) - 1)
            robotData.addJunctionRecord(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
        else if (numOfSingleMarkExits(robot) == numOfExits(robot) && remarkMap.lookRemark(robot, IRobot.BEHIND) < 2)
            return IRobot.BEHIND; // numOfSingleMarkExits(robot) == numOfExits(robot) && robot.look(IRobot.BEHIND) != IRobot.WALL &&
        // last it will come to this junction
        else if (numOfNoMarkExits(robot) == 0 && robotData.searchJunctionRecord(robot.getLocation().x, robot.getLocation().y) != -1) {
            robot.setHeading(reverseDirection(robotData.getLastJunctionArrivedHeading(robot.getLocation().x, robot.getLocation().y)));
            robotData.rmJunctionRecord(robot.getLocation().x, robot.getLocation().y);
            // remarkMap.markCurrentBlock(robot);
            // remarkMap.markCurrentBlock(robot);
            if (remarkMap.lookRemark(robot, IRobot.AHEAD) < 2)
                return IRobot.AHEAD;
        }

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

class JunctionRecorder {
    private int juncX;
    private int juncY;
    private int arrived;

    public JunctionRecorder(int juncX, int juncY, int arrived) {
        this.juncX = juncX;
        this.juncY = juncY;
        this.arrived = arrived;
    }

    public int getArrived() {
        return arrived;
    }
    public int getJuncX() {
        return juncX;
    }
    public int getJuncY() {
        return juncY;
    }
}



class RobotData {
    private ArrayList<JunctionRecorder> robotdata;

    public RobotData() {
        robotdata = new ArrayList<JunctionRecorder>();
    }

    public void resetRobotData() {
        robotdata.clear();
    }

    /**
     * searchJunctionRecord
     * @param juncX
     * @param juncY
     * @return index of seached junction, if its not in array return -1
     */
    public int searchJunctionRecord(int juncX, int juncY) {
        for (int i = 0; i < robotdata.size(); i++) {
            if (robotdata.get(i).getJuncX() == juncX && robotdata.get(i).getJuncY() == juncY)
                return i;
        }
        return -1;
    }

    public JunctionRecorder rmJunctionRecord(int index) {
        return robotdata.remove(index);
    }

    public JunctionRecorder rmJunctionRecord(int juncX, int juncY) {
        return robotdata.remove(searchJunctionRecord(juncX, juncY));
    }

    public int getLastJunctionArrivedHeading(int index) {
        return robotdata.get(index).getArrived();
    }

    public int getLastJunctionArrivedHeading(int juncX, int juncY) {
        return robotdata.get(searchJunctionRecord(juncX, juncY)).getArrived();
    }

    public void addJunctionRecord(int juncX, int juncY, int arrived) {
        JunctionRecorder temp = new JunctionRecorder(juncX, juncY, arrived);
        robotdata.add(temp);
    }
}


class Remark {
    private int[][] remarkMap;

    /**
     * Initialize table
     * @param ylength
     * @param xlength
     */
    public Remark(int ylength, int xlength) {
        remarkMap = new int[ylength][ylength];
    }

    public void resetRemarkMap(int ylength, int xlength){
        remarkMap = new int[ylength][xlength];
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
        if (absHeading == IRobot.NORTH)
            return remarkMap[robot.getLocation().y - 1][robot.getLocation().x];
        else if (absHeading == IRobot.EAST)
            return remarkMap[robot.getLocation().y][robot.getLocation().x + 1];
        else if (absHeading == IRobot.SOUTH)
            return remarkMap[robot.getLocation().y + 1][robot.getLocation().x];
        else
            return remarkMap[robot.getLocation().y][robot.getLocation().x - 1];
    }

    public void markCurrentBlock(IRobot robot) {
        System.out.println("Route marked");
        remarkMap[robot.getLocation().y][robot.getLocation().x] = remarkMap[robot.getLocation().y][robot.getLocation().x] + 1;
    }

    public void printMarks(IRobot robot) {
        System.out.println("["+robot.getLocation().y+", "+robot.getLocation().x+"] - " + remarkMap[robot.getLocation().y][robot.getLocation().x]);
    }

    public void printArray() {
        for (int[] x : remarkMap){
            for (int y : x) {
                System.out.print(y + " ");
            }
        System.out.println();
        }
    }
}

