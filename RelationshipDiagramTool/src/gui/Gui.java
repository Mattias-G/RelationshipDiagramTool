package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

import backend.Backend;
import utils.SaveStateHandler;

@SuppressWarnings("serial")
public class Gui extends JFrame {
	
	public Gui(Backend backend) {
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(800, 600));
		
		setLayout(new GridBagLayout());

		GraphPanel gp = addGraphPanel(backend);
		CategoryPanel cp = addCategoryPanel(backend);
		TimestampPanel tp = addTimestampPanel(backend);
		addMenuBar(backend, gp, cp);
		gp.setSiblingComponent(new GuiPanelGroup(cp, tp));
		cp.setSiblingComponent(new GuiPanelGroup(gp));
		tp.setSiblingComponent(new GuiPanelGroup(gp));
		
		setVisible(true);
		
	}

	private void addMenuBar(Backend backend, GraphPanel graphPanel, CategoryPanel categoryPanel) {
		SaveStateHandler saver = new SaveStateHandler(backend);
		JMenuBar menuBar = new MenuBar(saver, graphPanel, categoryPanel);
		setJMenuBar(menuBar);
	}
	
	private GraphPanel addGraphPanel(Backend backend) {
		GraphPanel graphPanel = new GraphPanel(backend);
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		add(graphPanel, gbc);
		graphPanel.registerKeyListener();
		return graphPanel;
	}

	private CategoryPanel addCategoryPanel(Backend backend) {
		CategoryPanel categoryPanel = new CategoryPanel(backend, this); 
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1;
		
		JScrollPane sp = new JScrollPane(categoryPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				
		add(sp, gbc);
		sp.setMinimumSize(new Dimension(categoryPanel.dw, categoryPanel.dh));
		categoryPanel.registerKeyListener();
		return categoryPanel;
	}

	private TimestampPanel addTimestampPanel(Backend backend) {
		TimestampPanel timestampPanel = new TimestampPanel(backend, this); 
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		JScrollPane sp = new JScrollPane(timestampPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(sp, gbc);
		sp.setMinimumSize(new Dimension(timestampPanel.dw, timestampPanel.dh));
		timestampPanel.registerKeyListener();
		return timestampPanel;
	}


	public static void main(String[] args) {
		Gui g = new Gui(new Backend());
		g.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
