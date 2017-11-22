package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;

import backend.Backend;
import gui.categories.GuiButtonObject;
import gui.categories.InputReturnCode;
import gui.graph.Timestamp;
import utils.Pair;

@SuppressWarnings("serial")
public class TimestampPanel extends ContentListPanel {	
	
	public TimestampPanel(Backend backend, JFrame frame) {	
		super(backend, frame);
		this.bkgColor = new Color(0.8f, 1.0f, 0.8f);

		setMinimumSize(new Dimension(0, 48));
		setPreferredSize(new Dimension(0, 48));

		dh = Timestamp.HEIGHT;
		dw = Timestamp.WIDTH;
		expandVertically = false;
	}

	@Override
	protected List<? extends GuiButtonObject> componentsToDraw() {
		return backend.getTimestamps();
	}

	@Override
	protected GuiButtonObject createNewListObject() {
		Timestamp timestamp = backend.createTimestamp();
		backend.makeCurrentTimestamp(timestamp);
		siblings.repaint();
		return timestamp;
	}
	
	@Override
	protected void deleteSelectedListObject() {
		backend.deleteTimestamp((Timestamp)selectedObject);
	}

	@Override
	protected boolean squareMarked(int i) {
		return (i == backend.getCurrentTimestampIndex());
	}
  
@Override
protected Pair<GuiButtonObject, InputReturnCode> mouseInput(int x, int y, boolean performAction) {
		List<Timestamp> timestamps = backend.getTimestamps();
		for (int i = 0; i < timestamps.size(); i++) {
			InputReturnCode rc = timestamps.get(i).mouseInput(x, y, performAction);
			x -= Timestamp.WIDTH;
			if (rc != InputReturnCode.miss)
				return new Pair<GuiButtonObject, InputReturnCode>(timestamps.get(i), rc);
		}
		return new Pair<GuiButtonObject, InputReturnCode>(null, InputReturnCode.miss);
	}

	@Override
	protected void onRightClick(GuiButtonObject target) {
		//Set current
			backend.makeCurrentTimestamp((Timestamp) target);
			siblings.repaint();
			siblings.unfocus();
			repaint();
	}

}
