package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import backend.Backend;
import gui.categories.Category;
import gui.categories.GuiButtonObject;
import gui.categories.InputReturnCode;
import gui.graph.Timestamp;
import utils.Pair;

@SuppressWarnings("serial")
public class TimestampPanel extends ContentListPanel {
	private Timestamp selectedTimestamp;
	
	public TimestampPanel(Backend backend) {	
		super(backend);
		this.bkgColor = new Color(0.8f, 1.0f, 0.8f);

		setMinimumSize(new Dimension(0, 48));
		setPreferredSize(new Dimension(0, 48));
		
		MouseListener ml = new MouseListener();
		addMouseListener(ml);
		
		dw = Timestamp.WIDTH;
	}

	public void registerKeyListener() {
		getTopLevelAncestor().addKeyListener(new KeyListener());
	}
	
	private void unselectTimestamp() { //TODO
		if (selectedTimestamp != null) {
			selectedTimestamp.setSelected(false);
			selectedTimestamp.setEditing(false);
			selectedTimestamp = null;
		}
	}

	@Override
	protected List<? extends GuiButtonObject> componentsToDraw() {
		return backend.getTimestamps();
	}
	

	@Override
	public void unfocus() { //TODO
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
}
