package cn.bran.japid.rendererloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.bran.japid.template.JapidRenderer;
import cn.bran.japid.template.JapidTemplateBaseWithoutPlay;

/**
 * The template class loader that detects changes and recompile on the fly.
 * 
 * 1. whenever changes detected, clear the global class cache. 2. Only redefine
 * the class to load, which will lead to define all the dependencies. All the
 * dependencies must be defined by the same class classloader or
 * InvalidAccessException. 3. The main program will call the loadClass once for
 * each of the classes defined in one classloader.
 * 
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class TemplateClassLoader extends ClassLoader {
	// the per classloader class cache
	private Map<String, Class<?>> localClasses = new ConcurrentHashMap<String, Class<?>>();

	private ClassLoader parentClassLoader;

	public TemplateClassLoader(ClassLoader _parentClassLoader) {
		super(TemplateClassLoader.class.getClassLoader());
//		super(parentClassLoader);
		this.parentClassLoader = _parentClassLoader;
		
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!name.startsWith(JapidRenderer.JAPIDVIEWS)) {
			Class<?> cl = this.parentClassLoader.loadClass(name);
			if (cl != null) {
				return cl;
			}
			Class<?> superClass = super.loadClass(name);
			return superClass;
		}
		String oid = "[TemplateClassLoader@" + Integer.toHexString(hashCode()) + "]";

		Class<?> cla = this.localClasses.get(name);
		if (cla != null) {
			return cla;
		}

		RendererClass rc = JapidRenderer.japidClasses.get(name);
		if (rc == null)
			throw new ClassNotFoundException("Japid could not resolve class: " + name);

		if (!rc.getClassName().contains("$")) {
			// added just in time compiling
			JapidRenderer.recompile(rc);
		}
		
		byte[] bytecode = rc.bytecode;

		if (bytecode == null) {
			throw new RuntimeException(oid + " could not find the bytecode for: " + name);
		}

		// the defineClass method will load the classes of the dependency
		// classes.
		@SuppressWarnings("unchecked")
		Class<? extends JapidTemplateBaseWithoutPlay> cl = (Class<? extends JapidTemplateBaseWithoutPlay>) defineClass(
				name, bytecode, 0, bytecode.length);
		rc.setClz(cl);
		this.localClasses.put(name, cl);
		rc.setLastDefined(System.currentTimeMillis());
		return cl;
	}

	/**
	 * Search for the byte code of the given class.
	 */
	protected byte[] getClassDefinition(String _name) {
		String name = _name.replace(".", "/") + ".class";
		InputStream is = getResourceAsStream(name);
		if (is == null) {
//			is = parentClassLoader.getResourceAsStream(name);
//			if (is == null)
				return null;
		}
		
//		System.out.println("got class def for: " + name);
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream(8192);
			byte[] buffer = new byte[8192];
			int count;
			while ((count = is.read(buffer, 0, buffer.length)) > 0) {
				os.write(buffer, 0, count);
			}
			return os.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}