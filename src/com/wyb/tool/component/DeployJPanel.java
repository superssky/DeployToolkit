package com.wyb.tool.component;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.dom4j.Document;

import com.wyb.tool.layout.GBC;
import com.wyb.tool.listener.FileChooserListener;
import com.wyb.tool.util.Config;

public class DeployJPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5608457364242620633L;
	private JTextField toField;
	private JTextField fromField;
	private JTextField workField;
	private JFileChooser fc;
	private JTextField remoteField;
	private JTextArea messagePane;
	private Document config;
	public static final String CONFIG_FILE="deploy-config.xml";
	
	/**
	 * Create the panel.
	 * @throws Exception 
	 */
	public DeployJPanel() {
		GridBagLayout deployLayout = new GridBagLayout();
		setLayout(deployLayout);
		
		initConfig();
		
		fc = new JFileChooser();
		JLabel toLabel = new JLabel("更新包存放目录：");
		add(toLabel, new GBC(0,0,1,1).setAnchor(GBC.EAST));
		
		toField = new JTextField();
		toField.setEditable(false);
		add(toField, new GBC(1,0,1, 1).setFill(GBC.HORIZONTAL).setWeightX(0.1));
		
		JButton toButton = new JButton(createImageIcon("images/Open16.gif"));
		add(toButton, new GBC(3,0,1,1));
		toButton.addActionListener(new FileChooserListener(toField, fc, this));
		toButton.setName("toDir");
		
		JLabel fromLabel = new JLabel("更新文件目录：");
		add(fromLabel, new GBC(0,2,1,1).setAnchor(GBC.EAST));
		
		fromField = new JTextField();
		fromField.setEditable(false);
		add(fromField, new GBC(1,2,1,1).setFill(GBC.HORIZONTAL).setWeightX(0.1));
		
		JButton fromButton = new JButton(createImageIcon("images/Open16.gif"));
		add(fromButton, new GBC(3,2,1,1));
		fromButton.addActionListener(new FileChooserListener(fromField, fc, this));
		fromButton.setName("fromDir");
		
		JLabel workLabel = new JLabel("系统工作目录：");
		add(workLabel, new GBC(0,4,1,1).setAnchor(GBC.EAST));
		
		workField = new JTextField();
		workField.setEditable(false);
		add(workField, new GBC(1,4,1,1).setFill(GBC.HORIZONTAL).setWeightX(0.1));
		
		JButton workButton = new JButton(createImageIcon("images/Open16.gif"));
		add(workButton, new GBC(3,4,1,1));
		workButton.addActionListener(new FileChooserListener(workField, fc, this));
		workButton.setName("workDir");
		
		JLabel remoteLabel = new JLabel("远程更新目录：");
		add(remoteLabel, new GBC(0,6,1,1).setAnchor(GBC.EAST));
		
		remoteField = new JTextField();
		remoteField.setEditable(false);
		add(remoteField, new GBC(1,6,1,1).setFill(GBC.HORIZONTAL).setWeightX(0.1));
		
		JButton remoteButton = new JButton(createImageIcon("images/Open16.gif"));
		add(remoteButton, new GBC(3,6,1,1));
		remoteButton.addActionListener(new FileChooserListener(remoteField, fc, this));
		remoteButton.setName("remoteDir");
		
		JPanel optionPanel = new DeployOptionPanel(this);
		add(optionPanel, new GBC(0,8,8,4).setFill(GBC.BOTH).setWeightX(0.1).setWeightY(0.1));
		
		messagePane = new JTextArea();
		messagePane.setEditable(false);
		JScrollPane paneScrollPane = new JScrollPane(messagePane);
		paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		paneScrollPane.setBounds(10, 224, 585, 212);
		paneScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("输出信息"),
						BorderFactory.createEmptyBorder(5,5,5,5)),
				paneScrollPane.getBorder()));
		add(paneScrollPane, new GBC(0,13,8,4).setFill(GBC.BOTH).setWeightX(0.1).setWeightY(0.1));
	}
	
	private void initConfig() {
		try {
			config = Config.parse(CONFIG_FILE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	public ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = DeployJPanel.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			return null;
		}
	}

	public JTextField getToField() {
		return toField;
	}

	public JTextField getFromField() {
		return fromField;
	}

	public JTextField getWorkField() {
		return workField;
	}

	public JFileChooser getFc() {
		return fc;
	}

	public JTextField getRemoteField() {
		return remoteField;
	}

	public JTextArea getMessagePane() {
		return messagePane;
	}

	public Document getConfig() {
		return config;
	}
	
}
