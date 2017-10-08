package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import backend.Backend;

@SuppressWarnings("serial")
public class Gui extends JFrame {
	
	public Gui(Backend backend) {
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(800, 600));
		
		setLayout(new GridBagLayout());

		GraphPanel gp = addGraphPanel(backend);
		CategoryPanel cp = addCategoryPanel(backend);
		addMenuBar(backend, gp, cp);
		gp.setSiblingComponent(cp);
		cp.setSiblingComponent(gp);
		
		setVisible(true);
	}

	private void addMenuBar(Backend backend, GraphPanel graphPanel, CategoryPanel categoryPanel) {
		JMenuBar menuBar = new MenuBar(backend, graphPanel, categoryPanel);  
		
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 0;
		
		add(menuBar, gbc);
	}
	
	private GraphPanel addGraphPanel(Backend backend) {
		GraphPanel graphPanel = new GraphPanel(backend);
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 1;
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
		CategoryPanel categoryPanel = new CategoryPanel(backend); 
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		
		add(categoryPanel, gbc);
		categoryPanel.registerKeyListener();
		return categoryPanel;
	}


	public static void main(String[] args) {
		Gui g = new Gui(new Backend());
		g.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
