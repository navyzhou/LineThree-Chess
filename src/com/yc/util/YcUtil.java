package com.yc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class YcUtil {
	/**
	 * 取出当前的时间，以固定格式显示.
	 */
	public static String getCurrentTime(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日  hh:mm:ss");
		return sdf.format(new Date());
	}
	
	/**
	 * 显示信息框
	 */
	public static void showMsg(Shell shell,String title,String content){
		MessageBox mb=new MessageBox(shell,SWT.DIALOG_TRIM);
		mb.setText(title);
		mb.setMessage(content);
		mb.open();
	}
	
	public static boolean isNull(String str){
		if(str==null||"".equals(str)){
			return true;
		}else{
			return false;
		}
	}
}
