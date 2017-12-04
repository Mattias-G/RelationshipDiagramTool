package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import backend.Backend;
import gui.categories.Category;
import gui.categories.GuiButtonObject;
import gui.categories.InputReturnCode;
import gui.graph.Timestamp;
import gui.misc.DescriptionDialogHandler;
import gui.misc.SiblingActions;
import utils.Pair;

@SuppressWarnings("serial")
public abstract class ContentListPanel extends JPanel implements GuiPanel, Scrollable {
	public static final double DOUBLE_CLICK_TIME_MILLIS = 500;
	protected Color bkgColor;
	protected boolean expandVertically;
	
	protected Backend backend;
	protected GuiPanelGroup siblings;

	protected GuiButtonObject selectedObject;
//	private int selectedObjectX;
	
	protected boolean editing;
	protected boolean dragging;
	
	protected long lastLeftClickTime;
	protected long lastRightClickTime;
	protected int dw;
	protected int dh;
	
	protected JFrame frame;
	protected DescriptionDialogHandler descriptionDialogHandler;
	
	public ContentListPanel(Backend backend, JFrame frame, DescriptionDialogHandler descriptionDialogHandler) {		
		this.backend = backend;
		this.frame = frame;
		this.descriptionDialogHandler = descriptionDialogHandler; 

		MouseListener ml = new MouseListener();
		addMouseListener(ml);

		setBorder(BorderFactory.createEtchedBorder());
	}

	public void registerKeyListener() {
		getTopLevelAncestor().addKeyListener(new KeyListener());
	}
	
	public void setSiblingComponent(GuiPanelGroup siblings) {
		this.siblings = siblings;
	}

	protected void unselectObject() {
		if (selectedObject != null) {
			selectedObject.setSelected(false);
			selectedObject.setEditing(false);
			selectedObject = null;
		}
	}

	@Override
	public void unfocus() {
		if (selectedObject != null) {
			unselectObject();
			editing = false;
			repaint();
		}
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		JScrollPane sp = (JScrollPane) getParent().getParent();
		
		if (expandVertically) {
			int h1 = getParent().getHeight();
			int h2 = dh * (backend.getCategories().size()+1);
			int w = dw;
			if (h2 > h1) {
				w += sp.getVerticalScrollBar().getSize().width;
			}
			
			sp.setPreferredSize(new Dimension(w, dh));
			sp.setMinimumSize(new Dimension(w, dh));
			return new Dimension(w, Math.max(h1, h2));
		}
	
		int h = dh;
		int w1 = getParent().getWidth();
		int w2 = dw * (backend.getTimestamps().size()+1);
		if (w2 > w1) {
			h += sp.getHorizontalScrollBar().getSize().height;
		}
		
		sp.setPreferredSize(new Dimension(dw, h));
		sp.setMinimumSize(new Dimension(dw, h));
		return new Dimension(Math.max(w1, w2), h);
	}

