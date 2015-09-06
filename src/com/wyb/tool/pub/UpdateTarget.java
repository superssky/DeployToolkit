package com.wyb.tool.pub;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.FileSet;

import com.wyb.tool.pub.updatefile.UpdateFile;

public class UpdateTarget extends Target {

	private File outFile;
	private File errorFile;
	private ExecTask exec = new ExecTask();
	private Copy copy = new Copy();
	private UpdateFile uf;
	
	public UpdateTarget(UpdateFile uf){
		super();
		
		this.uf = uf;
		init();
	}
	
	public void init() {
		setProject(new Project());
		getProject().init();
		
		exec.setProject(getProject());
		
		copy.setProject(getProject());
	}

	@Override
	public void execute() throws BuildException{
		try {
			//exec输出信息
			if(outFile == null || !outFile.exists()) {
				outFile = File.createTempFile("outFile", ".tmp");
				outFile.deleteOnExit();
			}
			//exec错误信息
			if(errorFile == null || !errorFile.exists()) {
				errorFile = File.createTempFile("errorFile", ".tmp");
			}
			
			exec.setOutput(outFile);
			exec.setError(errorFile);
			
			if(!uf.getFromDir().exists() || !uf.getFromDir().isDirectory()) {
				throw new BuildException("更新文件目录不存在！");
			}
			
			if(!uf.getToDir().exists() || !uf.getToDir().isDirectory()) {
				uf.getToDir().mkdir();
			}
			
			uf.initTask(exec);
			exec.perform();
			
			File updatefile = uf.dealUpdateFile(outFile);
			
			if(updatefile != null) {
				File updateDir = new File(uf.getToDir(), updatefile.getName().replace(".txt", ""));
				updateDir.mkdir();
				
				copy.setTodir(updateDir);
				FileSet outSet = new FileSet();
				outSet.createIncludesFile().setName(updatefile.getCanonicalPath());;
				outSet.setDir(uf.getFromDir());
				copy.addFileset(outSet);
				
				copy.perform();
				
				if(!uf.isGenUpdateFile()) {
					updatefile.delete();
				}
			}
			
		} catch (IOException e) {
			throw new BuildException(e);
		}
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
