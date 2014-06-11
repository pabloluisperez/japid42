package cn.bran.japid.compiler;

import cn.bran.japid.compiler.JapidParser.Token;

class TokenPair {
	Token token;
	String source;
	public TokenPair(Token _token, String _source) {
		super();
		this.token = _token;
		this.source = _source;
	}
}