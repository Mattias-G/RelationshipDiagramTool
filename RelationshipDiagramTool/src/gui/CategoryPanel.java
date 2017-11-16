package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Scrollable;

import backend.Backend;
import gui.categories.Category;
import gui.categories.GuiButtonObject;
import gui.categories.InputReturnCode;
import utils.Pair;

@SuppressWarnings("serial")
public class CategoryPanel extends ContentListPanel implements Scrollable {
	private Category selectedCategory;
	
	public CategoryPanel(Backend backend) {		
		super(backend);
		this.bkgColor = new Color(0.8f, 0.8f, 1.0f);

		setMinimumSize(new Dimension(160, 48));
		setPreferredSize(new Dimension(160, 48));
		
		MouseListener ml = new MouseListener();
		addMouseListener(ml);

		dh = Category.HEIGHT;
	}

	public void registerKeyListener() {
		getTopLevelAncestor().addKeyListener(new KeyListener());
	}

	private void unselectCategory() { //TODO
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
	protected List<? extends GuiButtonObject> componentsToDraw() {
		return backend.getCategories();
	}

	@Override
	public void unfocus() { //TODO
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

          getParent().revalidate();
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
  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return Category.HEIGHT / 2;
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return Category.HEIGHT / 2;
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
