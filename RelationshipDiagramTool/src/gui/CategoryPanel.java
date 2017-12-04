package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JFrame;

import backend.Backend;
import gui.categories.Category;
import gui.categories.GuiButtonObject;
import gui.categories.InputReturnCode;
import gui.misc.DescriptionDialogHandler;
import gui.misc.SiblingActions;
import utils.Pair;

@SuppressWarnings("serial")
public class CategoryPanel extends ContentListPanel {
	
	public CategoryPanel(Backend backend, JFrame frame, DescriptionDialogHandler ddh) {		
		super(backend, frame, ddh);
		this.bkgColor = new Color(0.8f, 0.8f, 1.0f);

		setMinimumSize(new Dimension(160, 48));
		setPreferredSize(new Dimension(160, 48));
		
		dh = Category.HEIGHT;
		dw = Category.WIDTH;
		expandVertically = true;
	}
		
	@Override
	protected List<? extends GuiButtonObject> componentsToDraw() {
		return backend.getCategories();
	}
	
	@Override
	protected GuiButtonObject createNewListObject() {
		return backend.createCategory();
	}

	@Override
	protected void deleteSelectedListObject() {
		backend.deleteCategory((Category)selectedObject);
	}

  @Override
  protected boolean squareMarked(int i)
  {
    return (backend.getCategories().get(i) == backend.getDefaultCategory());
  }
	
	@Override
	protected Pair<GuiButtonObject, InputReturnCode> mouseInput(int x, int y, boolean performAction) {
		List<Category> categories = backend.getCategories();
		for (int i = 0; i < categories.size(); i++) {
			InputReturnCode rc = categories.get(i).mouseInput(x, y, performAction);
			y -= Category.HEIGHT;
			if (rc != InputReturnCode.miss)
				return new Pair<GuiButtonObject, InputReturnCode>(categories.get(i), rc);
		}
		return new Pair<GuiButtonObject, InputReturnCode>(null, InputReturnCode.miss);
	}

	@Override
	protected void onRightClick(GuiButtonObject target, MouseEvent e) {
		siblings.performAction(SiblingActions.setCategory, target);
		siblings.repaint();
		
		// edit description
		if (e.isControlDown()) {
			descriptionDialogHandler.showEditingDialogue(target);
		}
		//Set default if double click
		else if (System.currentTimeMillis() - lastRightClickTime < DOUBLE_CLICK_TIME_MILLIS) {
			backend.setDefautCategory((Category)target);
			repaint();
		}
	}
}
