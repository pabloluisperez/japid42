/**
 * 
 */
package cn.bran.play.routing;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.reflections.ReflectionUtils;

import play.libs.F.Tuple;

import com.google.common.base.Predicates;

/**
 * @author bran
 * 
 */
public class RouterUtils {

	// old perhaps incorrect impl. was looking for subgroups
	// static List<String> findAllIn(Pattern p, String string) {
	// List<String> ret = new ArrayList<String>();
	// Matcher matcher = p.matcher(string);
	// while (matcher.find()) {
	// int c = matcher.groupCount();
	// for (int i = 1; i <= c; i++) {
	// String group = matcher.group(i);
	// ret.add(group);
	// }
	// }
	// return ret;
	// }
	//
	// find class level annotation
	/**
	 * find if the uri contains variables
	 * 
	 * @author Bing Ran (bing.ran@gmail.com)
	 * @param uri
	 * @param path
	 * @return true if it contains, false otherwise
	 */
	static boolean isResourcePath(String uri, String path) {
		String r = path.replaceAll(JaxrsRouter.urlParamCapture, "(.*)");
		Pattern p = Pattern.compile(r);
		if (!RegMatch.findAllIn(p, uri).isEmpty()){
			return true;
		}
		return false;
	}

	static Class<? extends Annotation> findHttpMethodAnnotation(String httpMethod) {
		if (httpMethod.equals("GET"))
			return GET.class;
		else if (httpMethod.equals("POST"))
			return POST.class;
		else if (httpMethod.equals("POST"))
			return POST.class;
		else if (httpMethod.equals("PUT"))
			return PUT.class;
		else if (httpMethod.equals("DELETE"))
			return DELETE.class;
		else if (httpMethod.equals("HEAD"))
			return HEAD.class;
		else if (httpMethod.equals("OPTIONS"))
			return OPTIONS.class;
		else
			return null;
	}

	static TargetClassWithPath findLongestMatch(Set<Class<?>> classes, play.mvc.Http.RequestHeader reqHeader,
			String appPath) {
		TargetClassWithPath ret = null;
		String rpath = reqHeader.path();
		for (Class<?> c : classes) {
			String path = appPath + JaxrsRouter.prefixSlash(c.getAnnotation(Path.class).value());
			if (isResourcePath(rpath, path)) {
				if (ret == null || path.length() > ret._2().length()) {
					ret = new TargetClassWithPath(c, path);
				}
			}
		}
		return ret;
	}

	static RouterClass findLongestMatch(List<RouterClass> classes, play.mvc.Http.RequestHeader reqHeader) {
		RouterClass ret = null;
		String rpath = reqHeader.path();
		for (RouterClass c : classes) {
			if (!RegMatch.findAllIn(c.absPathPatternForValues, rpath).isEmpty())
				if (ret == null || c.absPath.length() > ret.absPath.length()) {
					ret = c;
				}
		}
		return ret;
	}

	static java.util.Set<Method> relevantMethods(Class<?> c, Class<? extends Annotation> a) {
		return ReflectionUtils.getAllMethods(c, Predicates.and(ReflectionUtils.withAnnotation(a)));
	}

