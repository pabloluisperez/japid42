package cn.bran.play;

import java.io.Serializable;

import cn.bran.japid.template.RenderResult;

/**
 * bind a RenderResult with a cache status
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 *
 */
public class CachedRenderResult implements Serializable{
	public CachedItemStatus status;
	public RenderResult rr;
	public CachedRenderResult(CachedItemStatus _status, RenderResult _rr) {
		super();
		this.status = _status;
		this.rr = _rr;
	}
	public boolean isExpired() {
		return this.status.isExpired();
	}
	
}
