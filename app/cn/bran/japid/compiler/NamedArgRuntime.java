package cn.bran.japid.compiler;


/**
 * a = "asd", b = 12, etc
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 *
 */
public class NamedArgRuntime {
	public NamedArgRuntime(String _name, Object _val) {
		this.name = _name;
		this.val = _val;
	}
	public String name;
	public Object val;
	@Override
	public String toString() {
		return this.name + " = " + this.val;
	}
	
}
