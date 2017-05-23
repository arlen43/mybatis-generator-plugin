/**
 * 项目名: mybatis-generator-plugin
 * 文件名：PluginUtil.java 
 * 版本信息： V1.0
 * 日期：2017年2月9日 
 */
package com.arlen.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：PluginUtil <br>
 * 类描述：插件的一些共有方法抽出来<br>
 * 创建人：arlen <br>
 * 创建时间：2017年2月9日 下午7:36:54 <br>
 * @version 1.0
 * @author arlen
 */
public class PluginUtil {

	public static String getModelSimpleName(IntrospectedTable introspectedTable) {
		String simpleName = introspectedTable.getBaseRecordType();
		simpleName = simpleName.substring(simpleName.lastIndexOf(".")+1);
		return simpleName;
	}
	
	public static boolean hasField(TopLevelClass topLevelClass, String fieldName) {
		if (topLevelClass == null || fieldName == null) {
			return false;
		}
		List<Field> fieldList = topLevelClass.getFields();
		if (fieldList == null || fieldList.size() <= 0) {
			return false;
		}
		for (Field field : fieldList) {
			if (fieldName.equals(field.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasMethod(TopLevelClass topLevelClass, String methodName) {
		if (topLevelClass == null || methodName == null) {
			return false;
		}
		List<Method> methodList = topLevelClass.getMethods();
		if (methodList == null || methodList.size() <= 0) {
			return false;
		}
		for (Method method : methodList) {
			if (methodName.equals(method.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public static void addClazzField(TopLevelClass topLevelClass, String fieldName, FullyQualifiedJavaType javaType, String comment) {
		if (!PluginUtil.hasField(topLevelClass, fieldName)) {
			Field nameField = new Field(fieldName, javaType);
			nameField.setVisibility(JavaVisibility.PRIVATE);
			nameField.addJavaDocLine("/**");
			nameField.addJavaDocLine(" * 自动生成字段，" + comment);
			nameField.addJavaDocLine(" */");
			topLevelClass.addField(nameField);
		}
	}

	public static void addClazzGetMethod(TopLevelClass topLevelClass, String fieldName, FullyQualifiedJavaType javaType, String comment) {
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		if (!PluginUtil.hasMethod(topLevelClass, methodName)) {
			Method method = new Method(methodName);
	        method.setVisibility(JavaVisibility.PUBLIC);
	        method.setReturnType(javaType);
	        method.addJavaDocLine("/**");
	        method.addJavaDocLine(" * 自动生成get方法，" + fieldName);
	        method.addJavaDocLine(" * @return " + fieldName + " " + comment);
			method.addJavaDocLine(" */");
			method.addBodyLine("return "+fieldName+";");
			topLevelClass.addMethod(method);
		}
	}

	public static void addClazzSetMethod(TopLevelClass topLevelClass, String fieldName, FullyQualifiedJavaType javaType, String comment) {
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		if (!PluginUtil.hasMethod(topLevelClass, methodName)) {
			Method method = new Method(methodName);
	        method.setVisibility(JavaVisibility.PUBLIC);
	        
	        Parameter param = new Parameter(javaType, fieldName);
	        method.addParameter(param);
	        method.addJavaDocLine("/**");
	        method.addJavaDocLine(" * 自动生成set方法，" + fieldName);
	        method.addJavaDocLine(" * @param String " + fieldName + " " + comment);
			method.addJavaDocLine(" */");
			if (javaType.equals(FullyQualifiedJavaType.getStringInstance())) {
				method.addBodyLine("this."+fieldName+" = "+fieldName+" == null ? null : "+fieldName+".trim();");
			} else {
				method.addBodyLine("this."+fieldName+" = "+fieldName+";");
			}
			topLevelClass.addMethod(method);
		}
	}
	
}
