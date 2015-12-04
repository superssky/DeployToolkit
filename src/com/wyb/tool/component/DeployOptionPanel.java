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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.dom4j.Element;
import org.dom4j.Node;

import com.wyb.tool.Tool;
import com.wyb.tool.listener.FileChooserListener;
import com.wyb.tool.pub.UpdateTarget;
import com.wyb.tool.pub.updatefile.CVSUpdateFile;
import com.wyb.tool.pub.updatefile.GitUpdateFile;
import com.wyb.tool.pub.updatefile.LocalUpdateFile;
import com.wyb.tool.pub.updatefile.UpdateFile;
import com.wyb.tool.util.Config;

public class DeployOptionPanel extends JPanel {
	private final Logger logger = LogManager.getLogger(DeployOptionPanel.class);
	private static final long serialVersionUID = 5435877437844628214L;
	private JTextField controlPath;
	private JTextField localPath;
	private JCheckBox compareTime;
	private JCheckBox compareLength;
	private JCheckBox updateLocal;
	private DeployJPanel parent;
	private JComboBox<String> ctrBox;
	private JCheckBox updateRemote;
	private JCheckBox fileDelete;
	private JCheckBox updateZip;
	private JTextField projectField;
	private Map<String, String> ruleMap = new HashMap<String, String>();
	private Element base;
	private JTextField encodeField;
	private JPasswordField passwordField;
	private JTextField ignoreFile;
	private JCheckBox updateZipWas;
	private JTextField moduleNameField;
	private JTextField gitCmdField;
	
