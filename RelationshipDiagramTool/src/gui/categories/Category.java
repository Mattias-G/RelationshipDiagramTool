package gui.categories;

import java.awt.Color;
import java.awt.Graphics2D;

public class Category extends GuiButtonObject {
	public static final int WIDTH = 160;
	public static final int HEIGHT = 48;
	private boolean visible;
	
	public Category(int id) {
		super(id);
		visible = true;
	}
		
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	protected int componentWidth() {
		return WIDTH;
	}

	@Override
	protected int componentHeight() {
		return HEIGHT;
	}
	
	@Override
	protected InputReturnCode inheritedMouseInput(int x, int y, boolean performAction) {
		if (x >= 4 && x < 16 && y >= componentHeight() - 16 && y < componentHeight() - 4) {
			if (performAction)
				visible = !visible;
			return InputReturnCode.visible;
		}
		return null;
	}


	@Override
	public void drawInherited(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		g2d.drawRect(4, componentHeight() - 16, 12, 12);
		if (visible) {
			g2d.fillRect(6, componentHeight() - 14, 9, 9);
		}
		g2d.drawString("visible", 19, componentHeight() - 5);
	}
	
	@Override
	public String toString() {
		return id + " " + visible + " " + color.getRGB() + " " + name + super.toString();
	}
	
	public String toJson() {
		return 	"{\n" +
					"\"id\": " + id + ",\n" +
					"\"visible\": " + visible + ",\n" +
					"\"color\": " + color.getRGB() + ",\n" +
					"\"name\": \"" + name + "\",\n" +
					super.toJson() +
				"}";
	}
}
