
public class RobotState
{
	public double x, y; // position
	public RobotState() { set(0.0, 0.0); }
	public RobotState(double xx, double yy) { set(xx,yy); }
	public void set(double xx, double yy) { x=xx; y=yy; }
	public void move(double dx, double dy) { x+=dx; y+=dy; }
}
