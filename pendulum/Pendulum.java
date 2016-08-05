
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Pendulum extends JPanel 
	implements ActionListener, MouseListener, MouseMotionListener, KeyListener
{
	private static final int WIDTH = 1000;
	private static final int HEIGHT = 600;
	private static final double nudgeAmount = 2.0;
	private static Random RNG = new Random();

	// The drawing scale
	protected double drawScale = 30.0;
	
	// The system state
	protected double Cmass, Pmass, Plen; // cart mass, pendulum mass, pendulum length
	protected double X, Theta; // position or card, orientation of pendulum
	protected double dX, dTheta; // velocities
	protected double ddX, ddTheta; // accelerations
	protected double gravity; // gravity strength
	protected double motorPower; 
	protected double motorForce = 0.0;
	protected double friction;
	protected double Xmin = -10;
	protected double Xmax = 10;

	private MouseEvent prevMouseState;
	private boolean[] keyPressed = new boolean[256];
	private boolean manualControl;
	
	// Mode buttons
	private JButton initializeButton;	
	private JButton resetPendulumButton;
	private JButton manualControlButton;
	
	// Attribute components
	private JComboBox pendulumMassBox;
	private JComboBox pendulumLengthBox;
	private JComboBox cartMassBox;
	private JComboBox motorPowerBox;
	private JComboBox gravityBox;

	// Timer
	javax.swing.Timer animationTimer;
	private long prevTime;

	/**
	 * Constructor
	 */
	public Pendulum()
	{
		//setSize(WIDTH, HEIGHT);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.setLayout(new BorderLayout());

		// Top Button Panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,7));
		this.add(buttonPanel, BorderLayout.NORTH);
		//
		initializeButton = new JButton("Re-Init");
		initializeButton.addActionListener(this);
		initializeButton.addKeyListener(this);
		buttonPanel.add(initializeButton);
		//
		resetPendulumButton = new JButton("Reset");
		resetPendulumButton.addActionListener(this);
		resetPendulumButton.addKeyListener(this);
		buttonPanel.add(resetPendulumButton);
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
		attributePanel.add(new JLabel(" pendulum mass "));
		String[] pendWts = { "1", "2", "3", "4", "5" };
		pendulumMassBox = new JComboBox(pendWts);
		pendulumMassBox.addActionListener(this);
		attributePanel.add(pendulumMassBox);
		//
		attributePanel.add(new JLabel(" pendulum len. "));
		String[] pendLens = { "1", "2", "3", "4", "5" };
		pendulumLengthBox = new JComboBox(pendLens);
		pendulumLengthBox.addActionListener(this);
		attributePanel.add(pendulumLengthBox);
		//
		attributePanel.add(new JLabel(" cart mass "));
		String[] cartVals = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
		cartMassBox = new JComboBox(cartVals);
		cartMassBox.addActionListener(this);
		attributePanel.add(cartMassBox);
		//
		attributePanel.add(new JLabel(" motor power "));
		String[] motorVals = { "10", "20", "50", "70", "100", "150", "200", "300" };
		motorPowerBox = new JComboBox(motorVals);
		motorPowerBox.addActionListener(this);
		attributePanel.add(motorPowerBox);
		//
		attributePanel.add(new JLabel(" gravity strength "));
		String[] gravityVals = { "1", "5", "10", "25", "50", "100", "250", "500", "1000" };
		gravityBox = new JComboBox(gravityVals);
		gravityBox.addActionListener(this);
		attributePanel.add(gravityBox);

		initialize();

		animationTimer = new javax.swing.Timer(30, this);
		animationTimer.setInitialDelay(50);
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

			// Position Controls
			else if (source == initializeButton) {
				initialize();
				manualControl = false;
			} else if (source == resetPendulumButton) {
				resetPendulum();
				manualControl = false;
			} else if (source == manualControlButton) {
				manualControl = true;
			} 
		
			// System Controls
			else if (source == pendulumMassBox) {
				String val = (String) pendulumMassBox.getSelectedItem();
				setPendulumMass(Double.parseDouble(val));
			} else if (source == pendulumLengthBox) {
				String val = (String) pendulumLengthBox.getSelectedItem();
				setPendulumLength(Double.parseDouble(val));
			} else if (source == cartMassBox) {
				String val = (String) cartMassBox.getSelectedItem();
				setCartMass(Double.parseDouble(val));
			} else if (source == motorPowerBox) {
				String val = (String) motorPowerBox.getSelectedItem();
				setMotorPower(Double.parseDouble(val));
			} else if (source == gravityBox) {
				String val = (String) gravityBox.getSelectedItem();
				setGravity(-Double.parseDouble(val));
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
		long t = System.currentTimeMillis();
		double elapsedTime = (t-prevTime)/1000.0;
		prevTime = t;
		if (elapsedTime > 0.05) elapsedTime = 0.05;

		// Stationary base
		/*
		Theta += dTheta * elapsedTime;
		ddTheta = (gravity/Plen) * Math.sin(Theta);
		if (keyPressed[37]) dTheta += 10 * elapsedTime;
		if (keyPressed[39]) dTheta -= 10 * elapsedTime;
		dTheta += ddTheta;
		dTheta *= friction;
		*/

		// Moving cart
		elapsedTime /= 5;
		for (int j=0; j<5; j++) {
			double sinTheta = Math.sin(Theta);
			double cosTheta = Math.cos(Theta);
			double motor = motorForce * motorPower;
			if (manualControl)  motor = 0.0;
			if (keyPressed[37]) motor = -motorPower;
			if (keyPressed[39]) motor = motorPower;
			for (int i=0; i<3; i++) {
				ddTheta = (ddX * cosTheta - gravity * sinTheta) / Plen;
				ddX = (motor - Pmass * Plen * ddTheta * cosTheta -
					  Pmass * Plen * dTheta * dTheta * sinTheta) / (Pmass + Cmass);
			}
			//	 	
			dTheta += ddTheta * elapsedTime;
			dTheta *= friction;
			dX += ddX * elapsedTime;
			dX *= friction;
			//
			Theta += dTheta * elapsedTime;
			X += dX * elapsedTime;
			//
			
			// Hit against end of track
			/*
			if ((X > Xmax && dX > 0) || (X < Xmin && dX < 0)) {
				sinTheta = Math.sin(Theta);
				cosTheta = Math.cos(Theta);
				double impulse = dX * cosTheta / Plen;
				if (impulse > 2.0) impulse = 2.0;
				if (impulse < -2.0) impulse = -2.0;
				dTheta += impulse;
				dX = 0.0;
				if (X > Xmax) X = Xmax;
				if (X < Xmin) X = Xmin;
				ddX = 0.0; ddTheta = 0.0;
			}
			*/
		}
		if (Theta > Math.PI)  Theta -= 2*Math.PI;
		if (Theta < -Math.PI) Theta += 2*Math.PI;
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
	public void mouseDragged(MouseEvent e) 
	{
		prevMouseState = e;
	}

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
	public void keyTyped(KeyEvent e) { }
	
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
		int cx = getWidth()/2 - 50;
		int cy = getHeight()/2;

		double xx = X; 
		while (xx > Xmax) xx -= (Xmax-Xmin);
		while (xx < Xmin) xx += (Xmax-Xmin);
		int cartx = (int)(cx + (xx * drawScale));
		int carty = cy;
		int cartw = (int)(Math.sqrt(Cmass)*1.33*drawScale);
		int carth = (int)(Math.sqrt(Cmass)*0.8*drawScale);

		int pendx = (int)(cartx - Math.sin(Theta)*Plen*drawScale);
		int pendy = (int)(carty - Math.cos(Theta)*Plen*drawScale);
		int pendr = (int)(Math.sqrt(Pmass/Math.PI)*drawScale);

		int trackStart = (int)(cx + Xmin*drawScale - cartw/2 - 3);
		int trackEnd =   (int)(cx + Xmax*drawScale + cartw/2 + 3);
		
		// Draw Cart Track
		g2.setColor(Color.GREEN);
		g2.fillRect(trackStart, cy-2, trackEnd-trackStart, 4);
		g2.fillRoundRect(trackStart-2, cy-15, 4, 30, 2, 2);
		g2.fillRoundRect(trackEnd-2,   cy-15, 4, 30, 2, 2);

		// Draw cart
		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.BLUE);
		g2.fillRoundRect(cartx-cartw/2, carty-carth/2, cartw, carth, 7, 7);
		g2.setColor(Color.BLACK);
		g2.drawRoundRect(cartx-cartw/2, carty-carth/2, cartw, carth, 7, 7);
		g2.fillOval(cartx-pendr/2, carty-pendr/2, 1+pendr, 1+pendr);
		// Draw pendulum
		g2.setColor(Color.ORANGE);
		g2.fillOval(pendx-pendr, pendy-pendr, 2*pendr, 2*pendr);	
		g2.setColor(Color.BLACK);
		g2.drawOval(pendx-pendr, pendy-pendr, 2*pendr, 2*pendr);
		g2.fillOval(pendx-pendr/2, pendy-pendr/2, 1+pendr, 1+pendr);
		// Draw connecting arm
		g2.setStroke(new BasicStroke((int)(pendr*0.4)));
		g2.drawLine(cartx, carty, pendx, pendy);
		g2.setStroke(new BasicStroke(1));
	}

	/**
	 * Initializes the system, putting in default values for the physical constants
	 */
	public void initialize()
	{
		Cmass      = 4.0;
		Pmass      = 1.0;
		Plen       = 5.0;
		gravity    = -20.0;
		motorPower = 50.0;
		friction   = 0.999;
		resetPendulum();
	}

	/**
	 * Resets the state of the pendulum and cart
	 */
	public void resetPendulum()
	{
		X       = 0.0;
		Theta   = (RNG.nextDouble()-0.5)*0.5;
		dX      = 0.0;
		dTheta  = 0.0;
		ddX     = 0.0;
		ddTheta = 0.1;
	}

	/**
	 * Getters and setters
	 */
	public void setPendulumMass(double m) { Pmass = m; }
	public void setPendulumLength(double l) { Plen = l; }
	public void setCartMass(double m) { Cmass = m; }
	public void setMotorPower(double p) { motorPower = p; }
	public void setGravity(double g) { gravity = g; }
	
	/**
	 * Interface for Controller
	 */
	public void getPendulumState(double[] s) 
	{
		s[0] = X;
		s[1] = dX;
		s[2] = Theta;
		s[3] = dTheta;
	}
	public void setMotorForce(double m) 
	{
		if (m > 1.0)  m = 1.0;
		if (m < -1.0) m = -1.0;
		motorForce = m;
	}
	
	/**
	 * Main 
	 */	
	public static void main(String args[])
	{
		Pendulum pendulum = new Pendulum();
	
		JFrame frame = new JFrame("Pendulum");
		frame.setSize(WIDTH, HEIGHT);
		frame.add(pendulum);
		frame.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) { System.exit(0); }
			}
		);
		frame.setVisible(true);
		
		PendulumController controller = new PendulumController(pendulum);
		(new Thread(controller)).start();
	}
}








