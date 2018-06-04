package gui.graph;

import java.awt.Color;

import gui.GuiComponent;

public abstract class GraphComponent extends GuiComponent {
	protected final Color hoverHighlightColor = new Color(255, 255, 255, 50);
	
	protected boolean isHoveredOn = false;
	
	public GraphComponent() {
		description = "";
		name = "";
	}
	
	public void toggleMouseHover(boolean hover) {
		isHoveredOn = hover;		
	}
}
