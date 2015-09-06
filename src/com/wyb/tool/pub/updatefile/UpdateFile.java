package com.wyb.tool.pub.updatefile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.taskdefs.ExecTask;

public abstract class UpdateFile {
	
	//更新包存放目录
	private File toDir;
	//更新文件目录
	private File fromDir;
	//系统工作目录
	private File workDir;
	//远程更新目录
	private File remoteDir;
	//版本管理工具名称
	private String control;
	//版本管理工具程序路径
	private File controlPath;
	//是否更新远程目录
	private boolean isUpdateRemote;
	//是否生成更新文件列表
	private boolean isGenUpdateFile;
	//是否生成更新包
	private boolean isGenUpdate;
	//匹配规则
	private Map<String, String> ruleMap;
	
	@SuppressWarnings("unchecked")
	public void initArr(Map<String, Object> attrs) throws URISyntaxException {
		setToDir(createFile((String) attrs.get("toDir")));
		setFromDir(createFile((String) attrs.get("fromDir")));
		setWorkDir(createFile((String) attrs.get("workDir")));
//		setRemoteDir(createFile(new URI((String) attrs.get("remoteDir"))));
		setControlPath(createFile((String) attrs.get("controlPath")));
		setUpdateRemote((Boolean) attrs.get("isUpdateRemote"));
		setGenUpdateFile((Boolean) attrs.get("isGenUpdateFile"));
		setGenUpdate((Boolean) attrs.get("isGenUpdate"));
		setControl((String) attrs.get("control"));
		setRuleMap((Map<String, String>)attrs.get("rules"));
	}
	
	private File createFile(String path) {
		if(!StringUtils.isBlank(path)) {
			return new File(path);
		}
		return null;
	}

	private File createFile(URI path) {
		if(path != null) {
			return new File(path);
		}
		return null;
	}
	
	public abstract void initTask(ExecTask exec) throws IOException;

	public abstract File dealUpdateFile(File outFile) throws IOException;
	
	public File getToDir() {
		return toDir;
	}
	
	public void setToDir(File toDir) {
		this.toDir = toDir;
	}
	
	public File getFromDir() {
		return fromDir;
	}
	
	public void setFromDir(File fromDir) {
		this.fromDir = fromDir;
	}
	
	public File getWorkDir() {
		return workDir;
	}
	
	public void setWorkDir(File workDir) {
		this.workDir = workDir;
	}
	
	public File getRemoteDir() {
		return remoteDir;
	}
	
	public void setRemoteDir(File remoteDir) {
		this.remoteDir = remoteDir;
	}
	
	public File getControlPath() {
		return controlPath;
	}
	
	public void setControlPath(File controlPath) {
		this.controlPath = controlPath;
	}
	
	public boolean isUpdateRemote() {
		return isUpdateRemote;
	}
	
	public void setUpdateRemote(boolean isUpdateRemote) {
		this.isUpdateRemote = isUpdateRemote;
	}
	
	public boolean isGenUpdateFile() {
		return isGenUpdateFile;
	}
	
	public void setGenUpdateFile(boolean isGenUpdateFile) {
		this.isGenUpdateFile = isGenUpdateFile;
	}
	
	public boolean isGenUpdate() {
		return isGenUpdate;
	}
	
	public void setGenUpdate(boolean isGenUpdate) {
		this.isGenUpdate = isGenUpdate;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public Map<String, String> getRuleMap() {
		return ruleMap;
	}

	public void setRuleMap(Map<String, String> ruleMap) {
		this.ruleMap = ruleMap;
	}
}
