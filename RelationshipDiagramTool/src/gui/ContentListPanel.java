package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import backend.Backend;
import gui.categories.Category;
import gui.categories.GuiButtonObject;
import gui.graph.Timestamp;

@SuppressWarnings("serial")
public abstract class ContentListPanel extends JPanel implements GuiPanel {
	public static final double DOUBLE_CLICK_TIME_MILLIS = 500;
	protected Color bkgColor;
	
	protected Backend backend;
	protected GuiPanelGroup siblings;
	
	protected boolean editing;
	protected boolean dragging;
	
	protected long lastLeftClickTime;
	protected long lastRightClickTime;
	protected int dw;
	protected int dh;
	
	public ContentListPanel(Backend backend) {		
		this.backend = backend;

		setBorder(BorderFactory.createEtchedBorder());
	}
	
	public void setSiblingComponent(GuiPanelGroup siblings) {
		this.siblings = siblings;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g; 
		
		g2d.setColor(bkgColor);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		List<? extends GuiButtonObject> content = componentsToDraw();
		for (int i = 0; i < content.size(); i++) {
			content.get(i).draw(g2d);
			if (i == backend.getCurrentTimestampIndex()) {
				g2d.setColor(content.get(i).getColor().darker());
				g2d.fillRect(1, 1, 8, 8);
			}

			g2d.translate(dw,dh);
		}

		Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{3}, 0);
		Stroke original = g2d.getStroke();
		
		g2d.setStroke(dashed);
		g2d.setColor(bkgColor.darker());
		g2d.drawRect(2, 0, Category.WIDTH-4, Category.HEIGHT-4);
		g2d.setStroke(original);
		g2d.fillRect(Category.WIDTH/2-Category.HEIGHT/6, Category.HEIGHT/2-1, Category.HEIGHT/3, 2);
		g2d.fillRect(Category.WIDTH/2-1, Category.HEIGHT/3, 2, Category.HEIGHT/3);

		g2d.translate(-dw * content.size(), -dh * content.size());
	}

	protected abstract List<? extends GuiButtonObject> componentsToDraw();


	@Override
	public void performAction(SiblingActions actionCode, Object... params) {
		
	}
}
