package com.wyb.tool.listener;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;

public class FileChooserListener implements ActionListener {

	private JTextComponent tc;
	private JFileChooser fc;
	private Component parent;
	
	public FileChooserListener(JTextComponent tc, JFileChooser fc, Component parent) {
		this.tc = tc;
		this.fc = fc;
		this.parent = parent;
	}
	
	public void actionPerformed(ActionEvent e) {
		String path = tc.getText();
		File oldFile = null;
		try{
			if(!StringUtils.isBlank(path)) {
				oldFile = new File(path);
			} else {
				oldFile = fc.getFileSystemView().getDefaultDirectory();
			}
		} catch (NullPointerException ex) {
			oldFile = fc.getFileSystemView().getDefaultDirectory();
		}
		fc.setCurrentDirectory(oldFile);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				tc.setText(file.getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if(returnVal == JFileChooser.ERROR_OPTION) {
			System.err.println("some error occurred when select file!");
		}
	}

}
