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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Commandline;

import com.wyb.tool.util.DateUtil;

public class GitUpdateFile extends UpdateFile {
	
	public GitUpdateFile(Map<String, Object> attrs) throws URISyntaxException {
		initArr(attrs);
	}
	
	public void initTask(ExecTask exec) throws IOException {
		exec.setExecutable("cmd.exe");
		
		if(!getWorkDir().exists()) {
			throw new BuildException("工作目录不能为空！");
		}
		exec.setDir(getWorkDir());
		Commandline.Argument arg = exec.createArg();
		if(getControlPath().exists() && getControlPath().isDirectory()) {
			arg.setLine("/c "+getControlPath().getCanonicalPath()+"\\git.exe status -s");
		} else {
			arg.setLine("/c git status -s");
		}
//		if(exec.getResolveExecutable()) {
//			throw new BuildException("git.exe 未指定安装目录，或未添加至环境变量中！");
//		}
		exec.setFailonerror(true);
		exec.setAppend(true);
	}

	public File dealUpdateFile(File outFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)));
		String line = reader.readLine();
		
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
		while((line = reader.readLine()) != null) {
			if(out == null) {
				updateFile = new File(getToDir(), updateFileName);
				updateFile.createNewFile();
				out = new FileOutputStream(updateFile);
			}
			line = line.substring(3);
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
				String pDirStr = classStr.substring(0,classStr.lastIndexOf("/"));
				String className =  classStr.substring(classStr.lastIndexOf("/")+1);
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
				continue;
			} else if(line.endsWith("/")) {
				writeFile(out, getFromDir(), line);
				continue;
			}
			line += "\n";
			out.write(line.getBytes());
		}
		if(out != null) {
			out.close();
		}
		
		reader.close();
		return updateFile;
	}
	
	private void writeFile(FileOutputStream out, File parent, String filePath) throws IOException {
		File file = new File(parent, filePath);
		if(file.exists()) {
			File [] files = file.listFiles();
			for(File f : files) {
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
}
