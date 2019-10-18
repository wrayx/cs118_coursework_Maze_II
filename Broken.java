/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Broken 
{
     
     public void controlRobot(IRobot robot) {

     // System.out.println("North = " + IRobot.NORTH);
     // System.out.println("East = " + IRobot.EAST);
     // System.out.println("South = " + IRobot.SOUTH);
     // System.out.println("West = " + IRobot.WEST);
	
     int heading;

     heading = headingController(robot); 
     ControlTest.test(heading, robot); 
     robot.setHeading(heading);

     // testing
     // System.out.println("Target is North (True=1 False=-1): " + isTargetNorth(robot));
     // System.out.println("Target is East (True=1 False=-1): " + isTargetEast(robot));
     // System.out.println();
     // System.out.println("------ " + lookHeading(robot, heading) + " --------");
     // System.out.println("------ " + IRobot.WALL + " --------");
     // System.out.println("------ "+ robot.look(direction) +" --------");
     // System.out.println(cnt);

     }
     
     public void reset() { 
          ControlTest.printResults();
     }

     /**
      * @param robot that you are trying to guide 
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
      * @param robot that you are trying to guide 
      * @return 1 if the target is to the east of the robot, 
      *        -1 if the target is to the west of the robot, 
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

     /**
      * @param robot that you are trying to guide 
      * @param heading the absolute direction the robot is trying to look at
      * @return state of the corresponding square neighbouring the robot
      */
     private int lookHeading(IRobot robot, int heading) {
          int looking_at;
          if (heading == robot.getHeading())
               looking_at = robot.look(IRobot.AHEAD);
          else if (heading == robot.getHeading() - 1 || heading == robot.getHeading() + 3)
               looking_at = robot.look(IRobot.LEFT);
          else if (heading == robot.getHeading() + 1 || heading == robot.getHeading() - 3)
               looking_at = robot.look(IRobot.RIGHT);
          else 
               looking_at = robot.look(IRobot.BEHIND);

          return looking_at;
     }

     /**
      * @param robot that you are trying to guide 
      * @return heading that the robot should go to while there is a heading 
      * that guides the robot closer to the target
      * - it should not guide the robot into the wall
      * - it randomly choose between the headings if there
      *   are multiple heading it can goes to (can either or can neither)
      */
     private int headingController(IRobot robot){
          int heading;	
          double randno;
          int cnt = 0;
          heading = IRobot.SOUTH;
          // same latitude -- there is only 1 direction that will get the robot closer to its target
          if (isTargetNorth(robot) == 0) {  
               if (isTargetEast(robot) == 1 && lookHeading(robot, IRobot.EAST) == IRobot.PASSAGE) // Target is at the east
                    heading = IRobot.EAST;
               else if (isTargetEast(robot) == -1 && lookHeading(robot, IRobot.WEST) == IRobot.PASSAGE) // Target is at the west
                    heading = IRobot.WEST;
          }// same longtitude | still 1 direction
          else if (isTargetEast(robot) == 0) { 
               if (isTargetNorth(robot) == 1 && lookHeading(robot, IRobot.NORTH) == IRobot.PASSAGE)
                    heading = IRobot.NORTH;
               else if (isTargetNorth(robot) == -1 && lookHeading(robot, IRobot.SOUTH) == IRobot.PASSAGE)
                    heading = IRobot.SOUTH;
          } else if (isTargetNorth(robot) == 1 && isTargetEast(robot) == 1) {
               heading = chooseBet2(robot, IRobot.NORTH, IRobot.EAST);
          } else if (isTargetNorth(robot) == -1 && isTargetEast(robot) == 1) { // start here
               heading = chooseBet2(robot, IRobot.SOUTH, IRobot.EAST);
               // if the wall on the south is blocked by the wall, the robot needs to instead turn into east
          } else if (isTargetNorth(robot) == 1 && isTargetEast(robot) == -1) {
               heading = chooseBet2(robot, IRobot.NORTH, IRobot.WEST);
          } else if (isTargetNorth(robot) == -1 && isTargetEast(robot) == -1) {
               heading = chooseBet2(robot, IRobot.SOUTH, IRobot.WEST);
          }

          /* 3^2 
          TargetNorth    TargetEast     Going 
          -------------------------------------------
          0              1              East
          0              -1             West
          1              0              North
          -1             0              South
          1              1              North | East
          -1             1              South | East
          1              -1             North | West
          -1             -1             South | West
          0              0              Random choice 
          */
          while (lookHeading(robot, heading) == IRobot.WALL){
               heading = chooseRandomHeading();
          }
          /* check again if the above direction chosen will lead the robot
               crach into the wall. */
          
          return heading;
     }

     /** 
      * @return a absolute heading that is randomly chosen from 4 (N,E,S,W)
      */
     private int chooseRandomHeading() {
          double randno;
          int heading;
          randno = Math.random()*4;
          if (randno < 1)
               heading = IRobot.NORTH;
          else if (randno < 2)
               heading = IRobot.SOUTH;
          else if (randno < 3)
               heading = IRobot.EAST;
          else
               heading = IRobot.WEST;

          return heading;
     }

     /**
      * @param robot that you are trying to guide 
      * @param heading1 one of the absolute heading the robot can choose
      * @param heading2 the other absolute heading the robot can choose
      * @return the absolute heading chosen from 2 that will not crash into the wall
      */
     private int chooseBet2(IRobot robot, int heading1, int heading2) {
          int heading;
          double randno;
          randno = Math.random() * 2;
          if (randno < 1){
               if(lookHeading(robot, heading1) != IRobot.WALL)
                    heading = heading1;
               else 
                    heading = heading2;
                    /* if the first random direction chosen from the 2 did let the robot 
                         crash into the wall then it will instead choose the other direction. 
                         But the other one might still let the robot crash into the wall then the 
                         while loop inside of the headingController() function will check that again. 
                         if that is the case then it will just choose a new random direction from 
                         all 4 that will not let it crash into the wall */
          }else{
               if (lookHeading(robot, heading2) != IRobot.WALL)
                    heading = heading2;
               else 
                    heading =heading1;
          }
          return heading;
     }
}
