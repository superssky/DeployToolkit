package com.wyb.tool.component;

import java.awt.GridBagLayout;
import java.awt.Insets;

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
import java.awt.Dimension;

/**
 * 部署面板
 * @author wyb
 *
 */
public class DeployJPanel extends JPanel {

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
		
		GBC gbc = new GBC(0,0,3,1);
		
		JPanel optionPanel = new DeployOptionPanel(this);
		add(optionPanel, gbc.setFill(GBC.BOTH).setWeightX(0.05).setWeightY(0.05));
		
		JLabel toLabel = new JLabel("更新包存放目录：");
		toLabel.setHorizontalAlignment(JTextField.RIGHT);
		add(toLabel, gbc.setGridY(1).setGridWidth(1).setWeightX(0)
				.setWeightY(0).setAnchor(GBC.EAST).setInsets(new Insets(0, 0, 5, 0)));
		
		JLabel fromLabel = new JLabel("更新文件目录：");
		fromLabel.setHorizontalAlignment(JTextField.RIGHT);
		add(fromLabel, gbc.setGridY(2));
		
		JLabel workLabel = new JLabel("系统工作目录：");
		workLabel.setHorizontalAlignment(JTextField.RIGHT);
		add(workLabel, gbc.setGridY(3));
		
		JLabel remoteLabel = new JLabel("远程更新目录：");
		remoteLabel.setHorizontalAlignment(JTextField.RIGHT);
		add(remoteLabel, gbc.setGridY(4));
		
		toField = new JTextField("");
		toField.setSize(200, 100);
//		toField.setColumns(50);
		toField.setEditable(false);
		toField.setToolTipText("更新包及更新文件的路径！");
		add(toField, gbc.setGridX(1).setGridY(1).setAnchor(GBC.WEST).setWeightX(0.01));
		
		fromField = new JTextField();
//		fromField.setColumns(50);
		fromField.setEditable(false);
		fromField.setToolTipText("获取更新文件的路径！");
		add(fromField, gbc.setGridY(2));
		
		workField = new JTextField();
//		workField.setColumns(50);
		workField.setEditable(false);
		workField.setToolTipText("工作目录 ，即代码编辑目录！");
		add(workField, gbc.setGridY(3));
		
		remoteField = new JTextField();
//		remoteField.setColumns(50);
		remoteField.setToolTipText("远程更新地址，即需要部署的远程主机目录，可以是IP+目录，也可以是本地映射远程目录！");
		add(remoteField, gbc.setGridY(4));
		
		JButton toButton = new JButton(createImageIcon("images/Open16.gif"));
		toButton.setSize(new Dimension(70,20));
		add(toButton, gbc.setGridX(2).setGridY(1).setFill(GBC.NONE)
				.setWeightX(0).setInsets(new Insets(0, 3, 5, 0)));
		toButton.addActionListener(new FileChooserListener(toField, fc, this));
		toButton.setName("toDir");
		
		JButton fromButton = new JButton(createImageIcon("images/Open16.gif"));
		fromButton.setSize(new Dimension(70,20));
		fromButton.addActionListener(new FileChooserListener(fromField, fc, this));
		fromButton.setName("fromDir");
		add(fromButton, gbc.setGridY(2));
		
		JButton workButton = new JButton(createImageIcon("images/Open16.gif"));
		workButton.setSize(new Dimension(70,20));
		add(workButton, gbc.setGridY(3));
		workButton.addActionListener(new FileChooserListener(workField, fc, this));
		workButton.setName("workDir");
		
		JButton remoteButton = new JButton(createImageIcon("images/Open16.gif"));
		remoteButton.setSize(new Dimension(70,20));
		add(remoteButton, gbc.setGridY(4));
		remoteButton.addActionListener(new FileChooserListener(remoteField, fc, this));
		remoteButton.setName("remoteDir");
		
		messagePane = new JTextArea();
		messagePane.setEditable(false);
		messagePane.setWrapStyleWord(true);
		JScrollPane paneScrollPane = new JScrollPane(messagePane);
		paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		paneScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("输出信息"),
						BorderFactory.createEmptyBorder(5,5,5,5)),
				paneScrollPane.getBorder()));
		add(paneScrollPane, gbc.setGridX(0).setGridY(6).setWeightX(0.1).setAnchor(GBC.CENTER)
				.setWeightY(0.1).setGridWidth(3).setFill(GBC.BOTH).setInsets(new Insets(5, 0, 0, 0)));
	}
	
	/**
	 * 解析配置文件
	 * @author wyb
	 * @date 2015年9月9日 下午3:52:34
	 */
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
