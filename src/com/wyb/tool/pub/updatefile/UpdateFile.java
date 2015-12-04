package com.wyb.tool.pub.updatefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

public abstract class UpdateFile {
	private Logger logger = LogManager.getLogger(UpdateFile.class);
	
	//更新包存放目录
	private File toDir;
	//更新文件目录
	private File fromDir;
	//系统工作目录
	private File workDir;
	//远程更新目录
	private String remoteDir;
	//版本管理工具名称
	private String control;
	//版本管理工具程序路径
	private File controlPath;
	//git命令
	private String gitCmd;
	//密码
	private String password;
	//字符集
	private String encode;
	//忽略文件
	private String ignoreFile;
	//本地比对路径
	private File localPath;
	//比对大小
	private boolean compareLength;
	//比对时间
	private boolean compareTime;
	//更新本地
	private boolean updateLocal;
	//是否更新远程目录
	private boolean isUpdateRemote;
	//是否删除更新文件列表
	private boolean isDeleteFile;
	//是否生成更新包
	private boolean isGenUpdate;
	//是否生成WAS更新包
	private boolean isGenUpdateWAS;
	//模块名称
	private String moduleName;
	//删除的文件
	private File deleteFile;
	//匹配规则
	private Map<String, String> ruleMap;
	//输出文件列表
	private File outFile;
	//输出错误信息
	private File errorFile;
	
	@SuppressWarnings("unchecked")
	public void initArr(Map<String, Object> attrs) throws URISyntaxException {
		setToDir(createFile((String) attrs.get("toDir")));
		setFromDir(createFile((String) attrs.get("fromDir")));
		setWorkDir(createFile((String) attrs.get("workDir")));
		setEncode((String) attrs.get("encode"));
		setRemoteDir((String) attrs.get("remoteDir"));
		setIgnoreFile((String) attrs.get("ignoreFile"));
		setControlPath(createFile((String) attrs.get("controlPath")));
		setGitCmd((String) attrs.get("gitCmd"));
		setPassword((String) attrs.get("password"));
		setLocalPath(createFile((String) attrs.get("localPath")));
		setUpdateRemote((Boolean) attrs.get("isUpdateRemote"));
		setDeleteFile((Boolean) attrs.get("isDeleteFile"));
		setGenUpdate((Boolean) attrs.get("isGenUpdate"));
		setGenUpdateWAS((Boolean) attrs.get("isGenUpdateWAS"));
		setModuleName((String)attrs.get("moduleName"));
		setCompareLength((Boolean) attrs.get("compareLength"));
		setUpdateLocal((Boolean) attrs.get("updateLocal"));
		setCompareTime((Boolean) attrs.get("compareTime"));
		setControl((String) attrs.get("control"));
		setRuleMap((Map<String, String>)attrs.get("rules"));
		
		setDeleteFile(new File(getToDir(), "deleteFile"));
		try {
			if(getDeleteFile().exists()) {
				writeToFile(getDeleteFile(), "", false);
			} else {
				getDeleteFile().createNewFile();
			}
			
		} catch (IOException e) {
			logger.catching(e);
		}
	}
	
	private File createFile(String path) {
		if(!StringUtils.isBlank(path)) {
			return new File(path);
		}
		return null;
	}

	public abstract void dealUpdateFile(File outFile) throws IOException;

	public abstract File genUpdateFile() throws IOException;
	
	public void writeToFile(File file, String msg, boolean append) throws IOException {
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(file, append);
			out.write((msg).getBytes());
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}
	
	public void writeFile(FileOutputStream out, File parent, String filePath) throws IOException {
		File file = new File(parent, filePath);
		if(file.exists()) {
			File [] files = file.listFiles();
			for(File f : files) {
				if(!filePath.endsWith("/")) {
					filePath += "/";
				}
				String fileStr = filePath+f.getName();
				if(f.isFile()) {
					out.write((fileStr+"\n").getBytes());
				}
				if(f.isDirectory()) {
					writeFile(out, parent, fileStr+"/");
				}
			}
		}
	}

	/**
	 * 复制文件
	 * @author wyb
	 * @date 2015年9月8日 下午3:45:09
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public void copyFile(File src, File dest) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(src);
			if (!dest.exists()) {
				dest.createNewFile();
			}
			out = new FileOutputStream(dest);
			int c;
			byte buffer[] = new byte[1024];
			while ((c = in.read(buffer)) != -1) {
				for (int i = 0; i < c; i++)
					out.write(buffer[i]);
			}
		} finally {
			if(in != null) {
				in.close();
			}
			if(out != null) {
				out.close();
			}
		}
	}
	/**
	 * 通过ant的copy复制文件
	 * @author wyb
	 * @date 2015年9月11日 上午10:53:56
	 * @param src
	 * @param dest
	 */
	public void copyFile(File src, File dest, Project project) {
		Copy copy = new Copy();
		copy.setProject(project);
		copy.setTodir(dest);
		copy.setPreserveLastModified(true);
		FileSet outSet = new FileSet();
		outSet.setDir(src);
		copy.addFileset(outSet);
		
		copy.execute();
	}
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
	
	public String getRemoteDir() {
		return remoteDir;
	}
	
	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}
	
	public File getControlPath() {
		return controlPath;
	}
	
	public void setControlPath(File controlPath) {
		this.controlPath = controlPath;
	}
	
	public String getGitCmd() {
		return gitCmd;
	}

	public void setGitCmd(String gitCmd) {
		this.gitCmd = gitCmd;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getIgnoreFile() {
		return ignoreFile;
	}

	public void setIgnoreFile(String ignoreFile) {
		this.ignoreFile = ignoreFile;
	}

	public File getLocalPath() {
		return localPath;
	}

	public void setLocalPath(File localPath) {
		this.localPath = localPath;
	}

	public boolean isUpdateRemote() {
		return isUpdateRemote;
	}
	
	public void setUpdateRemote(boolean isUpdateRemote) {
		this.isUpdateRemote = isUpdateRemote;
	}
	
	public boolean isDeleteFile() {
		return isDeleteFile;
	}

	public void setDeleteFile(boolean isDeleteFile) {
		this.isDeleteFile = isDeleteFile;
	}

	public boolean isGenUpdate() {
		return isGenUpdate;
	}
	
	public void setGenUpdate(boolean isGenUpdate) {
		this.isGenUpdate = isGenUpdate;
	}

	public boolean isGenUpdateWAS() {
		return isGenUpdateWAS;
	}

	public void setGenUpdateWAS(boolean isGenUpdateWAS) {
		this.isGenUpdateWAS = isGenUpdateWAS;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public File getDeleteFile() {
		return deleteFile;
	}

	public void setDeleteFile(File deleteFile) {
		this.deleteFile = deleteFile;
	}

	public boolean isCompareLength() {
		return compareLength;
	}

	public void setCompareLength(boolean compareLength) {
		this.compareLength = compareLength;
	}

	public boolean isCompareTime() {
		return compareTime;
	}

	public void setCompareTime(boolean compareTime) {
		this.compareTime = compareTime;
	}

	public boolean isUpdateLocal() {
		return updateLocal;
	}

	public void setUpdateLocal(boolean updateLocal) {
		this.updateLocal = updateLocal;
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

	public File getOutFile() {
		return outFile;
	}

	public void setOutFile(File outFile) {
		this.outFile = outFile;
	}

	public File getErrorFile() {
		return errorFile;
	}

	public void setErrorFile(File errorFile) {
		this.errorFile = errorFile;
	}
}
