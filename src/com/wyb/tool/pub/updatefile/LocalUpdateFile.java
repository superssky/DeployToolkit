package com.wyb.tool.pub.updatefile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wyb.tool.util.DateUtil;

public class LocalUpdateFile extends UpdateFile {
	private final Logger logger = LogManager.getLogger(LocalUpdateFile.class);
	
	public LocalUpdateFile(Map<String, Object> attrs) throws URISyntaxException {
		initArr(attrs);
	}

	private boolean hasChange = false;
	public LocalUpdateFile(){}

	public File genUpdateFile() throws IOException {
		logger.entry();
		//更新文件列表文件
		String updateFileName = "updatefile-"+DateUtil.convertDateTime(new Date(), DateUtil.timeFormatStr)+".txt";
		File updateFile = new File(getToDir(), updateFileName);
		updateFile.createNewFile();
		setOutFile(updateFile);
		
		if(!getLocalPath().exists()) {
			hasChange = true;
			getLocalPath().mkdir();
		}
		
		FileOutputStream out = new FileOutputStream(updateFile, true);
		FileOutputStream deleteOut = new FileOutputStream(getDeleteFile(), true);
		try {
			dealDir(out, getFromDir(), true);
			dealDir(deleteOut, getLocalPath(), false);
		} finally {
			out.close();
			deleteOut.close();
		}
		if(!hasChange) {
			updateFile.delete();
		}
		return logger.exit(updateFile);
	}

	private void dealDir(FileOutputStream out, File dir, boolean isFrom) throws IOException {
		for(File file : dir.listFiles()) {
			String path = getFilePath((isFrom ? getFromDir() : getLocalPath()), file);
			if(isFrom) {
				if(file.isFile() && isNewFile(file, new File(getLocalPath(), path))) {
					hasChange = true;
					out.write((path+"\n").getBytes());
					logger.trace(path);
				} else if(file.isDirectory()) {
					dealDir(out, file, isFrom);
				}
			} else {
				if(file.isFile() && !(new File(getFromDir(), path).exists())) {
					out.write((path + "\n").getBytes());
					logger.trace(path);
				} else if(file.isDirectory()) {
					dealDir(out, file, isFrom);
				}
			}
		}
	}
	/**
	 * 取文件file的相对fromDir目录 的路径
	 * @author wyb
	 * @date 2015年9月14日 下午4:48:16
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String getFilePath(File parent, File file) throws IOException {
		String parentPath = parent.getAbsolutePath();
		String filePath = file.getAbsolutePath();
		String path = filePath.substring(parentPath.length()+1);
		return path.replaceAll("\\\\", "/");
	}
	/**
	 * 比较两个文件大小是不是一致，或者修改时间是否更新
	 * @author wyb
	 * @date 2015年9月11日 下午2:31:28
	 * @param newFile
	 * @param oldFile
	 * @return
	 */
	private boolean isNewFile(File newFile, File oldFile) {
		//旧文件不存在
		if(!oldFile.exists()) {
			return true;
		}
		//比较最近修改时间
		if(isCompareTime() && newFile.lastModified() > oldFile.lastModified()) {
			return true;
		}
		//比较大小
		if(isCompareLength() && newFile.length() != oldFile.length()) {
			return true;
		}
		return false;
	}
	public static void main(String [] args) throws IOException {
		LocalUpdateFile cuf = new LocalUpdateFile();
		cuf.setWorkDir(new File("D:\\workspace\\YTZB"));
		cuf.setFromDir(new File("D:\\Program Files\\apache-tomcat-6.0.16\\webapps\\ytzb"));
		cuf.setLocalPath(new File("D:\\tmp\\ytzb\\local"));
		cuf.setToDir(new File("D:\\tmp\\ytzb"));
		cuf.setOutFile(new File("D:\\tmp\\ytzb\\log\\outFile.txt"));
		cuf.setErrorFile(new File("D:\\tmp\\ytzb\\log\\errorFile.txt"));
		cuf.genUpdateFile();
	}

	@Override
	public void dealUpdateFile(File outFile) throws IOException {
	}
}
