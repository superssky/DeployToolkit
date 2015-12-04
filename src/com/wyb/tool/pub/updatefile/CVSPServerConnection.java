package com.wyb.tool.pub.updatefile;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

public class CVSPServerConnection extends PServerConnection {
	private String encoding;

	public CVSPServerConnection(CVSRoot root) {
		super(root);
	}

	@Override
	public LoggedDataOutputStream getOutputStream() {
		if(super.getOutputStream() instanceof CVSLoggedDataOutputStream) {
			return super.getOutputStream();
		}
		CVSLoggedDataOutputStream out = new CVSLoggedDataOutputStream(super.getOutputStream());
		out.setOutEncoding(encoding);
		return out;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	
}
