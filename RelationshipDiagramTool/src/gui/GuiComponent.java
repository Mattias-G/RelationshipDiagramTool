package gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public abstract class GuiComponent {
	protected static Font font;
	protected static FontMetrics fontMetrics;
	
	protected boolean isSelected;
	protected boolean isEditing;
	protected String name;
	protected int id;
	
	public abstract void draw(Graphics2D g2d);

	public int getId() {
		return id;
	}
	
	public void setSelected(boolean selected) {
		this.isSelected = selected;
	}

	public void setEditing(boolean editing) {
		this.isEditing = editing;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void keyTyped(char keyChar) {
		if (keyChar == 8 || keyChar == 127) //Backspace or delete
			delChar();
		else if (font.canDisplay(keyChar))
			name += keyChar;
	}
	
	private void delChar() {
		name = name.substring(0, Math.max(0,name.length()-1));
	}
}
