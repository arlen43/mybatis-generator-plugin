/**
 * 项目名: mybatis-generator-plugin
 * 文件名：ClientRenamePlugin.java 
 * 版本信息： V1.0
 * 日期：2017年2月13日 
 */
package com.arlen.generator.plugin;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/** 
 * 项目名称：mybatis-generator-plugin <br>
 * 类名称：ClientRenamePlugin <br>
 * 类描述：生成的java client重命名<br>
 * 创建人：arlen <br>
 * 创建时间：2017年2月13日 下午1:47:36 <br>
 * @version 1.0
 * @author arlen
 */
public class RenameClientClassPlugin extends PluginAdapter {

	private String searchRegex;
    private String replaceString;
    private String prefix;
    private Pattern pattern;

    public boolean validate(List<String> warnings) {

        searchRegex = properties.getProperty("searchRegex");
        replaceString = properties.getProperty("replaceString");
        prefix = properties.getProperty("prefix", "");

        boolean valid = stringHasValue(searchRegex)&& stringHasValue(replaceString);

        if (valid) {
            pattern = Pattern.compile(searchRegex);
        } else {
            if (!stringHasValue(searchRegex)) {
                warnings.add(getString("ValidationError.18", //$NON-NLS-1$
                        "RenameClientClassPlugin", //$NON-NLS-1$
                        "searchRegex")); //$NON-NLS-1$
            }
            if (!stringHasValue(replaceString)) {
                warnings.add(getString("ValidationError.18", //$NON-NLS-1$
                        "RenameExampleClassPlugin", //$NON-NLS-1$
                        "replaceString")); //$NON-NLS-1$
            }
        }
        return valid;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String oldType = introspectedTable.getMyBatis3JavaMapperType();
        System.out.println("--------- " + oldType);
        Matcher matcher = pattern.matcher(oldType);
        oldType = matcher.replaceAll(replaceString);
        int lastDot = oldType.lastIndexOf(".");
        String pre = oldType.substring(0, lastDot+1);
        String last = oldType.substring(lastDot+1);
        if (stringHasValue(prefix)) {
        	last = prefix + last;
        }
        introspectedTable.setMyBatis3JavaMapperType(pre + last);
    }

	public static void main(String[] args) {
		String regex = "Example";
		String oldType = "Order.Example";
		int lastDot = oldType.lastIndexOf(".");
		System.out.println(lastDot);
		String pre = oldType.substring(0, lastDot+1);
		System.out.println(pre);
		System.out.println(oldType.substring(lastDot+1));
		
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(oldType);
		oldType = matcher.replaceAll("Critiera");
		System.out.println(oldType);
	}
}
