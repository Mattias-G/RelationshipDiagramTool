package gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import gui.GuiComponent;

public class Edge extends GuiComponent {
	private Node startNode;
	private Node endNode;
	
	public Edge(Node startNode, Node endNode, int id) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.id = id;
		
		name = "";
		description = "";
	}
	
	
	public void draw(Graphics2D g2d) {
		if (isVisible()) {
			if (font == null) {
				font = g2d.getFont();
				fontMetrics = g2d.getFontMetrics();
			}
			
			if (isSelected) {
				g2d.setColor(Color.LIGHT_GRAY);
				Stroke s = g2d.getStroke();
				g2d.setStroke(new BasicStroke(8));
				g2d.drawLine(startNode.getX(), startNode.getY(), endNode.getX(), endNode.getY());
				g2d.setStroke(s);
			}
			g2d.setColor(Color.BLACK);
			g2d.drawLine(startNode.getX(), startNode.getY(), endNode.getX(), endNode.getY());

			int x = startNode.getX() + (endNode.getX() - startNode.getX()) / 2;
			int y = startNode.getY() + (endNode.getY() - startNode.getY()) / 2;
			int w = Math.max(64, 24 + fontMetrics.stringWidth(name));
			int h = Math.max(24, fontMetrics.getHeight());

			if (isSelected)
				g2d.setColor(new Color(0.9f, 0.9f, 0.9f));
			else
				g2d.setColor(Color.LIGHT_GRAY);
			g2d.fill3DRect( x - w/2, y - h/2, w, h, true);
			
			if (isSelected) {
				g2d.setColor(Color.BLUE);
				g2d.drawRect(x - w/2, y - h/2, w, h);
			}
			
			w = fontMetrics.stringWidth(name);
			h = fontMetrics.getHeight();
			
			g2d.setColor(Color.BLACK);
			if (isEditing) {
				g2d.drawString(name + "|", x - w/2, y - h/2 + fontMetrics.getAscent());
				g2d.setColor(Color.DARK_GRAY);
				g2d.drawRect( x - w/2 - 2, y - h/2 - 2, w + 4, h + 4);
			}
			else
				g2d.drawString(name, x - w/2, y - h/2 + fontMetrics.getAscent());
		}
	}
	
	public boolean pointInBounds(int pointX, int pointY) {
		int x = startNode.getX() + (endNode.getX() - startNode.getX()) / 2;
		int y = startNode.getY() + (endNode.getY() - startNode.getY()) / 2;
		int w = Math.max(64, 24 + fontMetrics.stringWidth(name));
		int h = Math.max(24, fontMetrics.getHeight());
		
		if (pointX > x - w/2 && pointX < x + w/2 && 
				pointY > y - h/2 && pointY < y + h/2) {
			return true;
		}
		return false;
	}

	public Node getStartNode() {
		return startNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public boolean isVisible() {
		return startNode.getCategory().isVisible() && endNode.getCategory().isVisible();
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
			return id + " " + startNode.getId() + " " + endNode.getId() + " " + name + super.toString();
	}
	
	public String toJson() {
		return 	"{\n" +
					"\"id\": " + id + ",\n" +
					"\"node1\": " + startNode.getId() + ",\n" +
					"\"node2\": " + endNode.getId() + ",\n" +
					"\"name\": \"" + name + "\",\n" +
					super.toJson() +
				"}";
	}
}
