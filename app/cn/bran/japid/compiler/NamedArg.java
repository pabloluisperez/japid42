package cn.bran.japid.compiler;

import japa.parser.ast.expr.Expression;

/**
 * a = "asd", b = 12, etc
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 *
 */
public class NamedArg {
	public NamedArg(Expression target, Expression value) {
		this.name = target.toString(); 
		this.valExpr = value.toString();
	}
	public String name;
	public String valExpr;
	@Override
	public String toString() {
		return this.name + " = " + this.valExpr;
	}
	public String toNamed() {
		return "named(\"" + this.name + "\", " + this.valExpr + ")";
	}
	
	
}
