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
import utils.Pair;

@SuppressWarnings("serial")
public class CategoryPanel extends JPanel implements GuiPanel {
	private static final Color bkgColor = new Color(0.8f, 0.8f, 1.0f);  
	public static final double DOUBLE_CLICK_TIME_MILLIS = 500; 
	
	private Backend backend;
	private GuiPanelGroup siblings;
	
	private boolean editing;
	private boolean dragging;
	private Category selectedCategory;

	private long lastLeftClickTime;
	private long lastRightClickTime;
	
	public CategoryPanel(Backend backend) {		
		this.backend = backend;

		setMinimumSize(new Dimension(160, 48));
		setPreferredSize(new Dimension(160, 48));
		
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

	private void unselectCategory() {
		if (selectedCategory != null) {
			selectedCategory.setSelected(false);
			selectedCategory.setEditing(false);
			selectedCategory = null;
		}
	}
		
	@Override
	public Dimension getPreferredSize()
	{
		int h1 = getParent().getHeight();
		int h2 = Category.HEIGHT * (backend.getCategories().size()+1);
		int w = Category.WIDTH;
		if (h2 > h1) {
			w += 16;
		}
		
		return new Dimension(w, Math.max(h1, h2));
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g; 
		
		g2d.setColor(bkgColor);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		List<Category> categories = backend.getCategories();
		for (int i = 0; i < categories.size(); i++) {
			categories.get(i).draw(g2d);
			if (categories.get(i) == backend.getDefaultCategory()) {
				//g2d.setStroke(dashed_thin);
				g2d.setColor(categories.get(i).getColor().darker());
				//g2d.drawRect(2, 1, Category.WIDTH-5, Category.HEIGHT-4);
				g2d.fillRect(1, 1, 8, 8);
				//g2d.setStroke(original);
			}

			g2d.translate(0, Category.HEIGHT);
		}

		Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{3}, 0);
		Stroke original = g2d.getStroke();
		
		g2d.setStroke(dashed);
		g2d.setColor(bkgColor.darker());
		g2d.drawRect(2, 0, Category.WIDTH-4, Category.HEIGHT-4);
		g2d.setStroke(original);
		g2d.fillRect(Category.WIDTH/2-Category.HEIGHT/6, Category.HEIGHT/2-1, Category.HEIGHT/3, 2);
		g2d.fillRect(Category.WIDTH/2-1, Category.HEIGHT/3, 2, Category.HEIGHT/3);

		g2d.translate(0, -Category.HEIGHT * categories.size());
	}

	@Override
	public void unfocus() {
		if (selectedCategory != null) {
			unselectCategory();
			editing = false;
			repaint();
		}
	}
	
	private Pair<Category, InputReturnCode> mouseInput(int x, int y, boolean performAction) {
		List<Category> categories = backend.getCategories();
		for (int i = 0; i < categories.size(); i++) {
			InputReturnCode rc = categories.get(i).mouseInput(x, y, performAction);
			y -= Category.HEIGHT;
			if (rc != InputReturnCode.miss)
				return new Pair<Category, InputReturnCode>(categories.get(i), rc);
		}
		return new Pair<Category, InputReturnCode>(null, InputReturnCode.miss);
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
				Pair<Category, InputReturnCode> ret = mouseInput(x, y, true);

				if (ret.second() == InputReturnCode.hit) {
					unselectCategory();
					selectedCategory = ret.first();
				}
				else if (ret.second() != InputReturnCode.miss) {
					repaint();
					siblings.repaint();
					return;
				}
				else
					unselectCategory();
				
				//Create new category
				y -= Category.HEIGHT * backend.getCategories().size();
				if (x > 0 && y > 0 && x < Category.WIDTH && y < Category.HEIGHT) {
					selectedCategory = backend.createCategory();
					selectedCategory.setSelected(true);
					selectedCategory.setColor(Color.getHSBColor((float)Math.random(), 0.5f, 1f));
					selectedCategory.setEditing(true);
					editing = true;
				}
				
				if (selectedCategory != null) {
					dragging = true;
				}
				
				//Edit if double click
				if ((selectedCategory != null)) {
					if (System.currentTimeMillis() - lastLeftClickTime < DOUBLE_CLICK_TIME_MILLIS) {
						selectedCategory.setEditing(true);
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
				Pair<Category, InputReturnCode> ret = mouseInput(x, y, false);
				
				if (ret.first() != null) {
					siblings.performAction(SiblingActions.setCategory, ret.first());
					siblings.repaint();
					
					//Set default if double click
					if (System.currentTimeMillis() - lastRightClickTime < DOUBLE_CLICK_TIME_MILLIS) {
						backend.setDefautCategory(ret.first());
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
				selectedCategory.keyTyped(e.getKeyChar());
				repaint();
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			boolean repaint = false;
			
			if (!editing && e.getKeyCode() == KeyEvent.VK_DELETE) {
				if (selectedCategory != null) {
					backend.deleteCategory(selectedCategory);
					selectedCategory = null;
					repaint = true;
					siblings.repaint();
					getParent().revalidate();
				}
			}
			
			if (editing && (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
					e.getKeyCode() == KeyEvent.VK_ENTER)) {
				editing = false;
				selectedCategory.setEditing(false);
				repaint = true;
			}
				
			if (selectedCategory != null && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				unselectCategory();
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
