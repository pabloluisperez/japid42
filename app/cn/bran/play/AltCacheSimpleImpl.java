package cn.bran.play;

import java.util.concurrent.ConcurrentHashMap;

/**
 * a CHM based simple cache for resultset. can also use the Google collection
 * which comes with concurrency control, expiration time, etc, which is a 500k
 * jar
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class AltCacheSimpleImpl implements AltCache {

	ConcurrentHashMap<String, CachedRenderResult> cache = new ConcurrentHashMap<String, CachedRenderResult>();

	@Override
	public CachedRenderResult get(String key) {
		CachedRenderResult crr = this.cache.get(key);
		if (crr.isExpired()) {
			this.cache.remove(key);
			return null;
		} else {
			return crr;
		}
	}

	@Override
	public void set(String key, CachedRenderResult rr, String ttl) {
		this.cache.put(key, rr);
	}

	@Override
	public void delete(String key) {
		this.cache.remove(key);
	}

}
