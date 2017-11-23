package gui;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class DescriptionDialogHandler {
	
	private JDialog dialog;
	private JTextArea textArea;
	private GuiComponent node;
	
	public DescriptionDialogHandler(JFrame frame) {
		createEditingDialogue(frame);
	}

	private void createEditingDialogue(JFrame frame) {
		dialog = new JDialog(frame, "Description", false);
		textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		scrollPane.setPreferredSize(new Dimension(300, 300));
		dialog.add(scrollPane);
		dialog.pack();
		dialog.setVisible(false);
		
		textArea.getDocument().addDocumentListener(new Listener());
	}
	
	public void showEditingDialogue(GuiComponent node) {
		this.node = node;
		dialog.setTitle(node.getName() + " - Description");
		textArea.setText(node.getDescription());
		
		dialog.setVisible(true);
	}
	
	private class Listener implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
				try {
					if (node != null)
						node.setDescription(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			try {
				if (node != null)
					node.setDescription(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			try {
				if (node != null)
					node.setDescription(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}
}
