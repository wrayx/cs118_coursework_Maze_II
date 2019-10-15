/**
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
		// count the num of walls surrounding the robot
		int walls = 0;
		/* Since there isn't a default value for the switch 
			cases down below, the string needed to be initiallized */
		String direction_output = "";
		// circumstances of the robot (deadend or corridor ...)
		String circumstances;

		/* do once to choose a random direction to turn to,
			and then check if the place ahead of it is a wall.
			if it is a wall, then it will repeat the loop 
			until the condition is not true anymore */
		do {
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
		} while (robot.look(IRobot.AHEAD) == IRobot.WALL);

		

		/* switch cases to determine which direction
			is the controller going */
		switch (direction){
			case IRobot.AHEAD:
				direction_output = "forward";
				break;
			case IRobot.BEHIND:
				direction_output = "backwards";
				break;
			case IRobot.LEFT:
				direction_output = "left";
				break;
			case IRobot.RIGHT:
				direction_output = "right";
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
			circumstances = "at a deadend";
		else if(walls == 2)
			circumstances = "down a corridor";
		else if(walls == 1)
			circumstances = "at a junction";
		else
			circumstances = "at a crossroad";

		System.out.println("I'm going " + direction_output + " " + circumstances);

		// System.out.println(walls);
	}

}
