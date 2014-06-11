/**
 * 
 */
package cn.bran.japid.template;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An ScheduledThreadPoolExecutor that uses one thread and cancels previous future work when new job is scheduled
 * 
 * @author bran
 *
 */
public class AmmendableScheduledExecutor extends ScheduledThreadPoolExecutor {
	ScheduledFuture<?> schedule;

	public AmmendableScheduledExecutor() {
		super(1);
	}

	public void schedule(Runnable r) {
		if (this.schedule != null) {
			this.schedule.cancel(false);
		}
		this.schedule = super.schedule(r, 2, TimeUnit.SECONDS);
	}
	
}
