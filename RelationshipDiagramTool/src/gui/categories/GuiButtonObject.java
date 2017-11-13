package gui.categories;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JColorChooser;

import gui.GuiComponent;

public abstract class GuiButtonObject extends GuiComponent {
	protected static final int TEXT_Y = 16;
	protected Color color;
	
	public GuiButtonObject(int id) {
		color = new Color(1,0.5f,0.5f);

		this.id = id;
		name = "";
	}

	protected abstract int componentWidth();
	protected abstract int componentHeight();
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public InputReturnCode mouseInput(int x, int y, boolean performAction) {
		if (x < 0 || x > componentWidth() || y < 0 || y > componentHeight())
			return InputReturnCode.miss;
		else if (x >= 68 && x < 80 && y >= componentHeight() - 16 && y < componentHeight() - 4) {
			if (performAction) {
				Color newColor = JColorChooser.showDialog(null, "Choose a color", color);
				if (newColor != null)
				color = newColor;
			}
			return InputReturnCode.color;
		}
		else {
			InputReturnCode rc = inheritedMouseInput(x, y, performAction);
			if (rc != null)
				return rc;
		}
		if (performAction)
			isSelected = true;
		return InputReturnCode.hit;
	}

	protected InputReturnCode inheritedMouseInput(int x, int y, boolean performAction) {
		return null;
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
		g2d.fill3DRect(0, 0, componentWidth(), componentHeight(), true);
		
		if (isSelected) {
			g2d.setColor(Color.WHITE);
			g2d.drawRect(0, 0, componentWidth(), componentHeight());
		}
		
		int w = fontMetrics.stringWidth(name);
		int h = fontMetrics.getHeight();
		
		g2d.setColor(Color.BLACK);
		
		if (isEditing) {
			g2d.drawString(name + "|", (componentWidth() - w) / 2, TEXT_Y / 2 + fontMetrics.getAscent());
			g2d.setColor(color.darker());
			g2d.drawRect((componentWidth() - w) / 2 - 2, TEXT_Y / 2 - 2, w + 4, h + 4);
		}
		else
			g2d.drawString(name, (componentWidth() - w) / 2, TEXT_Y / 2 + fontMetrics.getAscent());
		
		if (isSelected)
			g2d.setColor(color);
		else
			g2d.setColor(color.brighter());
		g2d.fillRect(68, componentHeight() - 16, 12, 12);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(68, componentHeight() - 16, 12, 12);
		g2d.drawString("color", 83, componentHeight() - 5);
		
		drawInherited(g2d);

	}

	protected void drawInherited(Graphics2D g2d) {
	}	
}
