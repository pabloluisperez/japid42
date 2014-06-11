package cn.bran.play;


public interface AltCache {

	void set(String key, CachedRenderResult rr, String ttl);

	CachedRenderResult get(String key);

	void delete(String key);
}
