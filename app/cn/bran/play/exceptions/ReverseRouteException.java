package cn.bran.play.exceptions;

import cn.bran.japid.exceptions.JapidRuntimeException;

public class ReverseRouteException extends JapidRuntimeException{
	private static final long serialVersionUID = 3289291726726075602L;
	String action;

	public ReverseRouteException(String _action) {
		super("Reverse Route Exception", "action not found: " + _action);
		this.action = _action;
	}
	
}
