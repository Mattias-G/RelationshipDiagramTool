package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import utils.SaveStateHandler;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private SaveStateHandler saver;

	private JMenuItem newItem;
	private JMenuItem openItem;
	private JMenuItem saveItem;
	private JMenuItem saveAsItem;

	private GuiPanel graphPanel;
	private GuiPanel categoryPanel;
	
	public MenuBar(SaveStateHandler saver, GuiPanel p1, GuiPanel p2) {
		this.saver = saver;
		this.graphPanel = p1;
		this.categoryPanel = p2;
		
		JMenu fileMenu = new JMenu("File");

		newItem = new JMenuItem("New");
		openItem = new JMenuItem("Open");
		saveItem = new JMenuItem("Save");
		saveAsItem = new JMenuItem("Save As");

		newItem.addActionListener(new Listener());
		openItem.addActionListener(new Listener());
		saveItem.addActionListener(new Listener());
		saveAsItem.addActionListener(new Listener());
		
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.addSeparator();
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		add(fileMenu);
	}
	
	
	private class Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == newItem) {
				saver.reset();
				graphPanel.repaint();
				categoryPanel.repaint();
			}
			else if (e.getSource() == openItem) {
				if (saver.open(getTopLevelAncestor())) {
					graphPanel.repaint();
					categoryPanel.repaint();
				}
			}
			else if (e.getSource() == saveItem) {
				saver.save(getTopLevelAncestor());
			}
			else if (e.getSource() == saveAsItem) {
				saver.saveAs(getTopLevelAncestor());
			}
		}
	}
}
