import java.util.Stack;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class GrandFinale {
    private int pollRun = 0;
    private static final int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    private RobotData robotData;
    public void controlRobot(IRobot robot) {
        if (pollRun == 0 && robot.getRuns() == 0){
            robotData = new RobotData();
        }
        robot.face(exploreControl(robot));
        pollRun++;
        robotData.printJunctionsInfo();
    }

    private int exploreControl(IRobot robot) {
        if (numOfExits(robot) == 1)
            return deadEnd(robot);
        else if (numOfExits(robot) == 2)
            return corridor(robot);
        else {
            if (robot.getRuns() == 0)
                return junction(robot);
            else
                return learnedJunction(robot);
        }
    }

    public void reset() {
        robotData.junctionsInfoToArray();
        robotData.resetJunctionsInfo();
        robotData.printJunctionArrRecord();
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
            robotData.addJunctionsInfo(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
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
            robot.setHeading(reverseAbsDirection(robotData.popJunctionsInfo().getArrivedHeading()));
            return IRobot.AHEAD;
        }
    }

    private int learnedJunction(IRobot robot) {
        int[] exits = exitsCanGo(robot);
        int index = robotData.searchJunctionArr(robot.getLocation().x, robot.getLocation().y);
        if (passageExits(robot) == numOfExits(robot) - 1) // first time encounter this junction
            robotData.addJunctionsInfo(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
        if (passageExits(robot) != 0 && index != -1 && !robotData.getJunctionArrRecordUsed(index)) {
            robot.setHeading(robotData.getJunctionArrRecordHeading(index));
            robotData.setJunctionArrRecordUsed(index);
            if (robot.look(IRobot.AHEAD) != IRobot.WALL)
                return IRobot.AHEAD;
            else{
                robotData.rmJunctionArrRecord(index);
                return junction(robot);
            }
        }
        else if (passageExits(robot) != 0) { // pick random passage to go
            int[] psExits = new int[passageExits(robot)];
            int j = 0;
            for (int exit : exits) {
                if (robot.look(exit) == IRobot.PASSAGE)
                    psExits[j++] = exit;
            }
            return chooseRandomHeading(psExits);
        }
        else {
            robot.setHeading(reverseAbsDirection(robotData.popJunctionsInfo().getArrivedHeading()));
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
            // printJunctionArrRecord();
        }

        popedJunctionsInfoReverseHeading();
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
}
