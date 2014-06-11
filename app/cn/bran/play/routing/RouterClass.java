/**
 * 
 */
package cn.bran.play.routing;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withReturnType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import play.libs.F.Tuple;
import play.mvc.Result;

import cn.bran.japid.util.JapidFlags;
import cn.bran.play.GlobalSettingsWithJapid;

import com.google.common.base.Predicates;

/**
 * @author bran
 * 
 */
public class RouterClass {
	String absPath;
	Pattern absPathPatternForValues;

//	List<String> routeTable = new ArrayList<String>();
	
	Class<?> clz;
	private String path;
	List<RouterMethod> routerMethods = new ArrayList<RouterMethod>();

	/**
	 * @param cl
	 */
	public RouterClass(Class<?> cl, String appPath) {
		this.clz = cl;
		this.path = cl.getAnnotation(Path.class).value();
		if (this.path.length() == 0) {
			// auto-pathing. using the class full name minus the "controller." part as the path
			String cname = cl.getName();
			if (cname.startsWith(JaxrsRouter.routerPackage + "."))
				cname = cname.substring((JaxrsRouter.routerPackage + ".").length());
			this.path = cname;
		}
		this.absPath = appPath + JaxrsRouter.prefixSlash(this.path);

		String r = this.absPath.replaceAll(JaxrsRouter.urlParamCapture, "(.*)");
		this.absPathPatternForValues = Pattern.compile(r);

//		@SuppressWarnings("unchecked")
//		Predicate<AnnotatedElement> and = Predicates.or(withAnnotation(GET.class),
//				withAnnotation(POST.class), withAnnotation(PUT.class),
//				withAnnotation(DELETE.class), withAnnotation(HEAD.class),
//				withAnnotation(OPTIONS.class));
//		Set<Method> allMethods = getAllMethods(cl, and);
		// let's allow any methods
		Set<Method> allMethods = getAllMethods(cl, Predicates.and(withModifier(Modifier.STATIC), withReturnType(Result.class)));
		for (Method m : allMethods) {
			if (m.getDeclaringClass() == cl)
				this.routerMethods.add(new RouterMethod(m, this.absPath));
		}
	}

	public Tuple<Method,Object[]> findMethodAndGenerateArgs(play.mvc.Http.RequestHeader r) {
		String uri = r.path();

		String contentType = "";
		String[] ct = r.headers().get("Content-Type");
		if (ct != null && ct.length > 0)
			contentType = ct[0];
		
		try {
			for (RouterMethod m : this.routerMethods) {
				if (m.containsConsumeType(contentType) 
						&& m.supportHttpMethod(r.method())
						&& m.matchURI(uri)) {
					return new Tuple<Method, Object[]>(m.meth, m.extractArguments(r));
				}
			}
		} catch (Exception e) {
			if (GlobalSettingsWithJapid._app.isDev())
				JapidFlags.warn(e.toString());
			else
				JapidFlags.debug(e.toString());
		}
		
		return null;
	}

	
	@Override
	public String toString() {
		String ret = "";
		for (RouterMethod m : this.routerMethods) {
			ret += m.toString() + "\n";
		}
		return ret;
	}
	
	public List<RouteEntry> getRouteTable(){
		List<RouteEntry> entries  = new ArrayList<RouteEntry>();
		for (RouterMethod m : this.routerMethods) {
			entries.add(m.getRouteEntry());
		}
		return entries;
	}
}
