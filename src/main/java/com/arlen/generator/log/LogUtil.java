/**
 * 项目名: mybatis-generator-plugin
 * 文件名：LogUtil.java 
 * 版本信息： V1.0
 * 日期：2017年2月9日 
 */
package com.arlen.generator.log;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：LogUtil <br>
 * 类描述：简单的日志封装组件，不去纠结日志组件了<br>
 * 创建人：arlen <br>
 * 创建时间：2017年2月9日 上午11:39:05 <br>
 * @version 1.0
 * @author arlen
 */
public class LogUtil {

	public static void info(String log) {
		System.out.println("[ARLEN-PLUGIN-INFO] " + log);
	}

	public static void warn(String log) {
		System.out.println("[ARLEN-PLUGIN-WARN] " + log);
	}

	public static void error(String log) {
		System.out.println("[ARLEN-PLUGIN-ERROR] " + log);
	}
}
