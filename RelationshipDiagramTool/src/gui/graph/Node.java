package gui.graph;

import java.awt.Color;
import java.awt.Graphics2D;

import gui.GuiComponent;
import gui.categories.Category;

public class Node extends GuiComponent {
	private int x;
	private int y;
	private int width;
	private int height;
	private Category category;
	
	public Node(int x, int y, Category c, int id) {
		width = 64;
		height = 32;
		this.x = x - width / 2;
		this.y = y - height / 2;

		name = "";
		description = "";
		
		category = c;
		
		this.id = id;
	}
	
	
	public Node(Node original, int id) {
		width = 64;
		height = 32;
		category = original.category;
		x = original.x;
		y = original.y;
		name = original.name;
		
		this.id = id;
	}


	public void draw(Graphics2D g2d) {
		if (isVisible()) {
			if (font == null) {
				font = g2d.getFont();
				fontMetrics = g2d.getFontMetrics();
			}
			
			if (isSelected) {
				g2d.setColor(category.getColor().brighter());
			}
			else
				g2d.setColor(category.getColor());
			g2d.fill3DRect(x, y, width, height, true);
			
			if (isSelected) {
				g2d.setColor(Color.BLUE);
				g2d.drawRect(x, y, width, height);
			}
			
			int w = fontMetrics.stringWidth(name);
			int h = fontMetrics.getHeight();
			
			g2d.setColor(Color.BLACK);
			
			if (isEditing) {
				g2d.drawString(name + "|", x + (width - w) / 2, y + (height - h) / 2 + fontMetrics.getAscent());
				g2d.setColor(category.getColor().darker());
				g2d.drawRect( x + (width - w) / 2 - 2, y + (height - h) / 2 - 2, w + 4, h + 4);
			}
			else
				g2d.drawString(name, x + (width - w) / 2, y + (height - h) / 2 + fontMetrics.getAscent());
			
		}
	}
	
	public boolean pointInBounds(int pointX, int pointY) {
		if (pointX > x && pointX < x + width && pointY > y && pointY < y + height) {
			return true;
		}
		return false;
	}

	public boolean coveredByArea(int x1, int y1, int x2, int y2) {
		return x1 <= x && y1 <= y && x2 > x + width && y2 > y + height; 
	}

	public void setPosition(int x2, int y2) {
		x = x2 - width/2;
		y = y2 - height/2;
	}

	public int getX() {
		return x + width/2;
	}
	
	public int getY() {
		return y + height/2;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Category getCategory() {
		return category;
	}

	@Override
	public void keyTyped(char keyChar) {
		super.keyTyped(keyChar);
		width = Math.max(64, 24 + fontMetrics.stringWidth(name));
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		width = Math.max(64, 24 + fontMetrics.stringWidth(name));
	}

	public boolean isVisible() {
		return category != null && category.isVisible();
	}
	
	@Override
	public String toString() {
		return id + " " + x + " " + y + " " + category.getId() + " " + name + super.toString();
	}
	
	public String toJson() {
		return 	"{\n" +
					"\"id\": " + id + ",\n" +
					"\"x\": " + x + ",\n" +
					"\"y\": " + y + ",\n" +
					"\"category\": " + category.getId() + ",\n" +
					"\"name\": \"" + name + "\",\n" +
					super.toJson() +
				"}";
	}
}
