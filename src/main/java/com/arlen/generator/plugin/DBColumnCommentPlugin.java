/**
 * 项目名: mybatis-generator-plugin
 * 文件名：DBFieldCommentPlugin.java 
 * 版本信息： V1.0
 * 日期：2017年2月9日 
 */
package com.arlen.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：DBFieldCommentPlugin <br>
 * 类描述：mybatis generator生成数据库字段注释<br>
 * 创建人：arlen <br>
 * 创建时间：2017年2月9日 上午11:34:31 <br>
 * @version 1.0
 * @author arlen
 */
public class DBColumnCommentPlugin extends PluginAdapter {

	/**
	 * 无任何参数，无需校验
	 */
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	/**
	 * 生成字段时调用方法
	 */
	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		String comment = introspectedColumn.getRemarks();
		if (comment != null && comment.trim().length() > 0) {
			field.addJavaDocLine("/**");
			field.addJavaDocLine(" * " + introspectedColumn.getRemarks());
			field.addJavaDocLine(" */");
		}
		return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}

	@Override
	public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		String comment = introspectedColumn.getRemarks();
		if (comment != null && comment.trim().length() > 0) {
			method.addJavaDocLine("/**");
			method.addJavaDocLine(" * @return " + introspectedColumn.getActualColumnName() + " " + introspectedColumn.getRemarks());
			method.addJavaDocLine(" */");
		}
		return super.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}

	@Override
	public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		String comment = introspectedColumn.getRemarks();
		if (comment != null && comment.trim().length() > 0) {
			Parameter param = method.getParameters().get(0);
			method.addJavaDocLine("/**");
			method.addJavaDocLine(" * @param " + param.getName() + " " + param.getType().getShortName() + " " + introspectedColumn.getRemarks());
			method.addJavaDocLine(" */");
		}
		return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
	}
	
}
