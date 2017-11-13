package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import backend.Backend;
import gui.categories.Category;
import gui.categories.InputReturnCode;
import gui.graph.Timestamp;
import utils.Pair;

@SuppressWarnings("serial")
public class TimestampPanel extends JPanel implements GuiPanel {
	private static final Color bkgColor = new Color(0.8f, 1.0f, 0.8f);  
	public static final double DOUBLE_CLICK_TIME_MILLIS = 500; 
	
	private Backend backend;
	private GuiPanelGroup siblings;
	
	private boolean editing;
	private boolean dragging;
	private Timestamp selectedTimestamp;

	private long lastLeftClickTime;
	private long lastRightClickTime;
	
	public TimestampPanel(Backend backend) {		
		this.backend = backend;

		setMinimumSize(new Dimension(0, 48));
		setPreferredSize(new Dimension(0, 48));
		
		setBorder(BorderFactory.createEtchedBorder());
		
		MouseListener ml = new MouseListener();
		addMouseListener(ml);
	}

	public void registerKeyListener() {
		getTopLevelAncestor().addKeyListener(new KeyListener());
	}
	
	public void setSiblingComponent(GuiPanelGroup siblings) {
		this.siblings = siblings;
	}
	
	private void unselectTimestamp() {
		if (selectedTimestamp != null) {
			selectedTimestamp.setSelected(false);
			selectedTimestamp.setEditing(false);
			selectedTimestamp = null;
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g; 
		
		g2d.setColor(bkgColor);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		List<Timestamp> timestamps = backend.getTimestamps();
		for (int i = 0; i < timestamps.size(); i++) {
			timestamps.get(i).draw(g2d);
			if (i == backend.getCurrentTimestampIndex()) {
				g2d.setColor(timestamps.get(i).getColor().darker());
				g2d.fillRect(1, 1, 8, 8);
			}

			g2d.translate(Timestamp.WIDTH,0);
		}

		Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{3}, 0);
		Stroke original = g2d.getStroke();
		
		g2d.setStroke(dashed);
		g2d.setColor(bkgColor.darker());
		g2d.drawRect(2, 0, Category.WIDTH-4, Category.HEIGHT-4);
		g2d.setStroke(original);
		g2d.fillRect(Category.WIDTH/2-Category.HEIGHT/6, Category.HEIGHT/2-1, Category.HEIGHT/3, 2);
		g2d.fillRect(Category.WIDTH/2-1, Category.HEIGHT/3, 2, Category.HEIGHT/3);

		g2d.translate(0, -Category.HEIGHT * timestamps.size());
	}

	@Override
	public void unfocus() {
		if (selectedTimestamp != null) {
			unselectTimestamp();
			editing = false;
			repaint();
		}
	}
	
	private Pair<Timestamp, InputReturnCode> mouseInput(int x, int y, boolean performAction) {
		List<Timestamp> timestamps = backend.getTimestamps();
		for (int i = 0; i < timestamps.size(); i++) {
			InputReturnCode rc = timestamps.get(i).mouseInput(x, y, performAction);
			x -= Timestamp.WIDTH;
			if (rc != InputReturnCode.miss)
				return new Pair<Timestamp, InputReturnCode>(timestamps.get(i), rc);
		}
		return new Pair<Timestamp, InputReturnCode>(null, InputReturnCode.miss);
	}
	
	private class MouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			
			if (e.getButton() == MouseEvent.BUTTON1) {
				siblings.unfocus();
	
				editing = false;
				//Select category
				Pair<Timestamp, InputReturnCode> ret = mouseInput(x, y, true);

				if (ret.second() == InputReturnCode.hit) {
					unselectTimestamp();
					selectedTimestamp = ret.first();
				}
				else if (ret.second() != InputReturnCode.miss) {
					repaint();
					siblings.repaint();
					return;
				}
				else
					unselectTimestamp();
				
				//Create new timestamp
				x -= Timestamp.WIDTH * backend.getTimestamps().size();
				if (x > 0 && y > 0 && x < Category.WIDTH && y < Category.HEIGHT) {
					selectedTimestamp = backend.createTimestamp();
					selectedTimestamp.setSelected(true);
					selectedTimestamp.setColor(Color.getHSBColor((float)Math.random(), 0.5f, 1f));
					selectedTimestamp.setEditing(true);
					editing = true;
					
					backend.makeCurrentTimestamp(selectedTimestamp);
					siblings.repaint();
				}
				
				if (selectedTimestamp != null) {
					dragging = true;
				}
				
				//Edit if double click
				if ((selectedTimestamp != null)) {
					if (System.currentTimeMillis() - lastLeftClickTime < DOUBLE_CLICK_TIME_MILLIS) {
						selectedTimestamp.setEditing(true);
						editing = true;
					}
					lastLeftClickTime = System.currentTimeMillis();
				}
				else {
					lastLeftClickTime = 0;
				}
				
				repaint();
			}
			else if (e.getButton() == MouseEvent.BUTTON3) {
				Pair<Timestamp, InputReturnCode> ret = mouseInput(x, y, false);
				
				if (ret.first() != null) {					
					//Set default if double click
					if (System.currentTimeMillis() - lastRightClickTime < DOUBLE_CLICK_TIME_MILLIS) {
						backend.makeCurrentTimestamp(ret.first());
						siblings.repaint();
						repaint();
					}
					lastRightClickTime = System.currentTimeMillis();
				}
				else
					lastRightClickTime = 0;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}
	}
	
	private class KeyListener extends KeyAdapter {
		@Override
		public void keyTyped(KeyEvent e) {
			if (editing) {
				selectedTimestamp.keyTyped(e.getKeyChar());
				repaint();
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			boolean repaint = false;
			
			if (!editing && e.getKeyCode() == KeyEvent.VK_DELETE) {
				if (selectedTimestamp != null) {
					backend.deleteTimestamp(selectedTimestamp);
					selectedTimestamp = null;
					repaint = true;
					siblings.repaint();
				}
			}
			
			if (editing && (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
					e.getKeyCode() == KeyEvent.VK_ENTER)) {
				editing = false;
				selectedTimestamp.setEditing(false);
				repaint = true;
			}
				
			if (selectedTimestamp != null && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				unselectTimestamp();
				repaint = true;
			}
			
			if (repaint)
				repaint();
		}
	}

	@Override
	public void performAction(SiblingActions actionCode, Object... params) {
		
	}
}