	@Override
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}
	
	public boolean sizeUpdate() {
		JScrollPane sp = (JScrollPane) getParent().getParent();
		
		if (expandVertically) {
			int h1 = getParent().getHeight();
			int h2 = dh * (backend.getCategories().size()+1);
			int w = dw;
			if (h2 > h1) {
				w += 15;
			}
			
			sp.setPreferredSize(new Dimension(w, dh));
			sp.setMinimumSize(new Dimension(w, dh));
			return (getWidth() != w);
		}
	
		int h = dh;
		int w1 = getParent().getWidth();
		int w2 = dw * (backend.getTimestamps().size()+1);
		if (w2 > w1) {
			h += 15;
		}
		
		sp.setPreferredSize(new Dimension(dw, h));
		sp.setMinimumSize(new Dimension(dw, h));
		return (getHeight() != h);
	}
	
  @Override
  public boolean isEditing()
  {
    return editing;
  }
  
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g; 
		
		g2d.setColor(bkgColor);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		List<? extends GuiButtonObject> content = componentsToDraw();
		for (int i = 0; i < content.size(); i++) {
			content.get(i).draw(g2d);
			if (squareMarked(i)) {
				g2d.setColor(content.get(i).getColor().darker());
				g2d.fillRect(1, 1, 8, 8);
			}

			if (expandVertically)
				g2d.translate(0,dh);
			else
				g2d.translate(dw,0);
		}

		Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{3}, 0);
		Stroke original = g2d.getStroke();
		
		g2d.setStroke(dashed);
		g2d.setColor(bkgColor.darker());
		g2d.drawRect(2, 0, Category.WIDTH-4, Category.HEIGHT-4);
		g2d.setStroke(original);
		g2d.fillRect(Category.WIDTH/2-Category.HEIGHT/6, Category.HEIGHT/2-1, Category.HEIGHT/3, 2);
		g2d.fillRect(Category.WIDTH/2-1, Category.HEIGHT/3, 2, Category.HEIGHT/3);

	if (expandVertically)
		g2d.translate(0, -dh * content.size());
	else
		g2d.translate(-dw * content.size(), 0);
	}

	protected abstract boolean squareMarked(int i);

	protected abstract List<? extends GuiButtonObject> componentsToDraw();

	protected abstract Pair<GuiButtonObject, InputReturnCode> mouseInput(int x, int y, boolean performAction);

	protected abstract void onRightClick(GuiButtonObject target, MouseEvent e);

	protected abstract GuiButtonObject createNewListObject();
	
	protected abstract void deleteSelectedListObject();
	
	private class MouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			
			if (e.getButton() == MouseEvent.BUTTON1) {
				siblings.unfocus();
	
				editing = false;
				//Select object
				Pair<GuiButtonObject, InputReturnCode> ret = mouseInput(x, y, true);

				if (ret.second() == InputReturnCode.hit) {
					unselectObject();
					selectedObject = ret.first();

//					selectedObjectX = e.getX();
					dragging = true;
				}
				else if (ret.second() != InputReturnCode.miss) {
					repaint();
					siblings.repaint();
					return;
				}
				else
					unselectObject();
				
				//Create new object
				if (expandVertically)
					y -= Category.HEIGHT * componentsToDraw().size();
				else
					x -= Timestamp.WIDTH * componentsToDraw().size();
				
				if (x > 0 && y > 0 && x < dw && y < dh) {
					selectedObject = createNewListObject();
					selectedObject.setSelected(true);
					selectedObject.setColor(Color.getHSBColor((float)Math.random(), 0.5f, 1f));
					selectedObject.setEditing(true);
					editing = true;

					getParent().revalidate();
					if (sizeUpdate()) {
						frame.revalidate();
					}
				}
				
				//Edit if double click
				if ((selectedObject != null)) {
					if (System.currentTimeMillis() - lastLeftClickTime < DOUBLE_CLICK_TIME_MILLIS) {
						selectedObject.setEditing(true);
						editing = true;
						dragging = false;
					}
					lastLeftClickTime = System.currentTimeMillis();
				}
				else {
					lastLeftClickTime = 0;
				}
				
				repaint();
			}
			else if (e.getButton() == MouseEvent.BUTTON3) {
				Pair<GuiButtonObject, InputReturnCode> ret = mouseInput(x, y, false);
				
				if (ret.first() != null) {
					onRightClick(ret.first(), e);
					lastRightClickTime = System.currentTimeMillis();
				}
				else
					lastRightClickTime = 0;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				dragging = false;
			}
		}
	}
	
	private class KeyListener extends KeyAdapter {
		@Override
		public void keyTyped(KeyEvent e) {
			if (editing) {
				selectedObject.keyTyped(e.getKeyChar());
				repaint();
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			boolean repaint = false;
			
			if (!editing && e.getKeyCode() == KeyEvent.VK_DELETE) {
				if (selectedObject != null) {
					deleteSelectedListObject();
					selectedObject = null;
					repaint = true;
					siblings.repaint();
					getParent().revalidate();
					if (sizeUpdate()) {
						frame.revalidate();
					}
				}
			}
			
			if (editing && (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
					e.getKeyCode() == KeyEvent.VK_ENTER)) {
				editing = false;
				selectedObject.setEditing(false);
				repaint = true;
			}
				
			if (selectedObject != null && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				unselectObject();
				repaint = true;
			}
			
			if (repaint)
				repaint();
		}
	}

	@Override
	public void performAction(SiblingActions actionCode, Object... params) {
	}

	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		if (expandVertically)
			return dh / 2;
		return dw / 2;
	}
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		if (expandVertically)
			return dh / 2;
		return dw / 2;
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
}
