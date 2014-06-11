/**
 * 
 */
package cn.bran.play.routing;

import java.util.regex.Pattern;

/**
 * The format of the expression is: "{" variable-name [ ":"
 * regular-expression ] "}" The regular-expression part is optional. When
 * the expression is not provided, it defaults to a wildcard matching of one
 * particular segment. In regular-expression terms, the expression defaults
 * to "([]*)"
 * 
 * @author bran
 * 
 */
public class ParamSpec {
	Pattern formatPattern;
	String name;
	String format = "[^/]+"; // the default regex
	Class<?> type;

	/**
	 * @param s
	 */
	public ParamSpec(String s) {
		int i = s.indexOf(':');
		if (i > 0) {
			this.name = s.substring(0, i).trim();
			this.format = s.substring(++i).trim();
		} else {
			this.name = s.trim();
		}
		this.formatPattern = Pattern.compile(this.format);
	}
	
	@Override
	public String toString() {
		return this.type.getSimpleName() + " " + this.name;
	}
}