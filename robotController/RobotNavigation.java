
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

public class RobotNavigation extends JPanel 
	implements ActionListener, MouseListener, MouseMotionListener, KeyListener
{
	// Drawing attributes
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	public static final double moveDist = 0.01;
	protected double drawScale = 30.0;
	
	// Animation Timer
	javax.swing.Timer animationTimer;
	private long prevTime;

	// Mouse and keyboard state
	private MouseEvent prevMouseState;
	private boolean[] keyPressed = new boolean[256];
	
	// Mode buttons
	private JButton loadButton;	
	private JButton resetButton;
	private JButton manualControlButton;

	// System attributes
	double sensorAccuracy = 0.1;
	double movementAccuracy = 0.01;
	
	// Attribute components
	private JComboBox sensorAccuracyBox;
	private JComboBox movementAccuracyBox;
	
	// The robot and world state
	protected char[][] maze;
	protected RobotState goalLocation = new RobotState();
	protected RobotState robotState = new RobotState();
	protected ArrayList<RobotState> stateParticles;
	
	// Random number generator for 
	Random rng = new Random();
	
	// The file chooser
	JFileChooser fileChooser = new JFileChooser();

	/**
	 * Constructor
	 */
	public RobotNavigation(String firstMaze)
	{
		//setSize(WIDTH, HEIGHT);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.setLayout(new BorderLayout());

		// Top Button Panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2));
		this.add(buttonPanel, BorderLayout.NORTH);
		//
		loadButton = new JButton("Load Maze");
		loadButton.addActionListener(this);
		loadButton.addKeyListener(this);
		buttonPanel.add(loadButton);
		//
		resetButton = new JButton("Reset Location");
		resetButton.addActionListener(this);
		resetButton.addKeyListener(this);
		buttonPanel.add(resetButton);
		//
		manualControlButton = new JButton("Manual Control");
		manualControlButton.addActionListener(this);
		manualControlButton.addKeyListener(this);
		buttonPanel.add(manualControlButton);
	
		// Side panel
		JPanel attributePanel = new JPanel();
		attributePanel.setLayout(new GridLayout(20,1));
		this.add(attributePanel, BorderLayout.EAST);
		//
		attributePanel.add(new JLabel(" Sensor Accuracy "));
		String[] sensorAccuracyWeights = { "0.1", "0.2", "0.3", "0.4", "0.5", "0.0" };
		sensorAccuracyBox = new JComboBox(sensorAccuracyWeights);
		sensorAccuracyBox.addActionListener(this);
		attributePanel.add(sensorAccuracyBox);
		//
		attributePanel.add(new JLabel(" Movement Accuracy "));
		String[] movementAccuracyWeights = { "0.01", "0.02", "0.03", "0.04", "0.05", "0.0" };
		movementAccuracyBox = new JComboBox(movementAccuracyWeights);
		movementAccuracyBox.addActionListener(this);
		attributePanel.add(movementAccuracyBox);

		initialize(firstMaze);

		animationTimer = new javax.swing.Timer(100, this);
		animationTimer.setInitialDelay(200);
		animationTimer.start(); 
	}
	
	/**
	 * Receives events from GUI controls.
	 * @param e: the event
	 */
	public void actionPerformed(ActionEvent e)
	{ 
		//System.out.println(e.getActionCommand());
		Object source = e.getSource();
		
		try {
			// Animation timer
			if (source == animationTimer) {
				animate();
			}
			else if (source == sensorAccuracyBox) {
				sensorAccuracy = Double.parseDouble(""+sensorAccuracyBox.getSelectedItem());
			}
			else if (source == movementAccuracyBox) {
				movementAccuracy = Double.parseDouble(""+movementAccuracyBox.getSelectedItem());
			}
			else if (source == resetButton) {
				resetRobotLocation();
			}
			else if (source == loadButton) {
				fileChooser.setCurrentDirectory(new File("."));
				int res = fileChooser.showOpenDialog(this);
				
				if (res == JFileChooser.APPROVE_OPTION) {
					File F = fileChooser.getSelectedFile();
					loadMaze(F);
					resetRobotLocation();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
			
		repaint();
	}

	/** 
	 * Animates the pendulum for 1 timestep
	 */
	public void animate()
	{
		if (keyPressed[KeyEvent.VK_LEFT]) { 
			moveRobot(Math.PI, 0.25);
			moveParticles(Math.PI, 0.25);
		}
		if (keyPressed[KeyEvent.VK_RIGHT]) { 
			moveRobot(0, 0.25);
			moveParticles(0, 0.25);
		}
		if (keyPressed[KeyEvent.VK_UP]) { 
			moveRobot(Math.PI*0.5, 0.25);
			moveParticles(Math.PI*0.5, 0.25);
		}
		if (keyPressed[KeyEvent.VK_DOWN]) { 
			moveRobot(Math.PI*1.5, 0.25);
			moveParticles(Math.PI*1.5, 0.25);
		}
	}
	
	/**
	 * Mouse events
	 * @param e: the MouseEvent
	 */
	public void mouseClicked(MouseEvent e) { prevMouseState = e; }
	public void mouseEntered(MouseEvent e) { prevMouseState = e; }
	public void mousePressed(MouseEvent e) { prevMouseState = e; } 
	public void mouseReleased(MouseEvent e) { prevMouseState = e; } 
	public void mouseExited(MouseEvent e) { prevMouseState = e; }  
	public void mouseMoved(MouseEvent e) { prevMouseState = e; }
	public void mouseDragged(MouseEvent e) { prevMouseState = e; }

	/**
	 * Key events
	 * @param e: the KeyEvent
	 */
	public void keyPressed(KeyEvent e) { 
		int keyCode = e.getKeyCode();
		if (keyCode >= 256) return;
		keyPressed[keyCode] = true;
	}
	public void keyReleased(KeyEvent e) { 
		int keyCode = e.getKeyCode();
		if (keyCode >= 256) return;
		keyPressed[keyCode] = false;
	}
	public void keyTyped(KeyEvent e) {
	}
	
	/**
	 * Paints the system with the cart and pendulum
	 * @param g: the graphics context
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		                    RenderingHints.VALUE_ANTIALIAS_ON);
							
		int renderScale = Math.min((getWidth()-100)/maze.length, 
		                           (getHeight()-100)/maze[0].length);	
		
		boolean win = false;
		int rx = (int) robotState.x;
		int ry = (int) robotState.y;
		if (rx>=0 && rx<maze[0].length && ry>=0 && ry<maze.length && maze[ry][rx]=='G') win = true;

		
		int x0 = getWidth()/2 - maze[0].length*renderScale/2;
		int y0 = getHeight()/2 - maze.length*renderScale/2 + 8;
		//int renderScale = 24;
		
		// Draw the Maze
		for (int j=0; j<maze.length; j++) {
			for (int i=0; i<maze[0].length; i++) {
				if (maze[j][i] == 'X') {
					g2.setColor(Color.BLUE);
					g2.fillRoundRect(x0+i*renderScale+1, y0+j*renderScale+1, renderScale-2, renderScale-2, 5, 5);
					g2.setColor(Color.GRAY);
					if (win == true) g2.setColor(Color.YELLOW);
					g2.fillOval(x0+i*renderScale+renderScale/4-1, y0+j*renderScale+renderScale/4-1, 
						renderScale/2, renderScale/2);
					g2.setColor(Color.BLACK);
					g2.drawRoundRect(x0+i*renderScale, y0+j*renderScale, renderScale-2, renderScale-2, 5, 5);
				} 
				else if (maze[j][i] == 'G') {
					g2.setColor(Color.YELLOW);
					g2.fillRoundRect(x0+i*renderScale+1, y0+j*renderScale+1, renderScale-2, renderScale-2, 5, 5);
					g2.setColor(Color.RED);
					g2.fillOval(x0+i*renderScale+renderScale/4-1, y0+j*renderScale+renderScale/4-1, 
						renderScale/2, renderScale/2);
					g2.setColor(Color.BLACK);
					g2.drawRoundRect(x0+i*renderScale, y0+j*renderScale, renderScale-2, renderScale-2, 5, 5);
				}
			}
		}
					
		// Draw the particles
		ArrayList<RobotState> particles = stateParticles;
		for (int i=0; particles!=null && i<particles.size(); i++) {
			RobotState P = particles.get(i);
			g2.setColor(Color.GREEN);
			g2.fillOval((int)(P.x*renderScale+x0-2), (int)(P.y*renderScale+y0-2), 4, 4);
			g2.setColor(Color.BLACK);
			g2.drawOval((int)(P.x*renderScale+x0-2), (int)(P.y*renderScale+y0-2), 4, 4);
		}

		// Draw sensor lines
		double[] d = new double[16];
		getDistancesFromRobot(d);
		drawDistances(g2, d, robotState.x, robotState.y, x0, y0, renderScale);
		
		// Draw the actual location of the robot
		g2.setColor(Color.RED);
		g2.fillOval((int)(robotState.x*renderScale+x0-6), (int)(robotState.y*renderScale+y0-6), 12, 12);
		g2.setColor(Color.BLACK);
		g2.drawOval((int)(robotState.x*renderScale+x0-6), (int)(robotState.y*renderScale+y0-6), 12, 12);
		g2.drawLine((int)(x0+robotState.x*renderScale),   (int)(y0+robotState.y*renderScale)-8,
			        (int)(x0+robotState.x*renderScale),   (int)(y0+robotState.y*renderScale)+8);
		g2.drawLine((int)(x0+robotState.x*renderScale)-8, (int)(y0+robotState.y*renderScale),
			        (int)(x0+robotState.x*renderScale)+8, (int)(y0+robotState.y*renderScale));
	}
	
	public void drawDistances(Graphics2D g, double[] d, double x, double y, int xOrigin, int yOrigin, double renderScale)
	{
		g.setColor(Color.GRAY);
		for (int i=0; i<d.length; i++) {
			double theta = i * 2.0 * Math.PI / d.length;
			int x0 = (int)(x*renderScale);
			int y0 = (int)(y*renderScale);
			int x1 = (int)((x+d[i]*Math.cos(theta))*renderScale);
			int y1 = (int)((y+d[i]*Math.sin(theta))*renderScale);
			g.drawLine(x0+xOrigin,y0+yOrigin, x1+xOrigin,y1+yOrigin);
		}
	}

	/**
	 * Initializes the system, putting in default values for the physical constants
	 */
	public void initialize(String fileName)
	{
		loadMaze(new File(fileName));
		robotState.set(2,7);
		stateParticles = new ArrayList<RobotState>();
	}
	
	public void loadMaze(File F)
	{
		try {
			Scanner scan = new Scanner(F);
			int w = scan.nextInt();
			int h = scan.nextInt();
			maze = new char[h][w];
			scan.nextLine();
			for (int j=0; j<h; j++) {
				String line = scan.nextLine();
				for (int i=0; i<w; i++) {
					if (i >= line.length()) break;
					maze[j][i] = line.charAt(i);
					if (maze[j][i] == 'G') goalLocation = new RobotState(i+0.5,j+0.5);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double getDistance(double x, double y, double theta)
	{
		double d = 0.0;
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		double dd = 0.5;
		while (dd > 0.02) {
			int i = (int)(x + cosTheta * d);
			int j = (int)(y - sinTheta * d);
			if (i<0 || i>=maze[0].length || j<0 || j>=maze.length) break;
			if (maze[j][i] == 'X') { d-=dd*0.5; dd*=0.5; continue; }
			d += dd;
		}
		return d;
	}
	
	/**
	 * Main 
	 */	
	public static void main(String args[])
	{
		JFrame frame = new JFrame("Robot Navigation");
		frame.setSize(WIDTH, HEIGHT);
		String firstMaze = "Maze1.txt";
		if (args.length > 0) firstMaze = args[0];
		RobotNavigation nav = new RobotNavigation(firstMaze);
		frame.add(nav);
		frame.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) { System.exit(0); }
			}
		);
		RobotController rc = new RobotController(nav);

		frame.setVisible(true);
	}
	
	public void resetRobotLocation()
	{
		double x,y;
		while (true) {
			x = rng.nextDouble() * maze[0].length;
			y = rng.nextDouble() * maze.length;
			if (maze[(int)y][(int)x] == ' ') break;
		}
		robotState.set(x,y);
	}
	
	//------------------------------ FUNCTIONS THE CONTROLLER CAN CALL
	
	public void setStateParticles(ArrayList<RobotState> particles)
	{
		stateParticles = particles;
	}

	public char[][] getMaze() 
	{
		return maze;
	}
	
	public RobotState getGoalLocation()
	{
		return goalLocation;
	}
	
	public void getDistancesFromRobot(double[] d)
	{
		getDistancesFromLocation(d, robotState.x, robotState.y);
		for (int i=0; i<d.length; i++) {
			d[i] = d[i] + rng.nextGaussian()*sensorAccuracy;
			if (d[i]<0.0) d[i]=0.0;
		}
	}
	
	public void getDistancesFromLocation(double[] d, double x, double y)
	{
		for (int i=0; i<d.length; i++) {
			double theta = -i * 2.0 * Math.PI / d.length;
			d[i] = getDistance(x,y, theta);
		}
	}
	
	public void moveRobot(double theta, double dist)
	{
		if (dist<0) dist = 0;
		if (dist>1.0) dist = 1.0;
		theta = -theta + rng.nextGaussian()*movementAccuracy;
		double dx = Math.cos(theta)*dist;
		double dy = Math.sin(theta)*dist;
		double x = robotState.x;
		double y = robotState.y;
		for (int i=0; i<5; i++) {
			x += dx/5.0;
			y += dy/5.0;
			if (y<=0 || y>=maze.length || x<=0 || x>=maze[0].length || maze[(int)y][(int)x] == 'X') {
				x -= dx/5.0; 
				y -= dy/5.0;
				dx *= 0.5;
				dy *= 0.5;
				continue;
			}
		}
		robotState.set(x,y);
	}
	
	public void moveParticles(double theta, double dist)
	{
		double dx = Math.cos(theta)*dist;
		double dy = -Math.sin(theta)*dist;
		for (int i=0; i<stateParticles.size(); i++) {
			RobotState P = stateParticles.get(i);
			P.move(dx,dy);
			if (P.x < 0 || P.x >= maze[0].length || P.y < 0 || P.y >= maze.length) {
					P.set(rng.nextDouble()*maze[0].length, rng.nextDouble()*maze.length);
			}
		}
	}
}








