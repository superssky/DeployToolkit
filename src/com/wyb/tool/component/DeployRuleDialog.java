package com.wyb.tool.component;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.wyb.tool.bean.Location;
import com.wyb.tool.layout.GBC;

public class DeployRuleDialog extends JDialog {

	private static final long serialVersionUID = -7096261810425854943L;
	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private JButton okButton;
	private int defaultWidth = 450;
	private int defaultHeight = 350;
	private DefaultTableModel model = new DefaultTableModel();

	public DeployRuleDialog(Frame owner, String title, boolean model) {
		super(owner, title, model);
		init();
	}
	
	private void init() {
		setSize(defaultWidth, defaultHeight);
		
		setLocation(getParent());
//		setBounds(100, 100, 508, 346);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(500, 100));
		table.setFillsViewportHeight(true);
		model.setColumnIdentifiers(new String[]{"源路径", "部署路径"});
		
		JScrollPane scrollPane = new JScrollPane(table);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		contentPanel.add(buttonPanel, BorderLayout.EAST);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		buttonPanel.setLayout(gbl_buttonPanel);
		
		JButton addButton = new JButton("添加");
		buttonPanel.add(addButton, new GBC(0,0,1,1));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.insertRow(0, new String[]{"",""});
			}
		});
		
		JButton deleteButton = new JButton("删除");
		buttonPanel.add(deleteButton, new GBC(0,1,1,1,new Insets(10, 0, 0, 0)));
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow() >= 0) {
					model.removeRow(table.getSelectedRow());
				}
			}
		});
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("确定");
				buttonPane.add(okButton);

				JButton cancelButton = new JButton("取消");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DeployRuleDialog.this.setVisible(false);
					}
				});

				getRootPane().setDefaultButton(okButton);
			}
		}
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
	
	public JTable getTable() {
		return table;
	}

	public void setTableData(Map<String, String> ruleMap) {
		if(ruleMap != null) {
			for(String key : ruleMap.keySet()) {
				model.insertRow(0, new String[]{key, ruleMap.get(key)});
			}
		}
	}
}
