/**
 * 
 */
package cn.bran.play;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedItemStatus implements Serializable{
	private static final int MIN_ALERT_ADVANCE = 1000;
	private long timein;
	private long ttl;
	private long safeBefore;
	//
	private AtomicBoolean isRefreshing  = new AtomicBoolean(false);
	private boolean expireSoon;

	public CachedItemStatus(long _timein, long _ttl) {
		super();
		this.timein = _timein;
		this.ttl = _ttl;
		long unsafeZone = (long) (_ttl * (1 - RenderResultCache.SAFE_TIME_ZONE));
		if (unsafeZone < MIN_ALERT_ADVANCE) {
			// make a minimum 1s alert advance
			this.safeBefore = _timein + _ttl - MIN_ALERT_ADVANCE;
		}
		else {
			this.safeBefore = _timein + _ttl - unsafeZone;
		}
			
	}

	public CachedItemStatus(long _ttl) {
		this(System.currentTimeMillis(), _ttl);
	}

	boolean expireSoon() {
		return this.expireSoon ? true : (this.expireSoon = System.currentTimeMillis() > this.safeBefore);
	}

	public boolean isRefreshing() {
		return this.isRefreshing.get();
	}

	public void setIsRefreshing() {
		this.isRefreshing.set(true);
	}

	/**
	 * this one mutate the internal state of expiration flag if it will expire
	 * soon.
	 * 
	 * @return
	 */
	public boolean shouldRefresh() {
		if (expireSoon())
			return this.isRefreshing.compareAndSet(false, true);
		else
			return false;
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > this.timein + this.ttl;
	}
}