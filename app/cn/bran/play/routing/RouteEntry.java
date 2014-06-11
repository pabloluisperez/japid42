/**
 * 
 */
package cn.bran.play.routing;

/**
 * @author bran
 *
 */
public class RouteEntry {
	public String verb, path, action;

	public RouteEntry(String _verb, String _path, String _action) {
		super();
		this.verb = _verb;
		this.path = _path;
		this.action = _action;
	}
	
	
}
