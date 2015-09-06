package com.wyb.tool.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 日期操作工具类
 * @author wyb
 *
 */
public class DateUtil {
	
	final static String dateTimeFormatStr = "yyyy-MM-dd HH:mm:ss";
	public final static String timeFormatStr = "yyyy-MM-dd-HHmmss";
	final static DateFormat dateTimeFormat = new SimpleDateFormat(dateTimeFormatStr);
	final static DateFormat timeFormat = new SimpleDateFormat(timeFormatStr);
	final static String dateFormatStr = "yyyy-MM-dd";
	final static DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
	
	/**
	 * 将指定字符串格式日期转换成日期类型
	 * @author wyb
	 * @date 2015年9月2日 下午2:55:55
	 * @param dateStr
	 * @param formatStr
	 * @return
	 */
	public static Date convertStringToDate(String dateStr,String formatStr){
		if(!StringUtils.isBlank(dateStr)){
			try {
				return new SimpleDateFormat(formatStr).parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 将日期类型转换成字符串，格式：yyyy-MM-dd HH:mm:ss
	 * @author wyb
	 * @date 2015年9月2日 下午2:55:35
	 * @param date
	 * @return
	 */
	public static String convertDateTime(Date date){
		if(date != null){
			return dateTimeFormat.format(date);
		}
		return "";
	}
	
	/**
	 * 将日期类型转换成字符串，格式：yyyy-MM-dd
	 * @author wyb
	 * @date 2015年9月2日 下午2:55:18
	 * @param date
	 * @return
	 */
	public static String convertDate(Date date){
		if(date != null){
			return dateFormat.format(date);
		}
		return "";
	}
	
	/**
	 * 将日期类型转换成指定格式的字符串类型
	 * @author wyb
	 * @date 2015年9月2日 下午2:54:46
	 * @param date
	 * @param formatStr
	 * @return
	 */
	public static String convertDateTime(Date date,String formatStr){
		if(date != null && !StringUtils.isBlank(formatStr)){
			return new SimpleDateFormat(formatStr).format(date);
		}
		return "";
	}
	
	/**
	 * 获取当前日期时间字符串，格式：yyyy-MM-dd HH:mm:ss
	 * @author wyb
	 * @date 2015年9月2日 下午2:53:37
	 * @return
	 */
	public static String getCurrentDateStr(){
		return dateTimeFormat.format(new Date());
	}
}
