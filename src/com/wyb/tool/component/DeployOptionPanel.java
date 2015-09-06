package com.wyb.tool.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import com.wyb.tool.Tool;
import com.wyb.tool.listener.FileChooserListener;
import com.wyb.tool.pub.UpdateTarget;
import com.wyb.tool.pub.updatefile.GitUpdateFile;
import com.wyb.tool.pub.updatefile.UpdateFile;
import com.wyb.tool.util.Config;

public class DeployOptionPanel extends JPanel {
	private static final long serialVersionUID = 5435877437844628214L;
	private JTextField exeField;
	private DeployJPanel parent;
	private JComboBox<String> ctrBox;
	private JCheckBox updateRemote;
	private JCheckBox fileGen;
	private JCheckBox updateZip;
	private JTextField projectField;
	private Map<String, String> ruleMap = new HashMap<String, String>();
	private Element base;
	
	public DeployOptionPanel(DeployJPanel parent) {
		this.parent = parent;
		setLayout(null);
		base = parent.getConfig().getRootElement();
		
		JLabel ctrLabel = new JLabel("版本管理工具：");
		ctrLabel.setBounds(10, 11, 98, 15);
		add(ctrLabel);
		
		String [] vCtr = new String[]{"Git", "SVN", "CVS"};
		ctrBox = new JComboBox(vCtr);
		ctrBox.setBounds(118, 8, 64, 21);
		add(ctrBox);
		
		JLabel exeLabel = new JLabel("路径：");
		exeLabel.setBounds(192, 11, 43, 15);
		add(exeLabel);
		
		exeField = new JTextField();
		exeField.setBounds(236, 8, 303, 21);
		exeField.setEditable(false);
		add(exeField);
		
		JButton btnNewButton = new JButton(parent.createImageIcon("images/Open16.gif"));
		btnNewButton.addActionListener(new FileChooserListener(exeField, parent.getFc(), this));
		btnNewButton.setBounds(551, 8, 49, 23);
		add(btnNewButton);
		btnNewButton.setName("controlPath");
		
		JButton exeButton = new JButton("执行");
		exeButton.setBounds(723, 7, 84, 23);
		add(exeButton);
		exeButton.addActionListener(new ExeActionListener());
		
		updateRemote = new JCheckBox("更新远程目录");
		updateRemote.setBounds(260, 35, 103, 23);
		updateRemote.setName("isUpdateRemote");
		add(updateRemote);
		
		fileGen = new JCheckBox("生成更新文件列表");
		fileGen.setBounds(6, 35, 137, 23);
		fileGen.setName("isGenUpdateFile");
		add(fileGen);
		
		updateZip = new JCheckBox("生成更新包");
		updateZip.setBounds(153, 35, 93, 23);
		updateZip.setName("isGenUpdate");
		add(updateZip);
		
		JLabel projectLabel = new JLabel("项目名称：");
		projectLabel.setBounds(10, 75, 74, 15);
		add(projectLabel);
		
		projectField = new JTextField((String) null);
		projectField.setBounds(84, 72, 162, 21);
		add(projectField);
		
		JLabel label = new JLabel("选择项目：");
		label.setBounds(387, 75, 74, 15);
		add(label);
		
		List<String> item = new ArrayList<String>();
		item.add("");
		for(Element element : base.elements("project")) {
			item.add(element.attributeValue("name"));
		}
		JComboBox<String> projectBox = new JComboBox(item.toArray());
		projectBox.setBounds(474, 72, 177, 21);
		add(projectBox);
		projectBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox)e.getSource();
				String projectName = (String)cb.getSelectedItem();
				setProjectConfig(projectName);
			}
		});
		
		JButton saveButton = new JButton("保存");
		saveButton.setToolTipText("项目名称不能重复，会将当前的配置信息保存到这个项目名称下！");
		saveButton.setBounds(261, 71, 84, 23);
		add(saveButton);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String projectName = projectField.getText();
					if(StringUtils.isBlank(projectName)) {
						setMessage("项目名称不能为空！");
						return;
					}
					boolean isAdd = false;
					Element project = (Element) base.selectSingleNode(".//project[@name='"+projectName+"']");
					if(project != null) {
						project.element("toDir").setText(parent.getToField().getText());
						project.element("fromDir").setText(parent.getFromField().getText());
						project.element("workDir").setText(parent.getWorkField().getText());
						project.element("remoteDir").setText(parent.getRemoteField().getText());
						
						project.element("control").setText((String)ctrBox.getSelectedItem());
						project.element("controlPath").setText(exeField.getText());
						
						project.element("isUpdateRemote").setText(Boolean.valueOf(updateRemote.isSelected()).toString());
						project.element("isGenUpdateFile").setText(Boolean.valueOf(fileGen.isSelected()).toString());
						project.element("isGenUpdate").setText(Boolean.valueOf(updateZip.isSelected()).toString());
						Element rules = (Element)project.selectSingleNode("rules");
						if(rules != null) {
							rules.detach();
						}
						rules = project.addElement("rules");
						for(String key : ruleMap.keySet()) {
							rules.addElement("rule").setText(key+"||"+ruleMap.get(key));
						}
					} else {
						project = base.addElement("project");
						project.addAttribute("name", projectName);
						project.addElement("toDir").setText(parent.getToField().getText());
						project.addElement("fromDir").setText(parent.getFromField().getText());
						project.addElement("workDir").setText(parent.getWorkField().getText());
						project.addElement("remoteDir").setText(parent.getRemoteField().getText());
						
						project.addElement("control").setText((String)ctrBox.getSelectedItem());
						project.addElement("controlPath").setText(exeField.getText());
						
						project.addElement("isUpdateRemote").setText(Boolean.valueOf(updateRemote.isSelected()).toString());
						project.addElement("isGenUpdateFile").setText(Boolean.valueOf(fileGen.isSelected()).toString());
						project.addElement("isGenUpdate").setText(Boolean.valueOf(updateZip.isSelected()).toString());
						
						Element rules = project.addElement("rules");
						for(String key : ruleMap.keySet()) {
							rules.addElement("rule").setText(key+"||"+ruleMap.get(key));
						}
						
						isAdd = true;
					}
					Config.write(base.getDocument(), new File(DeployJPanel.CONFIG_FILE));
					setMessage("项目配置："+projectName+"，保存成功！");
					
					if(isAdd) {
						projectBox.addItem(projectName);
						projectBox.setSelectedItem(projectName);
					}
				} catch (Exception e1) {
					setMessage(e1.getMessage());
				}
			}
		});
		
		JButton deleteButton = new JButton("删除");
		deleteButton.setToolTipText("删除选择的配置！");
		deleteButton.setBounds(675, 71, 84, 23);
		add(deleteButton);
		
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String projectName = (String)projectBox.getSelectedItem();
					if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(parent, "确定删除项目配置："+projectName+"？")) {
						Element project = (Element) base.selectSingleNode(".//project[@name='"+projectName+"']");
						if(project != null) {
							base.remove(project);
							Config.write(base.getDocument(), new File(DeployJPanel.CONFIG_FILE));
							setMessage("项目配置："+projectName+"，删除成功！");
							projectBox.removeItem(projectName);
						} else {
							setProjectConfig(null);
						}
					}
				} catch (Exception e1) {
					setMessage(e1.getMessage());
				}
		}
		});
		
		JButton ruleButton = new JButton("匹配规则");
		ruleButton.setBounds(610, 7, 96, 23);
		add(ruleButton);
		ruleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DeployRuleDialog drDialog = new DeployRuleDialog(Tool.getFrame(), "匹配规则", true);
				drDialog.setTableData(ruleMap);
				
				drDialog.getOkButton().addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ruleMap.clear();
						JTable table = drDialog.getTable();
						TableCellEditor editor = table.getCellEditor();
						if(editor != null) {
							editor.stopCellEditing();
						}
						
						for(int row = 0; row < table.getRowCount(); row++) {
							ruleMap.put((String)table.getValueAt(row, 0), (String)table.getValueAt(row, 1));
						}
						
						drDialog.setVisible(false);
					}
				});
				drDialog.setVisible(true);
			}
		});
	}
	private void setProjectConfig(String projectName) {
		Element project = (Element) base.selectSingleNode(".//project[@name='"+projectName+"']");
		if(project == null) {
			parent.getToField().setText("");
			parent.getFromField().setText("");
			parent.getWorkField().setText("");
			parent.getRemoteField().setText("");
			
			ctrBox.setSelectedItem("");
			exeField.setText("");
			updateRemote.setSelected(false);
			fileGen.setSelected(Boolean.valueOf(false));
			updateZip.setSelected(Boolean.valueOf(false));
			
			ruleMap.clear();
		} else {
			parent.getToField().setText(project.elementText("toDir"));
			parent.getFromField().setText(project.elementText("fromDir"));
			parent.getWorkField().setText(project.elementText("workDir"));
			parent.getRemoteField().setText(project.elementText("remoteDir"));
			
			ctrBox.setSelectedItem(project.elementText("control"));
			exeField.setText(project.elementText("controlPath"));
			updateRemote.setSelected(Boolean.valueOf(project.elementText("isUpdateRemote")));
			fileGen.setSelected(Boolean.valueOf(project.elementText("isGenUpdateFile")));
			updateZip.setSelected(Boolean.valueOf(project.elementText("isGenUpdate")));
			
			Element rules = project.element("rules");
			if(rules != null && !rules.elements().isEmpty()) {
				ruleMap.clear();
				for(Node node : rules.elements()) {
					String ruleText = node.getText();
					String[] rule = ruleText.split("\\|\\|");
					if(rule != null && rule.length == 2) {
						ruleMap.put(rule[0], rule[1]);
					}
				}
			}
		}
	}
	class ExeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			String ctrlVersion = ctrBox.getSelectedItem().toString();
			UpdateFile uf = null;
			Map<String, Object> attrs = new HashMap<String, Object>();
			attrs.put("toDir", parent.getToField().getText());
			attrs.put("fromDir", parent.getFromField().getText());
			attrs.put("workDir", parent.getWorkField().getText());
			attrs.put("remoteDir", parent.getRemoteField().getText());
			attrs.put("controlPath", exeField.getText());
			attrs.put("control", ctrBox.getSelectedItem());
			attrs.put("isUpdateRemote", updateRemote.isSelected());
			attrs.put("isGenUpdateFile", fileGen.isSelected());
			attrs.put("isGenUpdate", updateZip.isSelected());
			attrs.put("rules", ruleMap);
			
			if("Git".equals(ctrlVersion)) {
				try {
					uf = new GitUpdateFile(attrs);
				} catch (URISyntaxException e1) {
					parent.getMessagePane().append(e1.getMessage());
				}
			} else {
				setMessage("您选择的是："+ctrlVersion+"，暂只支持Git方式！");
				return;
			}
			
			UpdateTarget t;
			t = new UpdateTarget(uf);
			
			t.execute();

			BufferedReader inputStream = null;
			try {
				inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(t.getErrorFile()), "UTF-8"));
				String l;
				while ((l = inputStream.readLine()) != null) {
					setMessage(l);
				}
			} catch (IOException e1) {
				setMessage(e1.getMessage());
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e1) {
						setMessage(e1.getMessage());
					}
				}
			}
		}
		
	}
	
	public void setMessage(String msg) {
		parent.getMessagePane().append(msg + "\n");
	}
	
	public void setMessage(String[] msgs) {
		if(msgs != null) {
			for(String msg : msgs) {
				setMessage(msg);
			}
		}
	}
}
