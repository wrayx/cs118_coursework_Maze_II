import uk.ac.warwick.dcs.maze.logic.IRobot;
/**
 * one return statements?
 * java doc
 */
public class Explorer {
    private int[] directions = { IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND };
    public void controlRobot(IRobot robot) {
        int exits;
        int direction = 0;
        exits = nonwallExits(robot);
        if (exits == 1)
            direction = deadEnd(robot);
        else if (exits == 2)
            direction = corridor(robot);
        else if (exits == 3)
            direction = junction(robot);
        else
            direction = crossroads(robot);

        robot.face(direction);
        // System.out.println(chooseRandomHeading(directions));
    }

    /**
     * @param robot that you are trying to guide
     * @return the number of non-WALL squares around the robot
     */
    private int nonwallExits(IRobot robot) {
        int exits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int i = 0; i < directions.length; i++) {
            if (robot.look(directions[i]) != IRobot.WALL)
                exits++;
        }
        return exits;
    } // end nonwallExits()

    /**
     * @param robot that you are trying to guide
     * @return the number of PASSAGE squares around the robot
     */
    private int passageExits(IRobot robot) {
        int psExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int i = 0; i < directions.length; i++) {
            if (robot.look(directions[i]) == IRobot.PASSAGE)
                psExits++;
        }
        return psExits;
    }

    /**
     * @param robot that you are trying to guide
     * @return the number of BEENBEFORE squares adjacent to the robot
     */
    private int passageExits(IRobot robot) {
        int beenBeforeExits = 0;
        // Go through each of the direction to check whether there is a wall or not
        for (int i = 0; i < directions.length; i++) {
            if (robot.look(directions[i]) == IRobot.PASSAGE)
                beenBeforeExits++;
        }
        return beenBeforeExits;
    }

    /**
     * exits = 1
     * @param robot that you are trying to guide
     * if the robot isn't at the start
     * if the robot is at the start
     * @return the direction that isn't a wall
     * otherwise return behind
     */
    private int deadEnd(IRobot robot) {
        int heading = IRobot.BEHIND;
        if (robot.look(heading) != IRobot.BEENBEFORE) {
            if (exitsCanGoExBehind(robot).length == 0)
                heading = IRobot.BEHIND;
            heading = exitsCanGoExBehind(robot)[0];
        }
        return heading;
    }
    /**
     * exits = 2
     * @param robot that you are trying to guide
     * @return the direction that haven't been before if there is one
     * otherwise just choose one that doesn't make the robot to go back on itself
     */
    private int corridor(IRobot robot) {
        int[] Exits = exitsCanGoExBehind(robot);
        if (Exits[0] == IRobot.BEHIND)
            return Exits[1];
        return Exits[0];
    }
    /**
     * exits = 3
     * @param robot that you are trying to guide
     * @return PASSAGE exit if exit
     * otherwise return random direction between all non-wall exits
     */
    private int junction(IRobot robot) {
        int[] Exits = exitsCanGoExBehind(robot);
        if (passageExits(robot) == 0)
            return chooseRandomHeading(Exits);
        else {
            int[] psExits = new int[passageExits(robot)];
            for (int i = 0, j = 0; i < Exits.length; i++) {
                if (robot.look(Exits[i]) == IRobot.PASSAGE)
                    psExits[j++] = Exits[i];
            }
            return chooseRandomHeading(psExits);
        }
    }
    /**
     * exits = 4
     * @param robot that you are trying to guide
     * @return PASSAGE exits if exist
     * if theres more than 1 PASSAGE exits, return random one between them
     * otherwise return random direction that doesn’t cause a collision.
     */
    private int crossroads(IRobot robot) {
        int[] Exits = exitsCanGoExBehind(robot);
        /* if there are no unexplored exits, randomly
            selecting a direction that doesn’t cause a collision */
        if (passageExits(robot) == 0)
            return chooseRandomHeading(Exits);
        else {
            // new array for all passage exits
            int[] psExits = new int[passageExits(robot)];

            /* selecting an unexplored exit if possible,
                selecting randomly between these unexplored
                exits if more than one is possible */
            for (int i = 0, j = 0; i < Exits.length; i++) {
                if (robot.look(Exits[i]) == IRobot.PASSAGE)
                    psExits[j++] = Exits[i];
            }
            return chooseRandomHeading(psExits);
        }
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
     * (except go behind) that will not cause it crash into the wall
     */
    private int[] exitsCanGoExBehind(IRobot robot) {
        int noOfExitsExBehind;
        if (robot.look(IRobot.BEHIND) == IRobot.WALL)
            noOfExitsExBehind = nonwallExits(robot);
        else
            noOfExitsExBehind = nonwallExits(robot) - 1;
        int[] Exits = new int[noOfExitsExBehind];
        for (int i = 0, j = 0; i < directions.length - 1; i++) {
            if (robot.look(directions[i]) != IRobot.WALL)
                Exits[j++] = directions[i];
        }
        return Exits;
    }
} // end Explorer Class