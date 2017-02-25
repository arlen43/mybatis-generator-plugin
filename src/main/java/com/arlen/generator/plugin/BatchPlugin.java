/**
 * 项目名: mybatis-generator-plugin
 * 文件名：BatchPlugin.java 
 * 版本信息： V1.0
 * 日期：2017年2月10日 
 */
package com.arlen.generator.plugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import com.arlen.generator.log.LogUtil;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：BatchPlugin <br>
 * 类描述：生成批量插入、批量更新方法<br>
 * 创建人：arlen <br>
 * 创建时间：2017年2月10日 上午10:31:22 <br>
 * @version 1.0
 * @author arlen
 */
public class BatchPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		LogUtil.info("为表 "+introspectedTable.getFullyQualifiedTableNameAtRuntime()+" Dao类 生成批量操作方法 ");
		addBatchMethod(interfaze, "batchInsertSelective", introspectedTable.getBaseRecordType());
		addBatchMethod(interfaze, "batchUpdateByPrimaryKeySelective", introspectedTable.getBaseRecordType());
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	public static void addBatchMethod(Interface interfaze, String methodName, String baseRecordType) {
		Method batchInsertMethod = new Method(methodName);
		batchInsertMethod.setReturnType(FullyQualifiedJavaType.getIntInstance());
		batchInsertMethod.setVisibility(JavaVisibility.PUBLIC);
		
		Parameter param = new Parameter(new FullyQualifiedJavaType("List<" + baseRecordType + ">"), "recordList");
		batchInsertMethod.addParameter(param);
		
		interfaze.addMethod(batchInsertMethod);
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		LogUtil.info("为表 "+introspectedTable.getFullyQualifiedTableNameAtRuntime()+" SqlMap 生成批量操作sql ");
		addBatchInsertElements(document, introspectedTable);
		addBatchUpdateElements(document, introspectedTable);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}
	
    private void addBatchUpdateElements(Document document, IntrospectedTable introspectedTable) {
        XmlElement batchUpdateElement = new XmlElement("update");
        batchUpdateElement.addAttribute(new Attribute("id", "batchUpdateByPrimaryKeySelective")); 
        batchUpdateElement.addAttribute(new Attribute("parameterType", "java.util.List"));

        context.getCommentGenerator().addComment(batchUpdateElement);

        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", "list"));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("index", "index"));
        foreach.addAttribute(new Attribute("separator", ";"));
        batchUpdateElement.addElement(foreach);
        
        StringBuilder sb = new StringBuilder();

        sb.append("update ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        foreach.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("set");
        foreach.addElement(dynamicElement);

        String prefix = "item.";
        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
            XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty(prefix));
            sb.append(" != null");
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            dynamicElement.addElement(isNotNullElement);

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
            sb.append(',');

            isNotNullElement.addElement(new TextElement(sb.toString()));
        }

        boolean and = false;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.setLength(0);
            if (and) {
                sb.append("  and ");
            } else {
                sb.append("where ");
                and = true;
            }

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
            foreach.addElement(new TextElement(sb.toString()));
        }
        document.getRootElement().addElement(batchUpdateElement);
    
	}

	public void addBatchInsertElements(Document document, IntrospectedTable introspectedTable) {
        XmlElement batchInsertElement = new XmlElement("insert");
        batchInsertElement.addAttribute(new Attribute("id", "batchInsertSelective"));
        batchInsertElement.addAttribute(new Attribute("parameterType", "java.util.List"));

        context.getCommentGenerator().addComment(batchInsertElement);

        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("index", "index"));
        foreachElement.addAttribute(new Attribute("separator", ";"));
        batchInsertElement.addElement(foreachElement);

        StringBuilder sb = new StringBuilder();

        sb.append("insert into ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        foreachElement.addElement(new TextElement(sb.toString()));

        XmlElement insertTrimElement = new XmlElement("trim"); //$NON-NLS-1$
        insertTrimElement.addAttribute(new Attribute("prefix", "(")); //$NON-NLS-1$ //$NON-NLS-2$
        insertTrimElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        foreachElement.addElement(insertTrimElement);

        XmlElement valuesTrimElement = new XmlElement("trim"); //$NON-NLS-1$
        valuesTrimElement.addAttribute(new Attribute("prefix", "values (")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesTrimElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        foreachElement.addElement(valuesTrimElement);

        String prefix = "item.";
        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
            if (introspectedColumn.isIdentity()) {
                continue;
            }

            // 如果是唯一自增列或者是java原始类型，都是必选项
            if (introspectedColumn.isSequenceColumn() || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
               
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                sb.append(',');
                insertTrimElement.addElement(new TextElement(sb.toString()));

                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
                sb.append(',');
                valuesTrimElement.addElement(new TextElement(sb.toString()));
                continue;
            }
            
            XmlElement insertNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty(prefix));
            sb.append(" != null"); //$NON-NLS-1$
            insertNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(',');
            insertNotNullElement.addElement(new TextElement(sb.toString()));
            insertTrimElement.addElement(insertNotNullElement);

            XmlElement valuesNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty(prefix));
            sb.append(" != null"); //$NON-NLS-1$
            valuesNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, prefix));
            sb.append(',');
            valuesNotNullElement.addElement(new TextElement(sb.toString()));
            valuesTrimElement.addElement(valuesNotNullElement);
        }
        
        document.getRootElement().addElement(batchInsertElement);
    }

}
