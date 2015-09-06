package com.wyb.tool.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 属性文件读取
 * @author wyb
 *
 */
public class ConfigProperties
{
	private static Map<String, Properties> props = new HashMap<String, Properties>();

	public static Properties getProperties (String path)
	{
		if(props.get(path) == null) {
			props.put(path, loadConfig(path));
		}
		return props.get(path);
	}

	/**
	 * 解析属性文件
	 * @author wyb
	 * @date 2015年8月27日 下午5:16:42
	 * @param path
	 * @return
	 */
	private static Properties loadConfig (String path){
		Properties p = new Properties();
		try {
			InputStream is = ConfigProperties.class.getResourceAsStream("/"+path);
			p.load(is);
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return p;
	}

}