/**
 * File: DumboController.java
 * Created: 17 September 2002, 00:34
 * Author: Stephen Jarvis
 */
import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex2
{
	/**
	 * The count varbiable +1 with each block the robot has
	 * moved. when it hits the multiple of 8 it will guide 
	 * the robot to choose a new random direction
	 */ 
	int random_cnt = 0;
	public void controlRobot(IRobot robot) {

		double randno;
		/**
		 * The default value of direction is AHEAD
		 */
		int direction = IRobot.AHEAD;
		// count the num of walls surrounding the robot
		int walls = 0;
		/**
		 * 1 in random_prob times it will select a new 
		 * random direction
		 */
		int random_prob = 8;
		/**
		 * Since there isn't a default value for the switch 
		 * cases down below, the string needed to be initiallized
		*/ 
		String direction_output = "";
		// circumstances of the robot (deadend or corridor ...)
		String circumstances;

		random_cnt++;
		/* if the robot look ahead and there is a wall then it will randomly choose 
			a new directionto turn to 
			Regardless of the above condition, if the cnt hits the multiple of 8. 
			The robot will jump into this loop to choose a new random direction */
		while (robot.look(direction) == IRobot.WALL | random_cnt % random_prob == 0) {
			// Select a random number within the range of 0 and 3
			randno = Math.random()*4;

			// Convert this to a direction
			if (randno > 0 && randno < 1)
				direction = IRobot.LEFT;
			else if (randno < 2)
				direction = IRobot.RIGHT;
			else if (randno < 3)
				direction = IRobot.BEHIND;
			else
				direction = IRobot.AHEAD;
			
			/* cnt hits 8, robot has chosen a new direction. 
				Increase the cnt once to get the robot out of this loop */
			if(random_cnt % random_prob == 0)
				random_cnt++;
		}
		// testing
		// System.out.println(random_cnt);
		// Face the robot in this direction
		robot.face(direction); 

		

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

		// testing
		// System.out.println(walls);
	}

}
