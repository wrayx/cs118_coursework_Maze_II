/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Broken 
{
     int x = 1;
     int y = 1;
    
     public void controlRobot(IRobot robot) {

     int direction;	
     int randno;

     direction = IRobot.EAST;

     do {

          randno = (int) Math.round(Math.random()*3);

          if ( randno == 0)
               direction = IRobot.LEFT;
          else if (randno == 1)
               direction = IRobot.RIGHT;
          else if (randno == 2)
               direction = IRobot.BEHIND;
          else 
               direction = IRobot.AHEAD;
    } while (robot.look(direction)==IRobot.WALL);
          
     robot.face(direction);  /* Face the direction */

     // testing
     System.out.println("Target is North (True=1 False=-1): " + isTargetNorth(robot));
     System.out.println("Target is East (True=1 False=-1): " + isTargetEast(robot));
     // System.out.println();
     System.out.println("------ "+ lookHeading(robot, IRobot.EAST) +" --------");
     System.out.println("------ "+ robot.look(direction) +" --------");

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
          int surroundings;
          if (heading == robot.getHeading())
               surroundings = robot.look(IRobot.AHEAD);
          else if (heading++ == robot.getHeading() || heading - 3 == robot.getHeading())
               surroundings = robot.look(IRobot.LEFT);
          else if (heading-- == robot.getHeading() || heading + 3 == robot.getHeading())
               surroundings = robot.look(IRobot.RIGHT);
          else 
               surroundings = robot.look(IRobot.BEHIND);

          return surroundings;
     }

     // private int headingController(IRobot robot)

}
