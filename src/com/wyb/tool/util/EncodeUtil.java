package com.wyb.tool.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class EncodeUtil {

	public static String MD5Encrypt(String str) {
		String encryptStr = null;
		if (str == null)
			return null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			encryptStr = hash.toString(16).toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptStr;
	}
}