	/**
	 * 
	 * @author Bing Ran (bing.ran@gmail.com)
	 * @param rootPath
	 * @param uri
	 * @param methods
	 * @param contentType
	 * @return a tuple of method and its param name-value map
	 */
	static Tuple<Method, Map<String, String>> findMethodAndGenerateContext(String rootPath, String uri,
			java.util.Set<Method> methods, String contentType) {
		// include rootPath only for non root slash class level @Path values or
		// if the uri is root slash
		String[] consumeTypes = new String[] { contentType };
		String rootPathPrefix = (rootPath == "/" && uri != "/") ? "" : rootPath;

		for (Method m : methods) {

			Consumes consumes = m.getAnnotation(Consumes.class);
			if (consumes != null) {
				consumeTypes = consumes.value();
			}

			boolean contained = false;
			for (String c : consumeTypes) {
				if (c.equals(contentType)) {
					contained = true;
					break;
				}
			}
			if (contained) {
				Path p = m.getAnnotation(Path.class);
				String mPath = p == null ? "" : JaxrsRouter.prefixSlash(p.value());
				String fullMethodPath = (rootPathPrefix + mPath).replace("//", "/");

				// List<String> matches =
				// RegMatch.findAllIn(Pattern.compile(fullMethodPath), uri );
				// if (matches.size() > 0) {
				// matches = RegMatch.findAllIn(Pattern.compile(uri),
				// fullMethodPath);
				// if (matches.size() > 0) {
				// return new Tuple<Method, Map<String, String>>(m, new
				// HashMap<String, String>());
				// }
				// }

				// bran let's do exact match
				if (uri.equals(fullMethodPath)) {
					return new Tuple<Method, Map<String, String>>(m, new HashMap<String, String>());
				} else // any variables?
				if (fullMethodPath.contains("{") && fullMethodPath.contains("}")) {
					String combinedReg = fullMethodPath.replaceAll(JaxrsRouter.urlParamCapture, "\\\\{(.*)\\\\}");// .r;
					Pattern r = Pattern.compile(combinedReg);
					List<RegMatch> rootParamNameMatches = RegMatch.findAllMatchesIn(r, fullMethodPath);
					List<String> rootParamNames = new ArrayList<String>();
					for (RegMatch rm : rootParamNameMatches) {
						rootParamNames.addAll(rm.subgroups);
					}

					combinedReg = fullMethodPath.replaceAll(JaxrsRouter.urlParamCapture, "(.*)");
					r = Pattern.compile(combinedReg);
					List<RegMatch> rootParamValueMatches = RegMatch.findAllMatchesIn(r, uri);
					List<String> rootParamValues = new ArrayList<String>();
					for (RegMatch rm : rootParamValueMatches) {
						rootParamValues.addAll(rm.subgroups);
					}

					Map<String, String> methodMetaData = new java.util.HashMap<String, String>();
					int c = 0;
					for (String name : rootParamNames) {
						try {
							methodMetaData.put(name, rootParamValues.get(c++));
						} catch (Exception e) {
							methodMetaData.put(name, "");
						}
					}

					if (rootParamValues.size() > 0) {
						return new Tuple<Method, Map<String, String>>(m, methodMetaData);
					}
				}
			}
		}
		return null;
	}
	
	static play.mvc.Result invokeMethod(Class<?> targetClass, play.GlobalSettings global, Method method,
			Map<String, Object> extractedArgs, play.mvc.Http.RequestHeader r) {
		try {
			Object[] argValues = new Object[0];
			List<Object> argVals = new ArrayList<Object>();
			Annotation[][] annos = method.getParameterAnnotations();

			for (Annotation[] ans : annos) {
				PathParam pathParam = null;
				QueryParam queryParam = null;

				for (Annotation an : ans) {
					if (an instanceof PathParam)
						pathParam = (PathParam) an;
					else if (an instanceof QueryParam)
						queryParam = (QueryParam) an;
				}
				if (pathParam != null) {
					Object v = extractedArgs.get(pathParam.value());
					if (v != null)
						argVals.add(v);
					else
						throw new IllegalArgumentException("can not find annotation value for argument "
								+ pathParam.value() + "in " + targetClass + "#" + method);
				} else if (queryParam != null) {
					String queryString = r.getQueryString(queryParam.value());
					argVals.add(queryString); // string type conversion?
				} else
					throw new IllegalArgumentException(
							"can not find an appropriate JAX-RC annotation for an argument for method:" + targetClass
									+ "#" + method);
			}
			argValues = argVals.toArray(argValues);
			return (play.mvc.Result) method.invoke(global.getControllerInstance(targetClass), argValues);
		} catch (InvocationTargetException cause) {
			System.err.println("Exception occured while trying to invoke: " + targetClass.getName() + "#"
					+ method.getName() + " with " + extractedArgs + " for uri:" + r.path());
			throw new RuntimeException(cause.getCause());
		} catch (Exception e) {
			throw new RuntimeException(e.getCause());
		}
	}

	static Set<Class<?>> classes(ClassLoader parentClassloader) {
		return JaxrsRouter.getControllersWithPath();
//		Set<String> typesAnnotatedWith = JaxrsRouter.ref.getStore().getTypesAnnotatedWith(Path.class.getName());
//		Set<Class<?>> clazz = new HashSet<Class<?>>();
//		for (String t : typesAnnotatedWith) {
//			try {
//				clazz.add(Class.forName(t, true, parentClassloader));
//			} catch (ClassNotFoundException e) {
////				throw new RuntimeException(e);
//			}
//		}
//		return clazz;
	}

}
