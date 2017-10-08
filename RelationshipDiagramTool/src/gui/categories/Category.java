package gui.categories;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JColorChooser;

import gui.GuiComponent;

public class Category extends GuiComponent {
	public static final int WIDTH = 160;
	public static final int HEIGHT = 48;
	private static final int TEXT_Y = 16;
	private Color color;
	private boolean visible;
	
	public Category(int id) {
		color = new Color(1,0.5f,0.5f);
		visible = true;

		this.id = id;
		name = "";
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public InputReturnCode mouseInput(int x, int y, boolean performAction) {
		if (x < 0 || x > WIDTH || y < 0 || y > HEIGHT)
			return InputReturnCode.miss;
		else if (x >= 4 && x < 16 && y >= HEIGHT - 16 && y < HEIGHT - 4) {
			if (performAction)
				visible = !visible;
			return InputReturnCode.visible;
		}
		else if (x >= 68 && x < 80 && y >= HEIGHT - 16 && y < HEIGHT - 4) {
			if (performAction) {
				Color newColor = JColorChooser.showDialog(null, "Choose a color", color);
				if (newColor != null)
				color = newColor;
			}
			return InputReturnCode.color;
		}
		if (performAction)
			isSelected = true;
		return InputReturnCode.hit;
	}

	@Override
	public void draw(Graphics2D g2d) {
		if (font == null) {
			font = g2d.getFont();
			fontMetrics = g2d.getFontMetrics();
		}
		
		if (isSelected) {
			g2d.setColor(color.brighter());
		}
		else
			g2d.setColor(color);
		g2d.fill3DRect(0, 0, WIDTH, HEIGHT, true);
		
		if (isSelected) {
			g2d.setColor(Color.WHITE);
			g2d.drawRect(0, 0, WIDTH, HEIGHT);
		}
		
		int w = fontMetrics.stringWidth(name);
		int h = fontMetrics.getHeight();
		
		g2d.setColor(Color.BLACK);
		
		if (isEditing) {
			g2d.drawString(name + "|", (WIDTH - w) / 2, TEXT_Y / 2 + fontMetrics.getAscent());
			g2d.setColor(color.darker());
			g2d.drawRect((WIDTH - w) / 2 - 2, TEXT_Y / 2 - 2, w + 4, h + 4);
		}
		else
			g2d.drawString(name, (WIDTH - w) / 2, TEXT_Y / 2 + fontMetrics.getAscent());
		
		g2d.setColor(Color.BLACK);
		g2d.drawRect(4, HEIGHT - 16, 12, 12);
		if (visible) {
			g2d.fillRect(6, HEIGHT - 14, 9, 9);
		}
		g2d.drawString("visible", 19, HEIGHT - 5);

		if (isSelected)
			g2d.setColor(color);
		else
			g2d.setColor(color.brighter());
		g2d.fillRect(68, HEIGHT - 16, 12, 12);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(68, HEIGHT - 16, 12, 12);
		g2d.drawString("color", 83, HEIGHT - 5);
	}
	
	@Override
	public String toString() {
		return id + " " + visible + " " + color.getRGB() + " " + name;
	}
	
	public String toJson() {
		return 	"{\n" +
					"\"id\": " + id + ",\n" +
					"\"visible\": " + visible + ",\n" +
					"\"color\": " + color.getRGB() + ",\n" +
					"\"name\": \"" + name + "\"\n" +
				"}";
	}
	
}
