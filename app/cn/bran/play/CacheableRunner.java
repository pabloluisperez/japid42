package cn.bran.play;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import play.mvc.Controller;
import play.mvc.Http.Context.Implicit;
import play.mvc.Http.Request;
import cn.bran.japid.template.ActionRunner;
import cn.bran.japid.template.RenderResult;

/**
 * The class defines a cached adapter to invoke a piece of code.
 * 
 * This class can be used in actions to run a piece of rendering code
 * conditionally
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public abstract class CacheableRunner extends ActionRunner /*implements Externalizable*/{
	protected String ttlAbs = null;
	private String keyString;
	protected boolean noCache = true;
	private boolean readThru;

	/**
	 * 
	 * @param ttl
	 *            time to live for the cached item, format in "1s", "2mn", "3h",
	 *            "4d" for example, "29d" max
	 * @param args
	 *            objects to make a key from their string values
	 */
	public CacheableRunner(String ttl, Object... args) {
		init(ttl, args);
	}

	public CacheableRunner() {
	}

	/**
	 * @param ttl
	 * @param args
	 */
	protected void init(String ttl, Object... args) {
		if (args == null || args.length == 0)
			this.noCache = true;

		if (ttl == null || ttl.trim().length() == 0) {
			this.noCache = true;
		} else {
			this.noCache = false;
			if (ttl.startsWith("-")) {
				this.ttlAbs = ttl.substring(1);
				this.readThru = true;
			} else {
				this.ttlAbs = ttl;
			}
		}

		this.setKeyString(buildKey(args));
	}
	
	/**
	 * a constructor that use a key generated from the current query string to store the render result
	 * @param ttl
	 */
	public CacheableRunner(String ttl) {
		this(ttl, genCacheKey());
	}
	
	/**
	 * the main entry point this action runner
	 */
	@Override
	public RenderResult run() {
		if (!shouldCache()) {
			return render();
		} else {
			// cache in work
			RenderResult rr = null;
			if (!this.readThru) {
				try {
					rr = RenderResultCache.get(getKeyString());
					if (rr != null)
						return rr;
				} catch (ShouldRefreshException e) {
					// let's refresh the cache, flow through
				}
			}

			rr = render();
			
			cacheResult(rr);
			return (rr);
		}
		//
	}

	/**
	 * @return
	 */
	protected boolean shouldCache() {
//		System.out.println("CacheableRunner: should cache: " + !noCache);
		return !this.noCache;
	}

	/**
	 * @param rr1
	 */
	protected void cacheResult(RenderResult rr1) {
//		System.out.println("CacheableRunner: put in cache");
		RenderResultCache.set(getKeyString(), rr1, this.ttlAbs);
	}

	/**
	 * @return
	 */
	public static String buildKey(Object[] keys) {
		String keyString = "";
		for (Object k : keys) {
			keyString += ":" + String.valueOf(k);
		}
		if (keyString.startsWith(":"))
			if ( keyString.length() > 1)
				keyString = keyString.substring(1);
			else
				keyString = "";
		return keyString;
	}

	/**
	 * the render method can either return a RenderResult or throw a JapidResult
	 * 
	 * @return
	 */
	protected abstract RenderResult render();
	
	public static void deleteCache(Object...objects) {
		RenderResultCache.delete(buildKey(objects));
	}

	/**
	 * remove a cached Japid RenderResult from cache indexed by a Japid invoke directive. The key is synthesized from 
	 * the controller name, action name and the arguments. 
	 * 
	 * Keep in mind that the result cache from a regular action annotated with CacheFor by Play is keyed with URL and query string
	 * thus different from what we have here. 
	 * 
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @param controllerClass
	 * @param actionName
	 * @param args the arguments to the action
	 */
	public static <C extends Controller> void deleteCache(Class<C> controllerClass, String actionName, Object... args) {
		Object[] fullArgs = buildCacheKeyParts(controllerClass, actionName, args);
		deleteCache(fullArgs);
	}
	
	public static Object[] buildCacheKeyParts(Class<? extends Controller> controllerClass, String actionName,
			Object... args) {
		// shall we support the id attribute of id of CacheFor?
				Object[] fullArgs = new Object[args.length + 2];
				System.arraycopy(args, 0, fullArgs, 0, args.length);
				fullArgs[args.length] = controllerClass.getName();
				fullArgs[args.length + 1] = actionName;
				return fullArgs;
			}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(this.ttlAbs);
		out.writeUTF(getKeyString());
		out.writeBoolean(this.noCache);
		out.writeBoolean(this.readThru);
	}


	public void readExternal(ObjectInput in) throws IOException {
		this.ttlAbs = in.readUTF();
		setKeyString(in.readUTF());
		this.noCache = in.readBoolean();
		this.readThru = in.readBoolean();
		
	}

	/**
	 * @return the keyString
	 */
	public String getKeyString() {
		return this.keyString;
	}

	/**
	 * @param keyString the keyString to set
	 */
	public void setKeyString(String _keyString) {
		this.keyString = _keyString;
	}

	/**
	 * can be used to generate a key based on the query
	 * 
	 * @return
	 */
	public static String genCacheKey() {
		Request request = Implicit.request();
		return "japidcache:" + request.uri(); // play2 is this unique enough?
	}
	
	
}
