import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

public class RobotController implements ActionListener
{
	public static final int NUM_PARTICLES = 100;
	protected RobotNavigation robotWorld;
	protected javax.swing.Timer animationTimer;
	protected Random rng = new Random();
	protected ArrayList<RobotState> stateParticles;

	double[] rdist = new double[8];
	double[] pdist = new double[8];
	double[] plikelihood = new double[NUM_PARTICLES];
	double[][] utilities;

	public RobotController(RobotNavigation rw)
	{
		robotWorld = rw;
		char[][] maze = robotWorld.getMaze();
		utilities = new double[maze.length][maze[0].length];

		// Create particles 
		stateParticles = new ArrayList<RobotState>();
		for (int i=0; i<NUM_PARTICLES; i++) {
			stateParticles.add(
				new RobotState(rng.nextDouble()*maze[0].length, rng.nextDouble()*maze.length));
		}
		// Let robot world see the particle array
		robotWorld.setStateParticles(stateParticles);

		// Create animation timer
		animationTimer = new javax.swing.Timer(100, this);
		animationTimer.setInitialDelay(200);
		animationTimer.start(); 
	}

	public void actionPerformed(ActionEvent e)
	{
		// FUNCTIONS THAT YOU ARE SHOULD USE FROM robotWorld ARE:
		//
		// public char[][] getMaze()
		//    This gets the current maze as an array of char
		//
		// public RobotState getGoalLocation()
		//    Gets the goal location in the maze
		//
		// public void getDistancesFromRobot(double[] d)
		//    Gets the (inaccurate) measured distances to obstacles from the robot at different angles.
		//    The distances are returned in the d array.
		//
		// public void getDistancesFromLocation(double[] d, double x, double y)
		//    Gets the exact distances to obstacles for a particular point on the map.
		//    You would call this for the location of a particle.
		//    The distances are returned in the d array.
		//
		// public void moveRobot(double theta, double dist)
		//    Attempts to move the robot in a direction based on the angle theta.
		//    Maximum value for dist is 1.0.  The motion is inaccurate, and the
		//    robot will stop at an obstacle.
		//
		// public void moveParticles(double theta, double dist)
		//    Moves the particles in direction theta by dist.  If a particle goes outside
		//    the bounds of the maze, it is reset to a random location.
		
		// REPLACE THE CODE BELOW WITH YOUR CODE
		// Move randomly
		/*
		char[][] maze = robotWorld.getMaze();
		double dist = 0.02;
		double theta = rng.nextDouble() * 2.0 * Math.PI;
		robotWorld.moveRobot(theta,dist);
		robotWorld.moveParticles(theta,dist);
		*/

		char[][] maze = robotWorld.getMaze();

		// Do bellman update of utilities
		for (int r=0; r<utilities.length; r++)
		{
			for (int c=0; c<utilities[r].length; c++)
			{
				if (maze[r][c] == 'G')
				{
					utilities[r][c] = 1.0;
				}
				else if (maze[r][c] == 'X')
				{
					utilities[r][c] = -1.0;
				}
				else // bellman update
				{
					double u0 = utilities[r-1][c];
					double u1 = utilities[r+1][c];
					double u2 = utilities[r][c-1];
					double u3 = utilities[r][c+1];
					double u = u0;
					if (u1 > u) u = u1;
					if (u2 > u) u = u2;
					if (u3 > u) u = u3;
					utilities[r][c] = 0.9 * u;
				}
			}
		}

		// Get the distances from the robot to the walls
		robotWorld.getDistancesFromRobot(rdist);

		// Get the distances from the particles to the walls
		for (int i=0; i<NUM_PARTICLES; i++)
		{
			RobotState p = stateParticles.get(i);
			robotWorld.getDistancesFromLocation(pdist, p.x, p.y);

			double d = 0.0;
			for (int j=0; j<pdist.length; j++)
			{
				//d += Math.abs(pdist[j]-rdist[j]);
				d += (pdist[j]-rdist[j])*(pdist[j]-rdist[j]);
			}
			plikelihood[i] = d;
		}

		// Resample the particles
		ArrayList<RobotState> newParticles = new ArrayList<RobotState>();
		for (int i=0; i<NUM_PARTICLES; i++)
		{
			int aindex = rng.nextInt(NUM_PARTICLES);
			int bindex = rng.nextInt(NUM_PARTICLES);

			if (plikelihood[aindex] < plikelihood[bindex])
			{
				RobotState pOld = stateParticles.get(aindex);
				RobotState p = new RobotState(pOld.x, pOld.y);
				p.x += (rng.nextDouble()-0.5)*0.2;
				p.y += (rng.nextDouble()-0.5)*0.2;
				newParticles.add(p);
			}
			else
			{
				RobotState pOld = stateParticles.get(bindex);
				RobotState p = new RobotState(pOld.x, pOld.y);
				p.x += (rng.nextDouble()-0.5)*0.2;
				p.y += (rng.nextDouble()-0.5)*0.2;
				newParticles.add(p);
			}
		}

		// Estimate our position in the maze
		double rx = 0;
		double ry = 0;
		for (int i=0; i<NUM_PARTICLES; i++)
		{
			RobotState p = newParticles.get(i);
			rx += p.x;
			ry += p.y;
		}
		rx /= NUM_PARTICLES;
		ry /= NUM_PARTICLES;
		//System.out.println(rx + ", " + ry);

		// Figure what square the robot is in on the maze
		int ix = (int)rx;
		int iy = (int)ry;
		if (ix < 1) ix = 1;
		if (ix > maze[0].length-2) ix = maze[0].length-2;
		if (iy < 1) iy = 1;
		if (iy > maze.length-2) iy = maze.length-2;

		// Determine which way to go to maximize utility
		double u0 = utilities[iy-1][ix];
		double u1 = utilities[iy+1][ix];
		double u2 = utilities[iy][ix-1];
		double u3 = utilities[iy][ix+1];
		double u4 = utilities[iy][ix];
		//
		double dist = 0.1;
		if (u4 == 1.0) dist = 0.0;
		double theta = Math.PI;
		if (u0 >= u1 && u0 >= u2 && u0 >= u3)
		{
			theta = Math.PI * 0.5;
		}
		else if (u1 >= u0 && u1 >= u2 && u1 >= u3)
		{
			theta = Math.PI * 1.5;
		}
		else if (u3 >= u0 && u3 >= u1 && u3 >= u2)
		{
			theta = 0;
		}

		// Randomly reposition a few of the particles
		for (int i=0; i<10; i++)
		{
			int aindex = rng.nextInt(NUM_PARTICLES);
			RobotState p = newParticles.get(aindex);
			p.x = rng.nextDouble() * maze[0].length;
			p.y = rng.nextDouble() * maze.length;
		}

		// Swap out the old particles for the new
		stateParticles = newParticles;
		robotWorld.setStateParticles(stateParticles);

		// Move the robot and the particles
		robotWorld.moveRobot(theta,dist);
		robotWorld.moveParticles(theta,dist);
	}
}
