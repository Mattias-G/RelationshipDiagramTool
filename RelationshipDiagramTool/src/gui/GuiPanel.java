package gui;

public interface GuiPanel {
	public void unfocus();
	public void repaint();
	void performAction(SiblingActions actionCode, Object... params);
}
