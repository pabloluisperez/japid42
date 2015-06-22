package cn.bran.japid.rendererloader;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;

import cn.bran.japid.template.JapidTemplateBaseWithoutPlay;

public class RendererClass implements Serializable{
	private static final long serialVersionUID = -2039838560731729110L;
	String className;
	String javaSourceCode; // the java file
	String japidSourceCode; // japid code
	
	private long lastUpdated;
	byte[] bytecode;
	transient Class<? extends JapidTemplateBaseWithoutPlay> clz;
//	ClassLoader cl;
	private File scriptFile; // the original template source file
	// the constructor cache
	transient private Constructor<? extends JapidTemplateBaseWithoutPlay> constructor;
	private long lastCompiled;
	private long lastDefined;
	private long scriptTimestamp;
	// who has contributed this class. options: "jar"
	private String contributor = "";
	
//	public ClassLoader getCl() {
//		return cl;
//	}
//	public void setCl(ClassLoader cl) {
//		this.cl = cl;
//	}
	public Class<? extends JapidTemplateBaseWithoutPlay> getClz() {
		return this.clz;
	}
	public void setClz(Class<? extends JapidTemplateBaseWithoutPlay> _clz) {
		this.clz = _clz;
		if (_clz == null)
			this.setConstructor(null);
		else 
			try {
				if (!_clz.getName().contains("$"))
					this.setConstructor(_clz.getConstructor(StringBuilder.class));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	}
	public String getClassName() {
		return this.className;
	}
	public void setClassName(String _className) {
		this.className = _className;
	}
	public String getJavaSourceCode() {
		return this.javaSourceCode;
	}
	public void setJavaSourceCode(String sourceCode) {
		this.javaSourceCode = sourceCode;
	}

	public void setLastUpdated(long lastUpdates) {
		this.lastUpdated = lastUpdates;
	}
	public byte[] getBytecode() {
		return this.bytecode;
	}
	public void setBytecode(byte[] _bytecode) {
		this.bytecode = _bytecode;
	}
	
	public void clear() {
		this.bytecode = null;
	}
	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @param srcFile
	 */
	public void setScriptFile(File srcFile) {
		if (srcFile.exists()) {
			this.scriptFile = srcFile;
			this.scriptTimestamp = srcFile.lastModified();
		}
		else
			throw new RuntimeException("japid script does not exist: " + srcFile.getAbsolutePath());
	}
	/**
	 * @return the srcFile
	 */
	private File getScriptFile() {
		return this.scriptFile;
	}
	/**
	 * @return the javaSourceCode
	 */
	public String getJapidSourceCode() {
		return this.japidSourceCode;
	}
	/**
	 * @param javaSourceCode the javaSourceCode to set
	 */
	public void setJapidSourceCode(String oriSourceCode) {
		this.japidSourceCode = oriSourceCode;
	}
	/**
	 * @return the lastUpdated
	 */
	public long getLastUpdated() {
		return this.lastUpdated;
	}

	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @param lineNumber
	 * @return
	 */
	public int mapJavaLineToJapidScriptLine(int lineNumber) {
		String jsrc = getJavaSourceCode();
		String[] splitSrc = jsrc.split("\n");
		String line = splitSrc[lineNumber - 1];
		// can we have a line marker?
		int lineMarker = line.lastIndexOf("// line ");
		if (lineMarker > 0){ 
			String trim = line.substring(lineMarker + 8).trim();
			int commaPosition = trim.indexOf(",");
			if(commaPosition>=0){
				trim = trim.substring(0, trim.indexOf(","));
			}
			return Integer.parseInt(trim);
		}
		return -1;	

	}
	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @param ctor
	 */
	public  void setConstructor(Constructor<? extends JapidTemplateBaseWithoutPlay> ctor) {
		this.constructor = ctor;
	}
	/**
	 * @return the constructor
	 */
	public Constructor<? extends JapidTemplateBaseWithoutPlay> getConstructor() {
		return this.constructor;
	}
	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @param currentTimeMillis
	 */
	public void setLastCompiled(long currentTimeMillis) {
		this.lastCompiled = currentTimeMillis;
	}
	/**
	 * @return the lastCompiled
	 */
	public long getLastCompiled() {
		return this.lastCompiled;
	}
	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @param currentTimeMillis
	 */
	public void setLastDefined(long currentTimeMillis) {
		this.lastDefined = currentTimeMillis;
	}
	/**
	 * @return the lastDefined
	 */
	public long getLastDefined() {
		return this.lastDefined;
	}

	@Override
	public String toString() {
		return "Japid Renderer class wrapper for: " + this.getClassName() + ". Source file: " + this.scriptFile;
	}
	/**
	 * @return the scripTimestamp
	 */
	public long getScriptTimestamp() {
		return this.scriptTimestamp;
	}
	/**
	 * @param scripTimestamp the scripTimestamp to set
	 */
	public void setScriptTimestamp(long _scriptTimestamp) {
		this.scriptTimestamp = _scriptTimestamp;
	}
	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @return
	 */
	public String getScriptPath() {
		File scriptFile1 = getScriptFile();
		if (scriptFile1 != null && scriptFile1.exists()) {
			return scriptFile1.getPath();
		}
		return this.className;
	}
	/**
	 * set the contributor. Options are: "jar"
	 *  
	 * @author Bing Ran (bing.ran@gmail.com)
	 * @param string
	 */
	public void setContributor(String string) {
		this.contributor = string;
	}
	/**
	 * @author Bing Ran (bing.ran@gmail.com)
	 * @return
	 */
	public String getContributor() {
		return this.contributor;
	}
	/**
	 * @author Bing Ran (bing.ran@gmail.com)
	 * @return
	 */
	public boolean fromJar() {
		if (this.contributor != null && this.contributor.startsWith("jar:")){
			return true;
		}
		return false;
	}

}
