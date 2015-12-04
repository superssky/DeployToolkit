package com.wyb.tool.pub.updatefile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Commandline;

import com.wyb.tool.util.DateUtil;

public class GitUpdateFile extends UpdateFile {
	private final Logger logger = LogManager.getLogger(GitUpdateFile.class);

	private ExecTask exec = new ExecTask();
	private String cmd = "";
	private boolean isLog = false;
	
	public GitUpdateFile(Map<String, Object> attrs) throws URISyntaxException {
		initArr(attrs);
	}
	
	public File genUpdateFile() throws IOException {
		exec.setProject(new Project());
		exec.setExecutable("cmd.exe");
		
		if(!getWorkDir().exists()) {
			throw new BuildException("工作目录不能为空！");
		}
		exec.setDir(getWorkDir());
		if(StringUtils.isBlank(getGitCmd())) {
			cmd = "status -s";
		} else if(getGitCmd().matches("git\\s+status.*")){
			cmd = "status -s";
		} else if(getGitCmd().matches("git\\s+log\\s+-\\d+.*")) {
			String[] matches = getGitCmd().split("\\s+");
			for(String m : matches) {
				if(m.equals("log")) {
					cmd += m;
				}
				if(m.matches("-\\d+")) {
					int num = Integer.parseInt(m.substring(1));
					cmd += " -1 --skip="+(num-1);
					
					break;
				}
			}
			cmd += " --name-status --pretty=format:\"\" --date-order";
			isLog = true;
		} else {
			cmd = "status -s";
		}
		Commandline.Argument arg = exec.createArg();
		if(getControlPath().exists() && getControlPath().isDirectory()) {
			arg.setLine("/c "+getControlPath().getCanonicalPath()+"\\git.exe "+cmd);
		} else {
			arg.setLine("/c git "+cmd);
		}
		exec.setFailonerror(true);
		exec.setAppend(true);

		File outFile = File.createTempFile("outFile", ".tmp");
		setOutFile(outFile);
		exec.setOutput(outFile);
		
		exec.execute();
		dealUpdateFile(outFile);
		return getOutFile();
	}

	public void dealUpdateFile(File outFile) throws IOException {
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
		FileOutputStream out = null;
		BufferedReader reader = null;
		FileOutputStream deleteOut = null;
		boolean isDelete = false;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)));
			String line = "";
			while((line = reader.readLine()) != null) {
				if(StringUtils.isBlank(line)) {
					continue;
				}
				logger.trace(line);
				if(out == null) {
					updateFile = new File(getToDir(), updateFileName);
					updateFile.createNewFile();
					out = new FileOutputStream(updateFile);
					deleteOut = new FileOutputStream(getDeleteFile(), true);
				}
				if(isLog) {
					if(line.charAt(0) == 'D') {
						isDelete = true;
					} else {
						isDelete = false;
					}
					line = line.substring(1).trim();
				} else {
					if(line.charAt(1) == 'D') {
						isDelete = true;
					} else {
						isDelete = false;
					}
					line = line.substring(3);
				}
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
		}
		outFile.delete();
		setOutFile(updateFile);
	}
}
