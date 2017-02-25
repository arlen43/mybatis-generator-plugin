/**
 * 项目名: mybatis-generator-plugin
 * 文件名：PagePlugin.java 
 * 版本信息： V1.0
 * 日期：2017年2月9日 
 */
package com.arlen.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.arlen.generator.log.LogUtil;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：PagePlugin <br>
 * 类描述：生成mapper、java类带分页<br>
 * 创建人：arlen <br>
 * 创建时间：2017年2月9日 下午6:56:25 <br>
 * @version 1.0
 * @author arlen
 */
public class PagePlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		LogUtil.info("为表 "+introspectedTable.getFullyQualifiedTableNameAtRuntime()+" Example类 生成分页参数 ");
		String fieldName = "pageFlag", comment = "是否分页";
		PluginUtil.addClazzField(topLevelClass, fieldName, FullyQualifiedJavaType.getBooleanPrimitiveInstance(), comment);
		PluginUtil.addClazzGetMethod(topLevelClass, fieldName, FullyQualifiedJavaType.getBooleanPrimitiveInstance(), comment);
		PluginUtil.addClazzSetMethod(topLevelClass, fieldName, FullyQualifiedJavaType.getBooleanPrimitiveInstance(), comment);
		
		fieldName = "offset"; comment = "要查的起始行";
		PluginUtil.addClazzField(topLevelClass, fieldName, FullyQualifiedJavaType.getIntInstance(), comment);
		PluginUtil.addClazzGetMethod(topLevelClass, fieldName, FullyQualifiedJavaType.getIntInstance(), comment);
		PluginUtil.addClazzSetMethod(topLevelClass, fieldName, FullyQualifiedJavaType.getIntInstance(), comment);
		
		fieldName = "limit"; comment = "要查的总行数";
		PluginUtil.addClazzField(topLevelClass, fieldName, FullyQualifiedJavaType.getIntInstance(), comment);
		PluginUtil.addClazzGetMethod(topLevelClass, fieldName, FullyQualifiedJavaType.getIntInstance(), comment);
		PluginUtil.addClazzSetMethod(topLevelClass, fieldName, FullyQualifiedJavaType.getIntInstance(), comment);
		
		// 生成的Example类的clear重置新加的三个参数
		List<Method> methods = topLevelClass.getMethods();
		for (Method method : methods) {
			if ("clear".equals(method.getName())) {
				List<String> bodyLineList = method.getBodyLines();
				bodyLineList.add("pageFlag = false;");
				bodyLineList.add("offset = 0;");
				bodyLineList.add("limit = 0;");
			}
		}
		return true;
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		LogUtil.info("为表 "+introspectedTable.getFullyQualifiedTableNameAtRuntime()+" SelectByExampleWithoutBLOBs 生成分页参数 ");
		addPageSql(element);
		return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		LogUtil.info("为表 "+introspectedTable.getFullyQualifiedTableNameAtRuntime()+" SelectByExampleWithBLOBs 生成分页参数 ");
		addPageSql(element);
		return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		LogUtil.info("为表 "+introspectedTable.getFullyQualifiedTableNameAtRuntime()+" SelectAllElement 生成分页参数 ");
		addPageSql(element);
		return super.sqlMapSelectAllElementGenerated(element, introspectedTable);
	}
	
	private void addPageSql(XmlElement element) {
		XmlElement pageElement = new XmlElement("if");
		pageElement.addAttribute(new Attribute("test", "pageFlag"));
		pageElement.addElement(new TextElement("limit #{limit,jdbcType=INTEGER} offset #{offset,jdbcType=INTEGER}"));
		element.addElement(element.getElements().size(), pageElement);
	}
	
}
