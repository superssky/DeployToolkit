package com.wyb.tool.pub.updatefile;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

public class CVSLoggedDataOutputStream extends LoggedDataOutputStream {

	private String outEncoding;
	public CVSLoggedDataOutputStream(OutputStream out) {
		super(out);
	}
	
	@Override
	public void writeBytes(String line) throws IOException {
		if(!StringUtils.isBlank(outEncoding)) {
			super.writeBytes(line, outEncoding);
		} else {
			super.writeBytes(line);
		}
	}

	public String getOutEncoding() {
		return outEncoding;
	}
	public void setOutEncoding(String outEncoding) {
		this.outEncoding = outEncoding;
	}

	
}
