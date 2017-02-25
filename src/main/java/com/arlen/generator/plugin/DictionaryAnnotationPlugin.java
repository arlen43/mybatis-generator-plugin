/**
 * 项目名: mybatis-generator-plugin
 * 文件名：DictionaryAnnotationPlugin.java 
 * 版本信息： V1.0
 * 日期：2017年2月9日 
 */
package com.arlen.generator.plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import com.arlen.generator.log.LogUtil;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：DictionaryAnnotationPlugin <br>
 * 类描述：字典项，生成code对应的name属性和对应的字典注解<br>
 * 创建人：arlen <br>
 * 创建时间：2017年2月9日 下午2:06:17 <br>
 * @version 1.0
 * @author arlen
 */
public class DictionaryAnnotationPlugin extends PluginAdapter {

	private final static Map<String, String> tableCacheMap = new ConcurrentHashMap<String, String>();
	
	private boolean needGenerate = false;
	
	public boolean validate(List<String> warnings) {
		return true;
	}
	

	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		List<IntrospectedColumn> allColumnList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : allColumnList) {
			DictNeedResult dictResult = needDictGenerate(introspectedColumn.getRemarks());
			if (needGenerate = dictResult.needFlag) {
				break;
			}
		}
		super.initialized(introspectedTable);
	}

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		if (needGenerate) {
			interfaze.addImportedType(new FullyQualifiedJavaType("com.hongkun.dictionary.annotation.DictionaryFill"));
		}
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	private void addAnnotationToMethod(Method method, IntrospectedTable introspectedTable) {
		if (needGenerate) {
			method.addAnnotation("@DictionaryFill("+PluginUtil.getModelSimpleName(introspectedTable)+".class)");
		}
	}
	
	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		addAnnotationToMethod(method, introspectedTable);
		return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		addAnnotationToMethod(method, introspectedTable);
		return super.clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
	}

	@Override
	public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		addAnnotationToMethod(method, introspectedTable);
		return super.clientSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		
		String comment = introspectedColumn.getRemarks();
		DictNeedResult dictResult = needDictGenerate(comment);
		if (!dictResult.needFlag) {
			return true;
		}
		
		// 多个字典项，只import一次包
		String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
		if (!tableCacheMap.containsKey(tableName)) {
			tableCacheMap.put(tableName, "");
			topLevelClass.addAnnotation("@DictionaryFill("+PluginUtil.getModelSimpleName(introspectedTable)+".class)");
			topLevelClass.addImportedType("com.hongkun.dictionary.annotation.Dictionary");
			topLevelClass.addImportedType("com.hongkun.dictionary.annotation.DictionaryFill");
		}
		
		String fieldName = field.getName();
		if (fieldName.endsWith("code") || fieldName.endsWith("Code")) {
			fieldName = fieldName.substring(0, fieldName.length() - 4);
		} else if (fieldName.endsWith("co") || fieldName.endsWith("Co")) {
			fieldName = fieldName.substring(0, fieldName.length() - 2);
		}
		
		String dictIndex = dictResult.dictIndex;
		field.addAnnotation("@Dictionary(dictIndex=\""+dictIndex+"\", cnFlag="+dictResult.cnFlag+", enFlag="+dictResult.enFlag+")");
		
		if (dictResult.cnFlag) {
			String fieldNameCN = fieldName + "NameCN";
			PluginUtil.addClazzField(topLevelClass, fieldNameCN, FullyQualifiedJavaType.getStringInstance(), comment);
			PluginUtil.addClazzGetMethod(topLevelClass, fieldNameCN, FullyQualifiedJavaType.getStringInstance(), comment);
			PluginUtil.addClazzSetMethod(topLevelClass, fieldNameCN, FullyQualifiedJavaType.getStringInstance(), comment);
		}
		
		if (dictResult.enFlag) {
			String fieldNameEN = fieldName + "NameEN";
			PluginUtil.addClazzField(topLevelClass, fieldNameEN, FullyQualifiedJavaType.getStringInstance(), comment);
			PluginUtil.addClazzGetMethod(topLevelClass, fieldNameEN, FullyQualifiedJavaType.getStringInstance(), comment);
			PluginUtil.addClazzSetMethod(topLevelClass, fieldNameEN, FullyQualifiedJavaType.getStringInstance(), comment);
		}

		return true;
	}

	private class DictNeedResult {
		private String dictIndex;
		private boolean cnFlag = false;
		private boolean enFlag = false;
		private boolean needFlag;
		
		public DictNeedResult(boolean needFlag) {
			this.needFlag = needFlag;
		}
	}
	
	// 商品货值币制。Dict[currency,cn,en]
	private DictNeedResult needDictGenerate(String comment) {
		DictNeedResult dictResult = new DictNeedResult(false);
		try {
			if (comment == null || comment.trim().length() == 0) {
				return dictResult;
			}
			
			Pattern pattern = Pattern.compile("Dict\\[.*?\\]");
			Matcher matcher = pattern.matcher(comment);
			if (!matcher.find()) {
				return dictResult;
			}
			
			LogUtil.info("为注释'"+comment+"'生成相应的中英文名字段。");
			String result = matcher.group(0);
			String configs = result.substring(result.indexOf("[")+1, result.indexOf("]"));
			String[] configArr = configs.split("\\,");
			dictResult.dictIndex = configArr[0];
			dictResult.needFlag = true;
			if ("cn".equalsIgnoreCase(configArr[1])) {
				dictResult.cnFlag = true;
			}
			if ("en".equalsIgnoreCase(configArr[2])) {
				dictResult.enFlag = true;
			}
		} catch (Exception e) {
			LogUtil.error("解析注释"+comment+"出错。" + e.getMessage());
		}
		return dictResult;
	}
	
}
