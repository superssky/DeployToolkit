package com.wyb.tool.component;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.wyb.tool.bean.Location;

public class PropertyDialog extends JDialog{
	
	private static final long serialVersionUID = -4329994191385091419L;

	public PropertyDialog(Frame owner, String title, boolean model) {
		super(owner, title, model);
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		setSize(defaultWidth, defaultHeight);
		
		setLocation(getParent());
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("确定");
		buttonPanel.add(okButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void setLocation(Container owner, int x, int y) {
		setLocation(owner.getX()+x, owner.getY()+y);
	}
	
	public void setLocation(Container owner) {
		setLocation(Location.CENTER.getLoaction(owner.getLocation(), 
				owner.getWidth(), owner.getHeight(), getWidth(), getHeight()));
	}
	
	public JButton getOkButton() {
		return okButton;
	}
	
	private int defaultWidth = 250;
	private int defaultHeight = 250;
	private JButton okButton;
}
