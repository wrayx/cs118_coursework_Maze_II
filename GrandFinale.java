import java.util.Stack;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class GrandFinale {
    private int pollRun = 0;
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private Remark remarkMap;
    private int ylength = 32;
    private int xlength = 32;
    private static boolean startMode; // turn off when it get out from the first deadend
    private RobotData robotData;
    public void controlRobot(IRobot robot) {
        if (pollRun == 0 && robot.getRuns() == 0){
            robotData = new RobotData();
            remarkMap = new Remark(ylength, xlength);
        }
        if (pollRun == 0) {
            startMode = true;
        }
        robot.face(exploreControl(robot));
        // testing
        remarkMap.printMarks(robot);
        remarkMap.printArray();
        if (startMode)
            remarkMap.markCurrentBlock(robot, 2);
        pollRun++;
        robotData.printJunctionsInfo();
    }

    private int exploreControl(IRobot robot) {
        if (numOfPhyExits(robot) == 1)
            return deadEnd(robot);
        else if (numOfPhyExits(robot) == 2)
            return corridor(robot);
        else {
            if (robot.getRuns() == 0)
                return junction(robot);
            else
                return learnedJunction(robot);
        }
    }

    public void reset() {
        remarkMap.resetRemarkMap(ylength, xlength);
        robotData.junctionsInfoToArray();
        robotData.resetJunctionsInfo();
        robotData.printJunctionArrRecord();
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

    private int numOfnoMarkExits(IRobot robot) {
        int numOfPsExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int direction : directions) {
            if (robot.look(direction) == IRobot.PASSAGE)
                numOfPsExits++;
        }
        return numOfPsExits;
    }

    private int[] noMarkExits(IRobot robot) {
        int[] nmExits = new int[numOfnoMarkExits(robot)];
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
        int[] exits = exits(robot);
        if (numOfnoMarkExits(robot) == numOfPhyExits(robot) - 1) // first time encounter this junction
            robotData.addJunctionsInfo(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
        // turn around when all exits are single mark exits
        if (numOfSingleMarkExits(robot) == numOfExits(robot) && remarkMap.lookRemark(robot, IRobot.BEHIND) < 2)
            return IRobot.BEHIND;
        else if (numOfnoMarkExits(robot) != 0) {
            return hierarchySelectExit(robot);
        }
        else {
            // robot.setHeading(reverseDirection(robotData.popJunctionsInfo().getArrivedHeading()));
            // return IRobot.AHEAD;
            int heading = 0;
            heading = hierarchySelectExit(robot);
            robotData.peekJunctionsInfo().setArrivedHeading(remarkMap.relativeToAbs(robot, heading));
            robotData.popJunctionsInfo();
            return heading;
        }
    }

    private int learnedJunction(IRobot robot) {
        startMode = false;
        int[] exits = exits(robot);
        int index = robotData.searchJunctionArr(robot.getLocation().x, robot.getLocation().y);
        if (numOfnoMarkExits(robot) == numOfPhyExits(robot) - 1) // first time encounter this junction
            robotData.addJunctionsInfo(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
        // turn around when all exits are single mark exits
        if (numOfSingleMarkExits(robot) == numOfExits(robot) && remarkMap.lookRemark(robot, IRobot.BEHIND) < 2)
            return IRobot.BEHIND;
        else if (numOfnoMarkExits(robot) != 0 && index != -1 && !robotData.getJunctionArrRecordUsed(index)) {
            robot.setHeading(robotData.getJunctionArrRecordHeading(index));
            robotData.setJunctionArrRecordUsed(index);
            if (robot.look(IRobot.AHEAD) != IRobot.WALL)
                return IRobot.AHEAD;
            else{
                robotData.rmJunctionArrRecord(index);
                return junction(robot);
            }
        }
        else if (numOfnoMarkExits(robot) != 0) { // pick random passage to go
            return hierarchySelectExit(robot);
        }
        else {
            // robot.setHeading(reverseDirection(robotData.popJunctionsInfo().getArrivedHeading()));
            // return IRobot.AHEAD;
            int heading = 0;
            heading = hierarchySelectExit(robot);
            robotData.peekJunctionsInfo().setArrivedHeading(remarkMap.relativeToAbs(robot, heading));
            robotData.popJunctionsInfo();
            return heading;
        }
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
        /* intValue() truncat the digits after decimal
            Value of randno can only be 0, 1, 2 ... length-1 */
        int randno = temp.intValue();
        return directionsChooseFrom[randno];
    }
}

class JunctionRecord {
    private int arrivedHeading;
    private int juncX;
    private int juncY;
    private boolean used;

    public JunctionRecord (int juncX, int juncY, int arrivedHeading) {
        this.juncX = juncX;
        this.juncY = juncY;
        this.arrivedHeading = arrivedHeading;
        this.used = false;
    }

    public int getArrivedHeading() {
        return arrivedHeading;
    }

    public void setArrivedHeading(int heading) {
        arrivedHeading = heading;
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

    public void setUsed() {
        used = true;
    }

    public void setUsed(boolean a) {
        used = a;
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

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof JunctionRecord)) {
            return false;
        }
        JunctionRecord junc = (JunctionRecord) o;
        return juncX == junc.juncX &&
                juncY == junc.juncY &&
                arrivedHeading == junc.arrivedHeading;
        // Objects.equals(juncX, junc.juncX) && Objects.equals(juncY, junc.juncY) && Objects.equals(arrivedHeading, junc.arrivedHeading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(juncX, juncY, arrivedHeading);
    }

    public void reverseArrivedHeading() {
        if ((arrivedHeading - IRobot.NORTH) < 2)
            arrivedHeading = arrivedHeading + 2;
        else
            arrivedHeading = arrivedHeading - 2;
    }
}

class RobotData {
    private Stack<JunctionRecord> junctionsInfo;
    private Stack<JunctionRecord> popedJunctionsInfo;
    private static ArrayList<JunctionRecord> preJunctionsInfoArr;

    public RobotData() {
        junctionsInfo = new Stack<JunctionRecord>();
        popedJunctionsInfo = new Stack<JunctionRecord>();
    }

    public void resetJunctionsInfo() {
        junctionsInfo.clear();
        popedJunctionsInfo.clear();
    }

    public JunctionRecord popJunctionsInfo() {
        popedJunctionsInfo.push(junctionsInfo.peek());
        return junctionsInfo.pop();
    }

    public JunctionRecord peekJunctionsInfo() {
        return junctionsInfo.peek();
    }

    public void addJunctionsInfo(int juncX, int juncY, int arrivedHeading) {
        JunctionRecord junc = new JunctionRecord(juncX, juncY, arrivedHeading);
        junctionsInfo.push(junc);
    }

    public void printJunctionsInfo() {
        System.out.println("-junctionsInfo-");
        junctionsInfo.forEach(data -> data.printJunction());
    }

    public void junctionsInfoToArray() {
        if (preJunctionsInfoArr == null){
            preJunctionsInfoArr = new ArrayList<JunctionRecord>(junctionsInfo);
        }
        else {
            preJunctionsInfoArr.forEach(e -> e.setUsed(false));
            junctionsInfo.forEach(e -> preJunctionsInfoArr.add(e));
            rmDuplicateJunctionArrRecord();
            rmMistakenJunctionArrRecord();
            // printJunctionArrRecord();
        }

        // popedJunctionsInfoReverseHeading();
        popedJunctionsInfo.forEach(e -> preJunctionsInfoArr.add(e));
    }

    public int searchJunctionArr(int junctionX, int junctionY) {
        for (int i = preJunctionsInfoArr.size() - 1; i >= 0; i--)
            if (preJunctionsInfoArr.get(i).getJuncX() == junctionX && preJunctionsInfoArr.get(i).getJuncY() == junctionY)
                return i;
        // finished the loop and there still isn't any match
        return -1;
    }

    public void rmJunctionArrRecord(int i) {
        preJunctionsInfoArr.remove(i);
    }

    public int getJunctionArrRecordHeading(int i) {
        return preJunctionsInfoArr.get(i).getArrivedHeading();
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

    public void rmDuplicateJunctionArrRecord() {
        LinkedHashSet<JunctionRecord> set = new LinkedHashSet<JunctionRecord>(preJunctionsInfoArr);
        preJunctionsInfoArr.clear();
        preJunctionsInfoArr.addAll(set);
    }

    public void printJunctionArrRecord() {
        System.out.println("-preJunctionsInfoArr-");
        preJunctionsInfoArr.forEach(e -> e.printJunction());
    }

    public void popedJunctionsInfoReverseHeading() {
        popedJunctionsInfo.forEach(e -> e.reverseArrivedHeading());
    }


    // remove previous incorrect directions
    public void rmMistakenJunctionArrRecord() {
        for (int i = 0; i < preJunctionsInfoArr.size(); i++) {
            for (int j = 0; j < preJunctionsInfoArr.size(); j++) {
                if (preJunctionsInfoArr.get(i).getJuncX() == preJunctionsInfoArr.get(j).getJuncX()
                && preJunctionsInfoArr.get(i).getJuncY() == preJunctionsInfoArr.get(j).getJuncY()){
                        if (i < j)
                            rmJunctionArrRecord(i);
                        else if (j < i)
                            rmJunctionArrRecord(j);
                }
            }
        }
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
        remarkMap[robot.getLocation().y][robot.getLocation().x] = remarkMap[robot.getLocation().y][robot.getLocation().x] + 1;
    }

    public void markCurrentBlock(IRobot robot, int num) {
        remarkMap[robot.getLocation().y][robot.getLocation().x] = remarkMap[robot.getLocation().y][robot.getLocation().x] = num;
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

