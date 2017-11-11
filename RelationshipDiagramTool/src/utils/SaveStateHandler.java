package utils;

import java.awt.Container;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import backend.Backend;

public class SaveStateHandler {
	private Backend backend;
	private boolean recentlySaved;
	private JFileChooser fileChooser;
	private File lastUsedFile;
	
	public SaveStateHandler(Backend backend) {
		this.backend = backend;

		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Json", "json"));
	}
	
	public void reset() {
		backend.reset();
		lastUsedFile = null;
		recentlySaved = true;
	}
	
	public boolean open(Container topComponent) {
		if (fileChooser.showOpenDialog(topComponent) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			backend.load(f);
			
			lastUsedFile = f;
			recentlySaved = true;
			return true;
		}
		return false;
	}
	
	public void save(Container topComponent) {
		if (lastUsedFile == null)
			saveAs(topComponent);
		else {
			recentlySaved = true;
			backend.save(lastUsedFile);
		}
	}
	
	public void saveAs(Container topComponent) {
		if (fileChooser.showSaveDialog(topComponent) == JFileChooser.APPROVE_OPTION) {
			FileFilter fileFilter = fileChooser.getFileFilter();
			File file = fileChooser.getSelectedFile();
			if (!fileFilter.accept(file))
				file = new File(file.getAbsolutePath() + ".json");
			backend.save(file);
			
			recentlySaved = true;
			lastUsedFile = file;
		}
	}
	
	public boolean isSaved() {
		return recentlySaved;
	}
	
	public void flagEditPerformed() {
		recentlySaved = false;
	}
}