package com.wyb.tool.pub;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;

import com.wyb.tool.pub.updatefile.LocalUpdateFile;
import com.wyb.tool.pub.updatefile.UpdateFile;

public class UpdateTarget extends Target {
	private final Logger logger = LogManager.getLogger(UpdateTarget.class);

	private File outFile;
	private UpdateFile uf;
	
	public UpdateTarget(UpdateFile uf) throws IOException{
		super();
		
		this.uf = uf;
		
		setProject(new Project());
		getProject().init();
	}
	
	@Override
	public void execute() throws BuildException{
		try {
			
			if(!uf.getFromDir().exists() || !uf.getFromDir().isDirectory()) {
				throw new BuildException("更新文件目录不存在！");
			}
			
			if(!uf.getToDir().exists() || !uf.getToDir().isDirectory()) {
				uf.getToDir().mkdir();
			}
			
			//更新文件
			File updatefile = uf.genUpdateFile();
			
			if(updatefile != null && updatefile.exists()) {
				setOutFile(updatefile);
				File updateDir = new File(uf.getToDir(), updatefile.getName().replace(".txt", ""));
				updateDir.mkdir();

				Copy copy = new Copy();
				
				copy.setProject(getProject());
				copy.setPreserveLastModified(true);
				
				FileSet outSet = new FileSet();
				outSet.setDir(uf.getFromDir());
				outSet.setDefaultexcludes(false);
				copy.addFileset(outSet);
				copy.setForce(true);
				
				if(uf.isUpdateLocal() && uf instanceof LocalUpdateFile) {
					copy.setTodir(uf.getLocalPath());
					copy.execute();
				}
				
				outSet.createIncludesFile().setName(updatefile.getCanonicalPath());
				DefaultLogger listener = new DefaultLogger();
				listener.setOutputPrintStream(System.out);
//				listener.setErrorPrintStream(System.out);
				listener.setMessageOutputLevel(Project.MSG_VERBOSE);
				getProject().addBuildListener(listener);
				copy.setTaskName("copy");
				copy.setTodir(updateDir);
				copy.execute();
				
				if(uf.isGenUpdate()) {
					Zip zip = new Zip();
					zip.setProject(getProject());
					zip.setDestFile(new File(uf.getToDir(), updatefile.getName().replace(".txt", ".zip")));
					zip.setBasedir(updateDir);
					
					zip.execute();
				}
				
				if(uf.isGenUpdateWAS()) {
					Zip zip = new Zip();
					zip.setProject(getProject());
					
					File wasDir = new File(uf.getToDir(), uf.getToDir().getName()+"/"+uf.getModuleName());
					wasDir.mkdirs();
					
					copy.setTodir(wasDir);
					copy.execute();
					
					if(uf.getDeleteFile().length() != 0L) {
						File wasDeleteFile = new File(wasDir, "META-INF/ibm-partialapp-delete.props");
						wasDeleteFile.getParentFile().mkdir();
						uf.copyFile(uf.getDeleteFile(), wasDeleteFile);
					}
					
					zip.setDestFile(new File(uf.getToDir(), updatefile.getName().replace(".txt", ".zip")));
					zip.setBasedir(wasDir.getParentFile());
					
					zip.execute();
					
					Delete delete = new Delete();
					delete.setDir(wasDir.getParentFile());
					delete.execute();
				}
				
				if(uf.isUpdateRemote()) {
					ExecTask exec = new ExecTask();
					exec.setProject(getProject());
					exec.setExecutable("cmd.exe");
					exec.setOutput(outFile);
					
					exec.setFailonerror(true);
					exec.setAppend(true);
					
					Commandline.Argument arg = exec.createArg();
					arg.setLine("/c xcopy /D/S/Y "+updateDir.getAbsolutePath()+" "+ uf.getRemoteDir());
//							" \\\\192.168.1.204\\D$\\apache-tomcat-5.5.23-manifest_pt\\webapps\\manifestpt ");
					exec.execute();
				}
				
				if(uf.isDeleteFile()) {
					updatefile.delete();
				}
				Delete delete = new Delete();
				delete.setDir(updateDir);
				delete.execute();
			}

			uf.getDeleteFile().delete();
		} catch (IOException e) {
			logger.catching(e);
			throw new BuildException(e);
		}
	}
	
	public File getOutFile() {
		return outFile;
	}

	public void setOutFile(File outFile) {
		this.outFile = outFile;
	}
	
}
