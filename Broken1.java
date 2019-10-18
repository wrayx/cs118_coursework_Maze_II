/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Broken 
{
     
     public void controlRobot(IRobot robot) {

     int direction;	
     int heading;

     heading = headingController(robot); 
     // ControlTest.test(heading, robot); 
     robot.setHeading(heading);

     // testing
     System.out.println("Target is North (True=1 False=-1): " + isTargetNorth(robot));
     System.out.println("Target is East (True=1 False=-1): " + isTargetEast(robot));
     // System.out.println();
     System.out.println("------ "+ lookHeading(robot, heading) +" --------");
     System.out.println("------ "+ IRobot.WALL +" --------");
     // System.out.println("------ "+ robot.look(direction) +" --------");
     // System.out.println(cnt);

     }

     /**
      * @param robot you are using on the maze
      * @return 1 if the target is north of the robot, 
      *        -1 if the target is south of the robot 
      *        and 0 otherwise.
      */
     private byte isTargetNorth(IRobot robot) {
          byte result = 0;
          if (robot.getLocation().y > robot.getTargetLocation().y)
               result = 1;
          else if (robot.getLocation().y < robot.getTargetLocation().y)
               result = -1;
          // testing
          System.out.println("y coordinated of r and t: (" + robot.getLocation().y + ", " + robot.getTargetLocation().y + ")");
          return result;
     }

     /**
      * @param robot you are using on the maze
      * @return 1 if the target is to the east of the robot, 
      *        -1 if the target is to the west of the target, 
      *        and 0 otherwise.
      */
     private byte isTargetEast(IRobot robot) {
          byte result = 0;
          if (robot.getLocation().x < robot.getTargetLocation().x)
               result = 1;
          else if (robot.getLocation().x > robot.getTargetLocation().x)
               result = -1;
          // testing
          System.out.println("x coordinated of r and t: (" + robot.getLocation().x + ", " + robot.getTargetLocation().x + ")");
          return result;
     }

     private int lookHeading(IRobot robot, int heading) {
          int looking_at;
          if (heading == robot.getHeading())
               looking_at = robot.look(IRobot.AHEAD);
          else if (heading == (robot.getHeading())-1 || heading == robot.getHeading()+3)
               looking_at = robot.look(IRobot.LEFT);
          else if (heading == robot.getHeading()+1 || heading == robot.getHeading()-3)
               looking_at = robot.look(IRobot.RIGHT);
          else 
               looking_at = robot.look(IRobot.BEHIND);

          return looking_at;
     }

     private int headingController(IRobot robot){
          int heading;
          double randno;
          int cnt = 0;
          heading = IRobot.NORTH;

          randno = Math.random()*4;
          if (isTargetNorth(robot) == 0) { // same latitude 同纬度
               if (isTargetEast(robot) == 1 && lookHeading(robot, IRobot.EAST) != IRobot.WALL) // Target is at the east
                    heading = IRobot.EAST;
               else if (isTargetEast(robot) == -1 && lookHeading(robot, IRobot.WEST) != IRobot.WALL) // Target is at the west
                    heading = IRobot.WEST;
          } else if (isTargetEast(robot) == 0) { // same longtitude 同经度
               if (isTargetNorth(robot) == 1 && lookHeading(robot, IRobot.NORTH) != IRobot.WALL)
                    heading = IRobot.NORTH;
               else if (isTargetNorth(robot) == -1 && lookHeading(robot, IRobot.SOUTH) != IRobot.WALL)
                    heading = IRobot.SOUTH;
          } else if (isTargetNorth(robot) == 1 && isTargetEast(robot) == 1) {
               if (randno < 2 && lookHeading(robot, IRobot.NORTH) != IRobot.WALL)
                    heading = IRobot.NORTH;
               else if (lookHeading(robot, IRobot.SOUTH) != IRobot.WALL)
                    heading = IRobot.SOUTH;
          } else if (isTargetNorth(robot) == -1 && isTargetEast(robot) == 1) {
               if (randno < 2 && lookHeading(robot, IRobot.SOUTH) != IRobot.WALL)
                    heading = IRobot.SOUTH;
               else if (lookHeading(robot, IRobot.EAST) != IRobot.WALL)
                    heading = IRobot.EAST;
          } else if (isTargetNorth(robot) == 1 && isTargetEast(robot) == -1) {
               if (randno < 2 && lookHeading(robot, IRobot.NORTH) != IRobot.WALL)
                    heading = IRobot.NORTH;
               else if (lookHeading(robot, IRobot.WEST) != IRobot.WALL)
                    heading = IRobot.WEST;
          } else if (isTargetNorth(robot) == -1 && isTargetEast(robot) == -1) {
               if (randno < 2 && lookHeading(robot, IRobot.SOUTH) != IRobot.WALL)
                    heading = IRobot.SOUTH;
               else if (lookHeading(robot, IRobot.WEST) != IRobot.WALL)
                    heading = IRobot.WEST;
          }

          while (lookHeading(robot, heading) == IRobot.WALL)
               heading = chooseRandomHeading();
          // it randomly pick a heading that will not crash into the wall
          return heading;
     }

     // public void reset() { 
     //      ControlTest.printResults();
     // }

     private int chooseRandomHeading() {
          double randno;
          int heading;
          randno = Math.random()*4;
          if (randno > 0 && randno < 1)
               heading = IRobot.NORTH;
          else if (randno > 1 && randno < 2)
               heading = IRobot.SOUTH;
          else if (randno > 2 && randno < 3)
               heading = IRobot.EAST;
          else
               heading = IRobot.WEST;

          return heading;
     }

}
