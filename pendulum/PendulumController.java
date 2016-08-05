
public class PendulumController implements Runnable 
{
	protected Pendulum pendulum;
	
	public PendulumController(Pendulum P)
	{
		pendulum = P;
	}
	
	public void run()
	{
		double[] pState = new double[4];
		
		// Repeat forever
		while (true) {
			// DO NOT CHANGE
			// Current Pendulum State
			pendulum.getPendulumState(pState);
			double X = pState[0];
			double dX = pState[1];
			double Theta = pState[2];
			double dTheta = pState[3];
			
			// REPLACE WITH YOUR CODE
			
			// Actual Controller Code Here:
			// The dumbest possible bang-bang Controller
			/*
			if (Theta > 0) pendulum.setMotorForce(-1.0);
			else           pendulum.setMotorForce(1.0);
			*/

			// P CONTROLLER
			/*
			double mf = -Theta * 10.0;
			if (mf > 1.0) mf = 1.0;
			else if (mf < -1.0) mf = -1.0;
			pendulum.setMotorForce(mf);
			*/

			// PD Controller for goal angle
			double goalAngle = X * 0.05;
			goalAngle += dX * 0.02;
			if (goalAngle > 0.1) goalAngle = 0.1;
			else if (goalAngle < -0.1) goalAngle = -0.1;
			
			// PD Controller for Theta
			double mf = (Theta-goalAngle) * -10.0;
			if (mf > 1.0) mf = 1.0;
			else if (mf < -1.0) mf = -1.0;
			//
			mf += dTheta * -2.0;
			if (mf > 1.0) mf = 1.0;
			else if (mf < -1.0) mf = -1.0;
			
			// If the pendulum falls below horizontal
			// accelerate it in whatever direction it's
			// going.
			if (Theta > 1.57 || Theta < -1.57)
			{
				if (dTheta > 0) mf = -1.0;
				else mf = 1.0;
			}

			pendulum.setMotorForce(mf);

			// DO NOT CHANGE
			// Put the thread to sleep for a few milliseconds to avoid busy waiting
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
	}
}








