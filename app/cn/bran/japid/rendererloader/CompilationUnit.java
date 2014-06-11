/**
 * 
 */
package cn.bran.japid.rendererloader;

import java.util.StringTokenizer;

import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

import cn.bran.japid.template.JapidRenderer;
import cn.bran.japid.util.JapidFlags;

/**
 * Something to compile
 */
final class CompilationUnit implements ICompilationUnit {

	final private String clazzName;
	final private String fileName;
	final private char[] typeName;
	final private char[][] packageName;

	CompilationUnit(String _pClazzName) {
		String pClazzName = _pClazzName;
		this.clazzName = pClazzName;
		if (pClazzName.contains("$")) {
			pClazzName = pClazzName.substring(0, pClazzName.indexOf("$"));
		}
		this.fileName = pClazzName.replace('.', '/') + ".java";
		int dot = pClazzName.lastIndexOf('.');
		if (dot > 0) {
			this.typeName = pClazzName.substring(dot + 1).toCharArray();
		} else {
			this.typeName = pClazzName.toCharArray();
		}
		StringTokenizer izer = new StringTokenizer(pClazzName, ".");
		this.packageName = new char[izer.countTokens() - 1][];
		for (int i = 0; i < this.packageName.length; i++) {
			this.packageName[i] = izer.nextToken().toCharArray();
		}
	}

	@Override
	public boolean ignoreOptionalProblems() {
		return false;
	}

	@Override
	public char[] getFileName() {
		return this.fileName.toCharArray();
	}

	@Override
	public char[] getContents() {
			try {
				RendererClass rendererClass = JapidRenderer.japidClasses.get(this.clazzName);
				return rendererClass.getJavaSourceCode().toCharArray();
			} catch (NullPointerException e) {
				JapidFlags.log("NPE was thrown with class name: " + this.clazzName);
				throw e;
			}
	}

	@Override
	public char[] getMainTypeName() {
		return this.typeName;
	}

	@Override
	public char[][] getPackageName() {
		return this.packageName;
	}
}