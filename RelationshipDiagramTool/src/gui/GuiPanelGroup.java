package gui;

import java.util.ArrayList;
import java.util.List;

import gui.misc.SiblingActions;

public class GuiPanelGroup {
	private List<GuiPanel> panels = new ArrayList<>();
	
	public GuiPanelGroup(GuiPanel... panels) {
		for (GuiPanel guiPanel : panels) {
			this.panels.add(guiPanel);
		}
	}

	public void unfocus() {
		for (GuiPanel guiPanel : panels) {
			guiPanel.unfocus();
		}
		
	}

	public void repaint() {
		for (GuiPanel guiPanel : panels) {
			guiPanel.repaint();
		}
	}

	public void performAction(SiblingActions actionCode, Object... params) {
		for (GuiPanel guiPanel : panels) {
			guiPanel.performAction(actionCode, params);
		}
	}
	
	public boolean anyoneEditing() {
    for (GuiPanel guiPanel : panels) {
      if (guiPanel.isEditing())
        return true;
    }
    return false;
	}
	
}
