package com.wyb.tool.pub.updatefile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import com.wyb.tool.util.DateUtil;

public class CVSUpdateFile extends UpdateFile {
	private final Logger logger = LogManager.getLogger(CVSUpdateFile.class);
	
	public CVSUpdateFile(Map<String, Object> attrs) throws URISyntaxException {
		initArr(attrs);
	}
	
	public CVSUpdateFile() {}
	
	public static void main(String[] args) throws AuthenticationException, IOException, CommandException {
		CVSUpdateFile cuf = new CVSUpdateFile();
		cuf.setWorkDir(new File("D:\\workspace\\YTZB"));
		cuf.setPassword("weiyb");
		cuf.setEncode("GBK");
		cuf.setOutFile(new File("D:\\tmp\\ytzb\\log\\outFile.txt"));
		cuf.setErrorFile(new File("D:\\tmp\\ytzb\\log\\errorFile.txt"));
		cuf.genUpdateFile();
	}
	
	public File genUpdateFile() throws IOException {
		logger.entry();
		CVSRoot root = CVSRoot.parse(getCVSRoot());
		
		CVSPServerConnection psc = new CVSPServerConnection(root);
		psc.setEncoding(getEncode());
		psc.setEncodedPassword(StandardScrambler.getInstance().scramble(getPassword()));
		
		CVSStandardAdminHandler handler = new CVSStandardAdminHandler();
		handler.setEntryEncoding(getEncode());
		Client client = new Client(psc, handler);
		
		client.setLocalPath(getWorkDir().getAbsolutePath());
		client.getEventManager().addCVSListener(new BasicListener());
		
		UpdateCommand updateC = new UpdateCommand();
		
		GlobalOptions globalOptions = new GlobalOptions();
		globalOptions.setCVSCommand('n', "");
		globalOptions.setCVSCommand('q', "");
		
		
		Set<String> ignoreFiles = null;
		if (!StringUtils.isBlank(getIgnoreFile())) {
			ignoreFiles = processIgnores(getIgnoreFile());
		}
		
		File outFile = File.createTempFile("outFile", ".tmp");
		setOutFile(outFile);
		try {
			client.executeCommand(updateC, globalOptions);
			
			dealUpdateFile(outFile);
			
		} catch (CommandAbortedException e) {
			logger.catching(e);
		} catch (CommandException e) {
			logger.catching(e);
		} catch (AuthenticationException e) {
			logger.catching(e);
		}
		if(ignoreFiles != null) {
			for(String ignoreFile : ignoreFiles) {
				File file = new File(ignoreFile);
				File fileBak = new File(file.getParent(), ".cvsignorebak");
				file.delete();
				if(fileBak.exists()) {
					fileBak.renameTo(file);
//					fileBak.delete();
				}
			}
		}
		return logger.exit(getOutFile());
	}
	
	private class BasicListener extends CVSAdapter {
		/** * Stores a tagged line */
		private final StringBuffer taggedLine = new StringBuffer();

		/**
		 * * Called when the server wants to send a message to be displayed to *
		 * the user. The message is only for information purposes and clients *
		 * can choose to ignore these messages if they wish. * @param e the
		 * event
		 */
		public void messageSent(MessageEvent e) {
			String line = e.getMessage();
			try {
				if (e.isTagged()) {
					String message = MessageEvent.parseTaggedMessage(taggedLine, line);
					if (!StringUtils.isBlank(StringUtils.trim(message))) {
						writeToFile(getOutFile(), message+"\n", true);
					}
					logger.trace(message);
				} else {
					if (!StringUtils.isBlank(StringUtils.trim(line))) {
						writeToFile(getOutFile(), line+"\n", true);
					}
					logger.trace(line);
				}
			} catch (IOException e1) {
				logger.catching(e1);
			}
		}
	}
	/**
	 * 处理需要忽略的文件字符串
	 * 字符串通过空白字符进行分割，如果文件名和目录中有空格，会被当前两个文件或目录
	 * 文件名或目录名支持通配符*
	 * @author wyb
	 * @date 2015年9月8日 下午3:07:01
	 * @param ignoreStr
	 * @return
	 * @throws IOException 
	 */
	private Set<String> processIgnores(String ignoreStr) throws IOException {
		String [] fileStrs = ignoreStr.trim().split("\\s+");
		Set<String> ignoreFiles = new HashSet<String>();
		for(String fileName : fileStrs) {
			//处理含有子目录目录或文件
			if(fileName.contains("/")) {
				processIgnoreDir(getWorkDir(), fileName, ignoreFiles);
			}
			//处理不含有子目录 的文件或目录
			else {
				processIgnoreFile(getWorkDir(), fileName, ignoreFiles);
			}
		}
		return ignoreFiles;
	}
	
