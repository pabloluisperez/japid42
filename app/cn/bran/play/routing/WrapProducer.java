/**
 * 
 */
package cn.bran.play.routing;

import play.mvc.SimpleResult;

/**
 * @author bran
 * 
 */
public class WrapProducer extends SimpleResult {
	private String produces;
	private SimpleResult r;

	public WrapProducer(String _produces, SimpleResult _r) {
		this.produces = _produces;
		this.r = _r;
	}

//	public play.api.mvc.Result getWrappedResult() {
//		return r.getWrappedResult().as(produces);
//	}

	/* (non-Javadoc)
	 * @see play.mvc.SimpleResult#getWrappedSimpleResult()
	 */
	@Override
	public play.api.mvc.SimpleResult getWrappedSimpleResult() {
		return this.r.getWrappedSimpleResult().as(this.produces);
	}
}