	public DeployOptionPanel(final DeployJPanel parent) {
		this.parent = parent;
		setLayout(null);
		base = parent.getConfig().getRootElement();
		
		JLabel label = new JLabel("选择项目：");
		label.setHorizontalAlignment(JTextField.RIGHT);
		label.setBounds(10, 10, 95, 20);
		add(label);
		
		List<String> item = new ArrayList<String>();
		item.add("");
		for(Element element : base.elements("project")) {
			item.add(element.attributeValue("name"));
		}
		final JComboBox<String> projectBox = new JComboBox(item.toArray());
		projectBox.setBounds(110, 10, 162, 20);
		add(projectBox);
		projectBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox)e.getSource();
				String projectName = (String)cb.getSelectedItem();
				projectField.setText(projectName);
				//项目变更，修改页面配置信息
				setProjectConfig(projectName);
			}
		});
		
		JButton deleteButton = new JButton("删除");
		deleteButton.setToolTipText("删除选择的配置！");
		deleteButton.setBounds(300, 10, 84, 20);
		add(deleteButton);
		
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//如果项目配置信息存在，则从配置文件中删除
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
		
		JButton exeButton = new JButton("执行");
		exeButton.setBounds(406, 10, 84, 20);
		exeButton.setToolTipText("执行操作，取更新文件，根据选项，更新远程目录！");
		add(exeButton);
		exeButton.addActionListener(new ExeActionListener());
		
		JLabel projectLabel = new JLabel("项目名称：");
		projectLabel.setBounds(504, 10, 74, 20);
		add(projectLabel);
		
		projectField = new JTextField((String) null);
		projectField.setBounds(577, 10, 162, 20);
		add(projectField);
		
		JButton saveButton = new JButton("保存");
		saveButton.setToolTipText("项目名称不能重复，会将当前的配置信息保存到这个项目名称下！");
		saveButton.setBounds(749, 10, 84, 20);
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
					//已经存在的项目配置，则更新
					if(project != null) {
						project.element("toDir").setText(parent.getToField().getText());
						project.element("fromDir").setText(parent.getFromField().getText());
						project.element("workDir").setText(parent.getWorkField().getText());
						project.element("remoteDir").setText(parent.getRemoteField().getText());
						
						project.element("control").setText((String)ctrBox.getSelectedItem());
						project.element("controlPath").setText(controlPath.getText());
						project.element("gitCmd").setText(gitCmdField.getText());
						project.element("encode").setText(encodeField.getText());
						project.element("password").setText(new String(passwordField.getPassword()));
						project.element("ignoreFile").setText(ignoreFile.getText());
						project.element("localPath").setText(localPath.getText());
						project.element("moduleName").setText(moduleNameField.getText());

						project.element("updateLocal").setText(Boolean.valueOf(updateLocal.isSelected()).toString());
						project.element("compareLength").setText(Boolean.valueOf(compareLength.isSelected()).toString());
						project.element("compareTime").setText(Boolean.valueOf(compareTime.isSelected()).toString());
						project.element("isUpdateRemote").setText(Boolean.valueOf(updateRemote.isSelected()).toString());
						project.element("isDeleteFile").setText(Boolean.valueOf(fileDelete.isSelected()).toString());
						project.element("isGenUpdate").setText(Boolean.valueOf(updateZip.isSelected()).toString());
						project.element("isGenUpdateWAS").setText(Boolean.valueOf(updateZipWas.isSelected()).toString());
						Element rules = (Element)project.selectSingleNode("rules");
						if(rules != null) {
							rules.detach();
						}
						rules = project.addElement("rules");
						for(String key : ruleMap.keySet()) {
							rules.addElement("rule").setText(key+"||"+ruleMap.get(key));
						}
					} else {
						//不存在此项目名称配置，新增
						project = base.addElement("project");
						project.addAttribute("name", projectName);
						project.addElement("toDir").setText(parent.getToField().getText());
						project.addElement("fromDir").setText(parent.getFromField().getText());
						project.addElement("workDir").setText(parent.getWorkField().getText());
						project.addElement("remoteDir").setText(parent.getRemoteField().getText());
						
						project.addElement("control").setText((String)ctrBox.getSelectedItem());
						project.addElement("controlPath").setText(controlPath.getText());
						project.addElement("gitCmd").setText(gitCmdField.getText());
						project.addElement("encode").setText(encodeField.getText());
						project.addElement("password").setText(new String(passwordField.getPassword()));
						project.addElement("ignoreFile").setText(ignoreFile.getText());
						project.addElement("localPath").setText(localPath.getText());
						project.addElement("moduleName").setText(moduleNameField.getText());

						project.addElement("updateLocal").setText(Boolean.valueOf(updateLocal.isSelected()).toString());
						project.addElement("compareLength").setText(Boolean.valueOf(compareLength.isSelected()).toString());
						project.addElement("compareTime").setText(Boolean.valueOf(compareTime.isSelected()).toString());
						project.addElement("isUpdateRemote").setText(Boolean.valueOf(updateRemote.isSelected()).toString());
						project.addElement("isDeleteFile").setText(Boolean.valueOf(fileDelete.isSelected()).toString());
						project.addElement("isGenUpdate").setText(Boolean.valueOf(updateZip.isSelected()).toString());
						project.addElement("isGenUpdateWAS").setText(Boolean.valueOf(updateZipWas.isSelected()).toString());
						
						Element rules = project.addElement("rules");
						for(String key : ruleMap.keySet()) {
							rules.addElement("rule").setText(key+"||"+ruleMap.get(key));
						}
						
						isAdd = true;
					}
					//将项目配置写入文件
					Config.write(base.getDocument(), new File(DeployJPanel.CONFIG_FILE));
					setMessage("项目配置："+projectName+"，保存成功！");
					
					if(isAdd) {
						projectBox.addItem(projectName);
						projectBox.setSelectedItem(projectName);
					}
				} catch (Exception e1) {
					logger.catching(e1);
					setMessage(e1.getMessage());
				}
			}
		});
		
		final JLabel gitPath = new JLabel("Git安装路径：");
		gitPath.setHorizontalAlignment(JTextField.RIGHT);
		gitPath.setVisible(false);
		gitPath.setBounds(10, 60, 95, 20);
		add(gitPath);
		
		controlPath = new JTextField();
		controlPath.setVisible(false);
		controlPath.setBounds(110, 60, 239, 20);
		controlPath.setEditable(false);
		add(controlPath);
		
		final JButton gitButton = new JButton(parent.createImageIcon("images/Open16.gif"));
		gitButton.setVisible(false);
		gitButton.addActionListener(new FileChooserListener(controlPath, this.parent.getFc(), this));
		gitButton.setBounds(359, 60, 49, 20);
		add(gitButton);
		gitButton.setName("controlPath");
		
		final JLabel password = new JLabel("密码：");
		password.setHorizontalAlignment(JTextField.RIGHT);
		password.setVisible(false);
		
		final JLabel gitCmdLbl = new JLabel("git命令：");
		gitCmdLbl.setBounds(416, 60, 54, 20);
		add(gitCmdLbl);
		
		gitCmdField = new JTextField();
		gitCmdField.setBounds(480, 60, 353, 20);
		add(gitCmdField);
		gitCmdField.setColumns(10);
		password.setBounds(10, 60, 95, 20);
		add(password);
		
		passwordField = new JPasswordField();
		passwordField.setVisible(false);
		passwordField.setBounds(110, 60, 100, 20);
		add(passwordField);
		passwordField.setToolTipText("暂时为CVS用户密码！");
		
		final JLabel encodeLabel = new JLabel("字符集：");
		encodeLabel.setHorizontalAlignment(JTextField.RIGHT);
		encodeLabel.setVisible(false);
		encodeLabel.setBounds(230, 60, 60, 20);
		add(encodeLabel);
		
		encodeField = new JTextField("GBK");
		encodeField.setVisible(false);
		encodeField.setBounds(300, 60, 49, 20);
		add(encodeField);
		encodeField.setToolTipText("默认GBK，暂时只针对CVS!");
		
		final JLabel ignoreLabel = new JLabel("忽略文件：");
		ignoreLabel.setVisible(false);
		ignoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		ignoreLabel.setBounds(340, 60, 85, 20);
		add(ignoreLabel);
		
		ignoreFile = new JTextField((String) null);
		ignoreFile.setVisible(false);
		ignoreFile.setBounds(435, 60, 398, 20);
		add(ignoreFile);
		
		final JLabel localLabel = new JLabel("本地比对路径：");
		localLabel.setHorizontalAlignment(JTextField.RIGHT);
		localLabel.setVisible(false);
		localLabel.setBounds(10, 60, 95, 20);
		add(localLabel);
		
		localPath = new JTextField();
		localPath.setVisible(false);
		localPath.setBounds(110, 60, 360, 20);
		localPath.setEditable(false);
		add(localPath);
		
		final JButton localButton = new JButton(parent.createImageIcon("images/Open16.gif"));
		localButton.setVisible(false);
		localButton.addActionListener(new FileChooserListener(localPath, parent.getFc(), this));
		localButton.setBounds(495, 60, 49, 20);
		add(localButton);
		localButton.setName("localPath");
		
		compareTime = new JCheckBox("比对时间");
		compareTime.setVisible(false);
		compareTime.setBounds(558, 60, 86, 20);
		add(compareTime);
		
		compareLength = new JCheckBox("比对大小");
		compareLength.setVisible(false);
		compareLength.setBounds(646, 60, 93, 20);
		add(compareLength);
		
		updateLocal = new JCheckBox("更新本地");
		updateLocal.setVisible(false);
		updateLocal.setBounds(740, 60, 93, 20);
		add(updateLocal);
		
		JLabel ctrLabel = new JLabel("版本管理工具：");
		ctrLabel.setHorizontalAlignment(JTextField.RIGHT);
		ctrLabel.setBounds(10, 35, 95, 20);
		add(ctrLabel);
		
		String [] vCtr = new String[]{"Local", "Git", "SVN", "CVS"};
		ctrBox = new JComboBox(vCtr);
		ctrBox.setBounds(110, 35, 64, 20);
		add(ctrBox);
		ctrBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox)e.getSource();
				String ctrType = (String)cb.getSelectedItem();
				if("Git".equals(ctrType)) {
					setGitVisible(true, gitPath, gitButton, gitCmdLbl);
					
					setCvsVisible(false, password, encodeLabel, ignoreLabel);
					setLocalVisibel(false, localLabel, localButton);
				} else if("CVS".equals(ctrType)) {
					setCvsVisible(true, password, encodeLabel, ignoreLabel);

					setGitVisible(false, gitPath, gitButton);
					setLocalVisibel(false, localLabel, localButton);
				} else if("Local".equals(ctrType)) {
					setLocalVisibel(true, localLabel, localButton);

					setCvsVisible(false, password, encodeLabel, ignoreLabel);
					setGitVisible(false, gitPath, gitButton);
				} else {
					setLocalVisibel(false, localLabel, localButton);
					setCvsVisible(false, password, encodeLabel, ignoreLabel);
					setGitVisible(false, gitPath, gitButton);}
			}
		});
		ctrBox.setSelectedIndex(0);
		
		updateRemote = new JCheckBox("更新远程目录");
		updateRemote.setBounds(560, 35, 103, 20);
		updateRemote.setName("isUpdateRemote");
		add(updateRemote);
		
		fileDelete = new JCheckBox("删除更新文件列表");
		fileDelete.setBounds(193, 35, 135, 20);
		fileDelete.setName("isDeleteFile");
		add(fileDelete);
		
		updateZip = new JCheckBox("生成更新包");
		updateZip.setBounds(332, 35, 93, 20);
		updateZip.setName("isGenUpdate");
		add(updateZip);
		
		updateZipWas = new JCheckBox("生成WAS更新包");
		updateZipWas.setName("isGenUpdateWAS");
		updateZipWas.setBounds(435, 35, 118, 20);
		add(updateZipWas);
		moduleNameField = new JTextField();
		updateZipWas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//生成WAS更新包，需要模块名称
					if(updateZipWas.isSelected()) {
						moduleNameField.setText(JOptionPane.showInputDialog(new JTextField(moduleNameField.getText()), "模块名称"));
					}
				} catch (Exception e1) {
					setMessage(e1.getMessage());
				}
			}
		});
		
		JButton ruleButton = new JButton("匹配规则");
		ruleButton.setToolTipText("通过版本管理工具取得修改的文件，是源代码文件，需要与部署后的路径对应！");
		ruleButton.setBounds(705, 35, 96, 20);
		add(ruleButton);
		ruleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final DeployRuleDialog drDialog = new DeployRuleDialog(Tool.getFrame(), "匹配规则", true);
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
	private void setCvsVisible(boolean visible, JComponent ... args) {
		passwordField.setVisible(visible);
		encodeField.setVisible(visible);
		ignoreFile.setVisible(visible);
		for(JComponent c : args) {
			c.setVisible(visible);
		}
	}
	private void setGitVisible(boolean visible, JComponent ... args) {
		controlPath.setVisible(visible);
		gitCmdField.setVisible(visible);
		for(JComponent c : args) {
			c.setVisible(visible);
		}
	}
	private void setLocalVisibel(boolean visible, JComponent ... args) {
		localPath.setVisible(visible);
		compareLength.setVisible(visible);
		compareTime.setVisible(visible);
		updateLocal.setVisible(visible);
		for(JComponent c : args) {
			c.setVisible(visible);
		}
	}
	/**
	 * 更新页面配置信息，如果项目已经存在，则取存在的项目配置，如果不存在，则清空
	 * @author wyb
	 * @date 2015年9月9日 下午4:16:48
	 * @param projectName
	 */
	private void setProjectConfig(String projectName) {
		Element project = (Element) base.selectSingleNode(".//project[@name='"+projectName+"']");
		if(project == null) {
			parent.getToField().setText("");
			parent.getFromField().setText("");
			parent.getWorkField().setText("");
			parent.getRemoteField().setText("");
			
			ctrBox.setSelectedItem("");
			controlPath.setText("");
			gitCmdField.setText("git status -s");
			encodeField.setText("BGK");
			passwordField.setText("");
			ignoreFile.setText("");
			localPath.setText("");
			moduleNameField.setText("");

			updateLocal.setSelected(false);
			compareLength.setSelected(false);
			compareTime.setSelected(Boolean.valueOf(false));
			updateRemote.setSelected(false);
			fileDelete.setSelected(Boolean.valueOf(false));
			updateZip.setSelected(Boolean.valueOf(false));
			updateZipWas.setSelected(Boolean.valueOf(false));
			
			ruleMap.clear();
		} else {
			parent.getToField().setText(project.elementText("toDir"));
			parent.getFromField().setText(project.elementText("fromDir"));
			parent.getWorkField().setText(project.elementText("workDir"));
			parent.getRemoteField().setText(project.elementText("remoteDir"));
			
			ctrBox.setSelectedItem(project.elementText("control"));
			controlPath.setText(project.elementText("controlPath"));
			gitCmdField.setText(project.elementText("gitCmd"));
			encodeField.setText(project.elementText("encode"));
			passwordField.setText(project.elementText("password"));
			ignoreFile.setText(project.elementText("ignoreFile"));
			localPath.setText(project.elementText("localPath"));
			moduleNameField.setText(project.elementText("moduleName"));

			updateLocal.setSelected(Boolean.valueOf(project.elementText("updateLocal")));
			compareLength.setSelected(Boolean.valueOf(project.elementText("compareLength")));
			compareTime.setSelected(Boolean.valueOf(project.elementText("compareTime")));
			updateRemote.setSelected(Boolean.valueOf(project.elementText("isUpdateRemote")));
			fileDelete.setSelected(Boolean.valueOf(project.elementText("isDeleteFile")));
			updateZip.setSelected(Boolean.valueOf(project.elementText("isGenUpdate")));
			updateZipWas.setSelected(Boolean.valueOf(project.elementText("isGenUpdateWAS")));
			
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
			logger.entry();
			
			String ctrlVersion = ctrBox.getSelectedItem().toString();
			UpdateFile uf = null;
			Map<String, Object> attrs = new HashMap<String, Object>();
			attrs.put("toDir", parent.getToField().getText());
			attrs.put("fromDir", parent.getFromField().getText());
			attrs.put("workDir", parent.getWorkField().getText());
			attrs.put("remoteDir", parent.getRemoteField().getText());
			attrs.put("controlPath", controlPath.getText());
			attrs.put("gitCmd", gitCmdField.getText());
			attrs.put("control", ctrBox.getSelectedItem());
			attrs.put("password", new String(passwordField.getPassword()));
			attrs.put("encode", encodeField.getText());
			attrs.put("ignoreFile", ignoreFile.getText());
			attrs.put("localPath", localPath.getText());
			attrs.put("isUpdateRemote", updateRemote.isSelected());
			attrs.put("isDeleteFile", fileDelete.isSelected());
			attrs.put("isGenUpdate", updateZip.isSelected());
			attrs.put("isGenUpdateWAS", updateZipWas.isSelected());
			attrs.put("compareLength", compareLength.isSelected());
			attrs.put("compareTime", compareTime.isSelected());
			attrs.put("updateLocal", updateLocal.isSelected());
			attrs.put("moduleName", moduleNameField.getText());
			attrs.put("rules", ruleMap);
			
			if("Git".equals(ctrlVersion)) {
				try {
					uf = new GitUpdateFile(attrs);
				} catch (URISyntaxException e1) {
					logger.catching(e1);
					setMessage(e1.getMessage());
				}
			} else if("CVS".equals(ctrlVersion)) {
				try {
					uf = new CVSUpdateFile(attrs);
				} catch (URISyntaxException e1) {
					logger.catching(e1);
					setMessage(e1.getMessage());
				}
			} else if("Local".equals(ctrlVersion)) {
				try {
					uf = new LocalUpdateFile(attrs);
				} catch (URISyntaxException e1) {
					logger.catching(e1);
					setMessage(e1.getMessage());
				}
			} else {
				setMessage("您选择的是："+ctrlVersion+"，暂不支持！");
				return;
				
			}
			
			UpdateTarget t = null;
			try {
				t = new UpdateTarget(uf);
				t.execute();
				if(t.getOutFile() != null) {
					setMessage(t.getOutFile());
				} else {
					setMessage("没有需要更新的文件！");
				}
			} catch (BuildException e2) {
				logger.catching(e2);
				setMessage(e2.getMessage());
			} catch (Exception e2) {
				logger.catching(e2);
				setMessage("执行异常，请查看日志！");
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
	
	public void setMessage(File file) throws IOException {
		if(!file.exists()) {
			return;
		}
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		String l;
		while ((l = inputStream.readLine()) != null) {
			setMessage(l);
		}
		inputStream.close();
	}
}