	/**
	 * 处理含有分隔目录 的目录 或文件
	 * @author wyb
	 * @date 2015年9月8日 下午4:43:52
	 * @param parent
	 * @param fileName
	 * @param ignoreFileSet
	 * @throws IOException 
	 */
	private void processIgnoreDir(File parent, String fileName, Set<String> ignoreFileSet) throws IOException {
		int index = fileName.indexOf("/");
		if(index == (fileName.length()-1)) {
			processIgnoreFile(parent, fileName, ignoreFileSet);
		}
		String parentDir = fileName.substring(0, index);
		final String [] subFileNameMatchs = parentDir.split("\\*");
		final boolean endStar = fileName.endsWith("*");
		final boolean startStar = fileName.startsWith("*");
		File [] subMatchs = parent.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				boolean isMatch = true;
				for(String match : subFileNameMatchs) {
					if(startStar && match.equals("")) {
						continue;
					}
					if(name.contains(match)) {
						if(name.endsWith(match)) {
							name = "";
							continue;
						}
						name = name.substring(name.indexOf(match)+match.length());
					} else {
						isMatch = false;
					}
					if(!endStar && !StringUtils.isBlank(name)) {
						isMatch = false;
					}
				}
				return isMatch;
			}
		});
		if(subMatchs != null && subMatchs.length > 0) {
			String subFileName = "";
			if(fileName.length() > (index+1)) {
				subFileName = fileName.substring(index+1);
			}
			if(subFileName.indexOf("/") >=0) {
				for(File subMatch : subMatchs) {
					processIgnoreDir(subMatch, subFileName, ignoreFileSet);
				}
			} else if(subFileName.length() > 0){
				for(File subMatch : subMatchs) {
					processIgnoreFile(subMatch, subFileName, ignoreFileSet);
				}
			}
		}
	}
	/**
	 * 处理匹配的文件或目录 
	 * @author wyb
	 * @date 2015年9月8日 下午4:34:26
	 * @param fileName
	 * @param ignoreFileSet
	 * @throws IOException
	 */
	private void processIgnoreFile(File parent, String fileName, Set<String> ignoreFileSet) throws IOException {
		final String[] fileNameMatchs = fileName.split("\\*");
		final boolean endStar = fileName.endsWith("*");
		final boolean startStar = fileName.startsWith("*");
		File [] matchFiles = parent.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				boolean isMatch = true;
				for(String match : fileNameMatchs) {
					if(startStar && match.equals("")) {
						continue;
					}
					if(name.contains(match)) {
						if(name.endsWith(match)) {
							name = "";
							continue;
						}
						name = name.substring(name.indexOf(match)+match.length());
					} else {
						isMatch = false;
					}
				}
				if(!endStar && !StringUtils.isBlank(name)) {
					isMatch = false;
				}
				return isMatch;
			}
		});
		
		if(matchFiles == null || matchFiles.length < 1) {
			return;
		}
		for(File matchFile : matchFiles) {
			processIgnore(matchFile, ignoreFileSet);
		}
		
	}
	/**
	 * 处理忽略文件，如果已经存在忽略文件，则将原有的文件备份为.cvsignorebak
	 * @author wyb
	 * @date 2015年9月8日 下午3:42:28
	 * @param parent
	 * @param matchFile
	 * @return
	 * @throws IOException 
	 */
	private void processIgnore(File matchFile, Set<String> ignoreFileSet) throws IOException {
		String ignoreStr = "";
		File ignoreFile = null;
		ignoreStr = "\n"+matchFile.getName();
		ignoreFile = new File(matchFile.getParentFile(), ".cvsignore");
		addIgnore(ignoreStr, ignoreFile, ignoreFileSet);
		if(!matchFile.isFile()) {
			ignoreStr = "\n*";
			ignoreFile = new File(matchFile, ".cvsignore");
			addIgnore(ignoreStr, ignoreFile, ignoreFileSet);
			File[] subFiles = matchFile.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if(new File(dir, name).isDirectory()) {
						return true;
					}
					return false;
				}
			});
			if(subFiles != null && subFiles.length > 0) {
				for(File subFile : subFiles) {
					processIgnore(subFile, ignoreFileSet);
				}
			}
		}
	}
	private void addIgnore(String ignoreStr, File ignoreFile, Set<String> ignoreFileSet) throws IOException {
		if(!ignoreFileSet.contains(ignoreFile.getAbsolutePath())) {
			if(!ignoreFile.exists()) {
				ignoreFile.createNewFile();
				ignoreStr = ignoreStr + "\n.cvsignore";
			} else {
				File ignoreFileBak = new File(ignoreFile.getParentFile(), ".cvsignorebak");
				if(ignoreFileBak.exists()) {
					copyFile(ignoreFile, ignoreFileBak);
				}
			}
			ignoreFileSet.add(ignoreFile.getAbsolutePath());
		}

		FileOutputStream out = null;
		try{
			out = new FileOutputStream(ignoreFile, true);
			out.write((ignoreStr).getBytes());
		} finally {
			if(out != null) {
				out.close();
			}
		}
	}
	private String getCVSRoot() throws IOException{
		String root = null; 
		BufferedReader r = null; 
		try { 
			File rootFile = new File(getWorkDir(), "CVS/Root"); 
			if (rootFile.exists()) { 
				r = new BufferedReader(new FileReader(rootFile)); 
				root = r.readLine(); 
			} 
		} finally { 
			if (r != null) 
				r.close(); 
		} 
		return root;
	}
	
	public void dealUpdateFile(File outFile) throws IOException {
		logger.entry(outFile);
		boolean needRule = false;
		Map<String, String> rules = getRuleMap();
		StringBuilder regex = new StringBuilder("(");
		Pattern pattern = null;
		if(rules != null && !rules.isEmpty()) {
			for(String key : rules.keySet()) {
				regex.append(key+"|");
			}
			int length = regex.length();
			regex.replace(length-1, length, ")/.*");
			needRule = true;
			
			pattern = Pattern.compile(regex.toString());
		}
		
		String updateFileName = "updatefile-"+DateUtil.convertDateTime(new Date(), DateUtil.timeFormatStr)+".txt";
		File updateFile = null;
		BufferedReader reader = null;
		FileOutputStream out = null;
		FileOutputStream deleteOut = null;
		boolean isDelete = false;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)));
			String line = reader.readLine();
			while((line = reader.readLine()) != null) {
				if(out == null) {
					updateFile = new File(getToDir(), updateFileName);
					updateFile.createNewFile();
					out = new FileOutputStream(updateFile);
					deleteOut = new FileOutputStream(getDeleteFile(), true);
				}
				if(line.startsWith("U") ||
						line.startsWith("P")) {
					continue;
				}
				if(line.startsWith("R")) {
					isDelete = true;
				} else {
					isDelete = false;
				}
				line = line.substring(2);
				if(needRule) {
					Matcher matcher = pattern.matcher(line);
					if(matcher.matches()) {
						String key = matcher.group(1);
						line = line.replaceFirst(key, rules.get(key));
					}
				}
				
				if(line.startsWith("//")) {
					line = line.substring(2);
				} else if(line.startsWith("/")) {
					line = line.substring(1);
				}
				if(line.lastIndexOf(".java") > 0) {
					String classStr = line.replace(".java", "");
					if(!isDelete) {
						String pDirStr = classStr.substring(0,classStr.lastIndexOf("/"));
						final String className =  classStr.substring(classStr.lastIndexOf("/")+1);
						File pDir = new File(getFromDir(), pDirStr);
						String[] names = pDir.list(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								if(name.equals(className+".class") ||
										name.startsWith(className+"$")) {
									return true;
								}
								return false;
							}
						});
						if(names != null) {
							for(String name : names) {
								out.write((pDirStr+"/"+name+"\n").getBytes());
							}
						}
					} else {
						deleteOut.write((classStr+".class\n").getBytes());
						deleteOut.write((classStr+"$.*.class\n").getBytes());
					}
					continue;
				} else if(line.endsWith("/") || (new File(getFromDir(), line)).isDirectory()) {
					if(isDelete) {
						if(line.endsWith("/")) {
							deleteOut.write((line+".*").getBytes());
						} else {
							deleteOut.write((line+"/.*").getBytes());
						}
					} else {
						writeFile(out, getFromDir(), line);
					}
					continue;
				}
				line += "\n";
				if(isDelete) {
					deleteOut.write(line.getBytes());
				} else {
					out.write(line.getBytes());
				}
			}
		} finally {
			if(out != null) {
				out.close();
			}
			if(reader != null) {
				reader.close();
			}
			if(deleteOut != null) {
				deleteOut.close();
			}
		}
		outFile.delete();
		
		setOutFile(updateFile);
		logger.exit();
	}
}
