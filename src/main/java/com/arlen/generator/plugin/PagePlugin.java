/**
 * 项目名: mybatis-generator-plugin
 * 文件名：PagePlugin.java 
 * 版本信息： V1.0
 * 日期：2017年2月9日 
 */
package com.arlen.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.arlen.generator.log.LogUtil;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：PagePlugin <br>
 * 类描述：生成mapper、java类带分页，同时修改Example类的相关内容<br>
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
		
		// 修改GeneratedCriteria类的addCriterion方法为public，实现手动添加condition的目的
		List<InnerClass> innerClassList = topLevelClass.getInnerClasses();
		for (InnerClass innerClass : innerClassList) {
			if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
				// 遍历方法
				List<Method> innerMethods = innerClass.getMethods();
				for (Method method : innerMethods) {
					if ("addCriterion".equals(method.getName())) {
						method.setVisibility(JavaVisibility.PUBLIC);
						method.addJavaDocLine("/**");
						method.addJavaDocLine(" * 手动添加条件，不建议直接调用");
						method.addJavaDocLine(" */");
					}
				}
			}
		}
		
		// 添加默认del_flag = 0条件的 or、createCriteria方法
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

	/**
	 * bug 修复，如果有别名，默认生成的是有问题的<br>
	 * 默认： delete from table_a a;<br>
	 * 修改后：delete a from table_a a;
	 */
	@Override
	public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		Element firstEle = element.getElements().get(0);
		if (firstEle instanceof TextElement) {
			FullyQualifiedTable qualifiedTable = introspectedTable.getFullyQualifiedTable();
			element.getElements().set(0, new TextElement("delete " + qualifiedTable.getAlias() + " from " + qualifiedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
		}
		return super.sqlMapDeleteByExampleElementGenerated(element, introspectedTable);
	}
	
	
}
