/*
* File: DumboController.java
* Created: 17 September 2002, 00:34
* Author: Stephen Jarvis
*/

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex1
{

	public void controlRobot(IRobot robot) {

		int randno;
		int direction;
		int walls = 0;

		// Select a random number

		randno = (int) Math.round(Math.random()*3);

		// Convert this to a direction

		if (randno == 0)
			direction = IRobot.LEFT;
		else if (randno == 1)
			direction = IRobot.RIGHT;
		else if (randno == 2)
			direction = IRobot.BEHIND;
		else
			direction = IRobot.AHEAD;

		robot.face(direction); /* Face the robot in this direction */

		/* if the controller look ahead and there is a wall
			then it continues to turn left in order to
			prevent the collision */
		while (robot.look(IRobot.AHEAD) == IRobot.WALL)
			robot.face(IRobot.LEFT);

		System.out.print("I'm going ");

		/* switch cases to determine which direction
			is the controller going */
		switch (direction){
			case IRobot.AHEAD:
				System.out.print("forward ");
				break;
			case IRobot.BEHIND:
				System.out.print("backward ");
				break;
			case IRobot.LEFT:
				System.out.print("left ");
				break;
			case IRobot.RIGHT:
				System.out.print("right ");
		}

		/* calculate how many walls are there around
			the controller */
		if(robot.look(IRobot.AHEAD) == IRobot.WALL)
			walls++;
		if(robot.look(IRobot.RIGHT) == IRobot.WALL)
			walls++;
		if(robot.look(IRobot.LEFT) == IRobot.WALL)
			walls++;
		if(robot.look(IRobot.BEHIND) == IRobot.WALL)
			walls++;

		if(walls == 3)
			System.out.println("at a deadend");
		else if(walls == 2)
			System.out.println("down a corridor");
		else if(walls == 1)
			System.out.println("at a junction");
		else
			System.out.println("at a crossroad");

		// the number of walls should never go above 3
		// System.out.println(walls);
	}

}
