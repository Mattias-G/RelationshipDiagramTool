package gui;

public interface GuiPanel {
	public void unfocus();
	public void repaint();
  public boolean isEditing();
	void performAction(SiblingActions actionCode, Object... params);
}
