package com.wyb.tool;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.wyb.tool.bean.DefaultListModelObject;
import com.wyb.tool.component.DeployJPanel;
import com.wyb.tool.component.PropertyDialog;
import com.wyb.tool.component.TabComponent;

/**
 * this tool is only to deploy some project by the first.
 * @author Administrator
 *
 */
public class Tool {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					getFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Tool() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		//set window size to 25% of screen and set window location to center
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		Double width = screenSize.getWidth();
		Double height = screenSize.getHeight();
		frame.setBounds(Double.valueOf(width/4).intValue(), Double.valueOf(height/4).intValue(), 
				Double.valueOf(width/2).intValue(), Double.valueOf(height/2).intValue());
		
		JMenuBar menuBar = new JMenuBar();
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu menu = new JMenu("菜单");
		menuBar.add(menu);
		
		JMenuItem exitItem = new JMenuItem("退出");
		menu.add(exitItem);
		exitItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		JMenu tool = new JMenu("工具");
		menuBar.add(tool);
		
		JMenuItem propertyItem = new JMenuItem("界面风格");
		tool.add(propertyItem);
		propertyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PropertyDialog lfDialog = new PropertyDialog(frame, "属性设置", true);
				
				addLookAndFeelInfo(UIManager.getInstalledLookAndFeels(),lfDialog);
				
				lfDialog.getOkButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						lfDialog.setVisible(false);
						SwingUtilities.updateComponentTreeUI(frame);
					}
				});
				lfDialog.setVisible(true);
			}
		});
		
		JTabbedPane tabbedPane = new JTabbedPane();
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		JPanel startPanel = new JPanel();
		tabbedPane.addTab("首页", startPanel);
		startPanel.setLayout(new GridLayout(0,2));
		
		JButton deployButton = new JButton("部署");
		startPanel.add(deployButton);
		deployButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tabbedPane.add("部署", new DeployJPanel());
				
				int index = tabbedPane.getTabCount()-1;
				tabbedPane.setTabComponentAt(index, new TabComponent(tabbedPane));
				tabbedPane.setSelectedIndex(index);
			}
		});
	}

	public void addLookAndFeelInfo(UIManager.LookAndFeelInfo [] lfInfos, Container container) {
		DefaultListModel<DefaultListModelObject<String>> listModel = 
				new DefaultListModel<DefaultListModelObject<String>>();
		for(UIManager.LookAndFeelInfo lfInfo : lfInfos) {
			listModel.addElement(
					new DefaultListModelObject<String>(lfInfo.getName(), lfInfo.getClassName()));
		}
		JList<DefaultListModelObject<String>> list = 
				new JList<DefaultListModelObject<String>>(listModel);
		list.setVisibleRowCount(8);
		container.add(list);
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				try {
					UIManager.setLookAndFeel(listModel.getElementAt(e.getFirstIndex()).getValue());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	public static JFrame getFrame() {
		return getWindow().frame;
	}
	
	private static Tool getWindow() {
		if(window == null) {
			window = new Tool();
		}
		return window;
	}
	
	private static Tool window;
	private JFrame frame;
}
