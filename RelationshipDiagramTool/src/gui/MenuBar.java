package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import backend.Backend;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private Backend backend;

	private JMenuItem newItem;
	private JMenuItem openItem;
	private JMenuItem saveItem;
	private JMenuItem saveAsItem;
	
	private JFileChooser fileChooser;

	private GuiPanel graphPanel;
	private GuiPanel categoryPanel;
	
	public MenuBar(Backend backend, GuiPanel p1, GuiPanel p2) {
		this.backend = backend;
		this.graphPanel = p1;
		this.categoryPanel = p2;
		
		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Json", "json"));
		
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
				backend.reset();
				graphPanel.repaint();
				categoryPanel.repaint();
			}
			else if (e.getSource() == openItem) {
				if (fileChooser.showOpenDialog(getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
					backend.load(fileChooser.getSelectedFile());
					graphPanel.repaint();
					categoryPanel.repaint();
				}
			}
			else if (e.getSource() == saveItem || e.getSource() == saveAsItem) {
				if (fileChooser.showSaveDialog(getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
					FileFilter fileFilter = fileChooser.getFileFilter();
					File file = fileChooser.getSelectedFile();
					if (!fileFilter.accept(file))
						file = new File(file.getAbsolutePath() + ".json");
					backend.save(file);
				}
			}
		}
	}
}
