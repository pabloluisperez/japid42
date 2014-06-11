/**
 * Copyright 2010 Bing Ran<bing_ran@hotmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.bran.japid.classmeta;

import japa.parser.ast.body.Parameter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import cn.bran.japid.compiler.Tag;
import cn.bran.japid.compiler.Tag.TagDef;
import cn.bran.japid.template.ActionRunner;
import cn.bran.japid.template.JapidTemplateBaseStreaming;
import cn.bran.japid.template.JapidTemplateBaseWithoutPlay;
import cn.bran.japid.template.RenderResult;
import cn.bran.japid.template.RenderResultPartial;
import cn.bran.japid.util.DirUtil;

/**
 * lots of the code block generation is done here
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public abstract class AbstractTemplateClassMetaData {
	/**
	 * 
	 */
	public static final String VERSION_HEADER = "//version: ";
	private static final String PUBLIC = "public ";
	private static final String COMMA = ";";
	private static final String SPACE = " ";
	private static final String STATIC = "static";
	private static final String IMPORT = "import";
	private static Set<String> globalStaticImports = new HashSet<String>();
	private Set<String> staticImports = new HashSet<String>();
	private String originalTemplate;
	private static Pattern partialImport = Pattern.compile("import\\s+\\.(.+)");

	// control if we use a streaming based API or StringBuilder based API
	public static boolean streaming = false;
	// if we need to track the time to render
	boolean stopWatch = false;
	// control whether to allow safe expression navigation
	public boolean suppressNull = false;

	public boolean useWithPlay = true;

	public String getOriginalTemplate() {
		return this.originalTemplate;
	}

	public void setOriginalTemplate(String _originalTemplate) {
		this.originalTemplate = _originalTemplate.replace('\\', '/');
	}

	public StringBuilder sb = new StringBuilder();
	protected static final String SEMI = COMMA;
	protected static final String TAB = "\t";
	protected static final String RENDER_RESULT = RenderResult.class.getName();
	protected static final String RENDER_RESULT_PARTIAL = RenderResultPartial.class.getName();
	public static final String ACTION_RUNNERS = "actionRunners";
	private static final String IMPORT_SPACE = IMPORT + SPACE;
	private static final String CONTENT_TYPE = "Content-Type";
	/**
	 * 
	 */
	public static final String END_DO_LAYOUT = "endDoLayout";
	/**
	 * 
	 */
	public static final String BEGIN_DO_LAYOUT = "beginDoLayout";
	public String packageName;
	protected String className;

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String _className) {
		this.className = _className;
	}

	// each line: byte[] _lineXXX=new byte[]{12, 23, 45};
	List<String> statics = new ArrayList<String>();
	// the source of the plain text line
	List<String> staticsSrc = new ArrayList<String>();
	int staticCounter = 0;

	// List<String>importsLines = new ArrayList<String>();

	/**
	 * the main body part
	 */
	public String body;
	protected List<InnerClassMeta> innersforTagCalls = new ArrayList<InnerClassMeta>();
	protected List<InnerClassMeta> innersInvokeCalls = new ArrayList<InnerClassMeta>();
	protected boolean isAbstract;
	public static String curVersion;

	public AbstractTemplateClassMetaData() {
		super();
	}

	protected void pln(Object... ss) {
		for (Object o : ss) {
			this.sb.append(o);
		}
		this.sb.append("\n");
	}

	void p(String s) {
		this.sb.append(s);
	}

	public InnerClassMeta addCallTagBodyInnerClass(String _className, int count, String callbackArgs, String _body) {
		if (specialTags.contains(_className))
			return null;
		InnerClassMeta inner = new InnerClassMeta(_className, count, callbackArgs, _body);
		this.innersforTagCalls.add(inner);
		return inner;
	}

	public void removeLastCallTagBodyInnerClass() {
		this.innersforTagCalls.remove(this.innersforTagCalls.size() - 1);
	}
	
	private static Set<String> specialTags = new HashSet<String>();
	static {
		specialTags.add("set");
		specialTags.add("get");
		specialTags.add("invoke");
		specialTags.add("doBody");
		specialTags.add("doLayout");
		specialTags.add("extends");
	}

	/**
	 * 
	 */
	public void printHeaders() {
		printVersion();
		if (this.packageName != null) {
			pln("package " + this.packageName + SEMI);
		}
		pln("import java.util.*;");
		pln("import java.io.*;");
		// some nameing convention suport
		// cannot
		// pln("import japidviews._tags.*;");
		// pln("import japidviews._layouts.*;");

		if (streaming)
			pln(IMPORT_SPACE + cn.bran.japid.tags.streaming.Each.class.getName() + COMMA);
		else
			pln(IMPORT_SPACE + cn.bran.japid.tags.Each.class.getName() + COMMA);

		if (this.hasActionInvocation && this.useWithPlay) {
			pln(IMPORT_SPACE + ActionRunner.class.getName() + COMMA);
		}

		for (String l : this.imports) {
			l = l.trim();
			if (!l.endsWith(COMMA))
				l = l + COMMA;
			if (!l.startsWith(IMPORT))
				l = IMPORT_SPACE + l;
			// extension: allow partial imports. e.g.: import .sub.*
			// which requires the current package name to prefix it.
			l = expandPartialImport(l);
			if (considerPlayDependency(l))
				pln(l);
		}

		for (String l : globalStaticImports) {
			l = l.trim();
			if (!l.startsWith(IMPORT))
				l = IMPORT_SPACE + STATIC + SPACE + l;

			if (!l.endsWith(".*;")) {
				l += ".*;";
			}
			l = expandPartialImport(l);
			if (considerPlayDependency(l))
				pln(l);
		}

		for (String l : this.staticImports) {
			l = l.trim();
			if (!l.startsWith(IMPORT))
				l = IMPORT_SPACE + STATIC + SPACE + l;

			if (!l.endsWith(".*;")) {
				l += ".*;";
			}
			l = expandPartialImport(l);
			if (considerPlayDependency(l))
				pln(l);
		}

		// pln("import java.math.*;");
		// pln("import static java.lang.Math.*;");
		// // should decouple with JavaExtensions
		// pln("import static play.templates.JavaExtensions.*;");
		for (String l : globalImports) {
			l = l.trim();
			if (!l.endsWith(COMMA))
				l = l + COMMA;
			if (!l.startsWith(IMPORT))
				l = IMPORT_SPACE + l;
			l = expandPartialImport(l);
			if (considerPlayDependency(l))
				pln(l);
		}

		pln("//");
		pln("// NOTE: This file was generated from: " + this.originalTemplate);
		pln("// Change to this file will be lost next time the template file is compiled.");
		pln("//");

	}

	/**
	 * @author Bing Ran (bing.ran@gmail.com)
	 */
	private void printVersion() {
		String v = VERSION_HEADER + curVersion;
//		JapidFlags.debug("wrote version tag to Java derives: " + v );
		pln (v);
	}

	private boolean considerPlayDependency(String la) {
		String l = la;
		if (this.useWithPlay)
			return true;

		// filter out all Play related imports
		if (l.startsWith(IMPORT)) {
			l = l.substring(IMPORT.length()).trim();
		}

		if (l.startsWith(STATIC)) {
			l = l.substring(STATIC.length()).trim();
		}

		if (l.startsWith("play"))
			return false;

		if (l.startsWith("cn.bran.play"))
			return false;

//		if (l.startsWith("japidviews"))
//			return false;

//		if (l.startsWith("models"))
//			return false;

//		if (l.contains("JapidWebUtil"))
//			return false;

//		if (l.startsWith("controllers"))
//			return false;

		return true;
	}

	/**
	 * @param l
	 *            a partially specified import line such as: import .tags.*;
	 * @return
	 */
	public String expandPartialImport(String la) {
		String l = la;
		Matcher matcher = partialImport.matcher(l);
		if (matcher.matches()) {
			l = "import " + this.packageName + "." + matcher.group(1);
		}
		return l;
	}

	protected void embedSourceTemplateName() {
		pln("\t" + "public static final String sourceTemplate = \"" + this.originalTemplate + "\";");
	}

	// protected void embedContentType() {
	// String t = contentType == null ? "text/html" : contentType;
	// pln("\t" + "public static final String contentType = \"" + t + "\";");
	// }

	/**
	 * 
	 */
	// protected void callTags() {
	// // inners
	// for (InnerClassMeta inner : this.innersforTagCalls) {
	// // create a reusable instance _tagName_indexand a instance
	// // initializer
	// String tagClassName = inner.tagName;
	// String field = "private " + tagClassName + " _" +
	// inner.getInnerClassName() + inner.counter + " = new " + tagClassName +
	// "(getOut());";
	// pln("\t" + field);
	//
	// if (inner.renderBody != null) {
	// // body class
	// pln(inner.toString());
	// }
	// }
	// }

//	/**
//	 * @deprecated declare it as fields instead
//	 */
//	protected void setupTagObjectsAsVariables() {
//		boolean hasTags = this.innersforTagCalls.size() > 0;
//		if (hasTags)
//			pln("\n// -- set up the tag objects");
//		for (InnerClassMeta inner : this.innersforTagCalls) {
//			// create a reusable instance _tagName_indexand a instance
//			// initializer
//			String tagClassName = inner.tagName;
//			String var = "_" + inner.getVarRoot() + inner.counter;
//			String decl = "final " + tagClassName + " " + var + " = new " + tagClassName + "(getOut());";
//			pln(decl);
//			if (useWithPlay) {
//				String addRunner = var + ".setActionRunners(getActionRunners());";
//				pln(addRunner);
//			}
//			pln();
//		}
//		if (hasTags)
//			pln("// -- end of the tag objects\n");
//	}

	// can be used to create local variables too!
	/**
	 * commented out
	 * 
	 * 
		// don't declare in the front. always declare where it is used for safety
		// see JapidAbstractCompiler#regularTagInvoke
	 */

	protected void printAnnotations() {
		for (Class<? extends Annotation> anno : typeAnnotations) {
			pln("@" + anno.getName());
		}
	}

	/**
	 * add import lines to the to be generated imports lines, import and the
	 * ending ; are optional
	 * 
	 * @param imp
	 */
	public static void addImportLineGlobal(String _imp) {
		String imp = _imp;
		imp = imp.trim();
		if (imp.startsWith(IMPORT)) {
			imp = imp.substring(IMPORT.length()).trim();
		}

		globalImports.add(imp);
	}

	protected void buildStatics() {
		for (int i = 0; i < this.statics.size(); i++) {
			if (streaming)
				pln("static private final byte[] static_" + i + " = getBytes(" + this.statics.get(i) + ");");
			else
				pln("static private final String static_" + i + " = " + this.statics.get(i) + COMMA);
		}
	}

	protected void addConstructors() {
		if (!streaming) {
			// for StringBuilder data collection, create a default constructor
			pln(TAB + PUBLIC + this.className + "() {");
			pln(TAB + "super((StringBuilder)null);");
			if (this.useWithPlay)
				pln(TAB + "initHeaders();");
			pln(TAB +  "}");
		}

		if (streaming)
			pln(TAB + PUBLIC + this.className + "(OutputStream out) {");
		else
			pln(TAB + PUBLIC + this.className + "(StringBuilder out) {");

		pln(TAB + TAB + "super(out);");
		if (this.useWithPlay)
			pln(TAB + TAB + "initHeaders();");
		pln(TAB + "}");
		
		pln(TAB + PUBLIC + this.className + "(" + JapidTemplateBaseWithoutPlay.class.getName() + " caller) {\n" + 
				"		super(caller);\n" + 
				"	}\n" + 
				"");
	}

	/**
	 * 
	 */
	private void classDeclare() {
		if (this.superClass == null) {
			if (this.useWithPlay) {
//				superClass = JapidTemplateBase.class.getName();
				this.superClass = "cn.bran.play.JapidTemplateBase";
				
				if (streaming)
					this.superClass = JapidTemplateBaseStreaming.class.getName();
			}
			else {
				this.superClass = JapidTemplateBaseWithoutPlay.class.getName();
			}
		}

		String abs = this.isAbstract ? "abstract " : "";

		pln("public " + abs + "class " + this.className + " extends " + this.superClass);

	}

	/**
	 * set the generated class to be abstract
	 * 
	 * @param isAbstract
	 */
	public void setAbstract(boolean _isAbstract) {
		this.isAbstract = _isAbstract;
	}

	public String superClass;

	public static void addImportStatic(Class<?> class1) {
		String className = class1.getName();
		globalStaticImports.add(className);
	}

	/**
	 * this is for globally adding static imports, usually by tools.
	 * 
	 * @param imp
	 */
	public static void addImportStaticGlobal(String _imp) {
		String imp = _imp;
		if (imp.startsWith(IMPORT))
			imp = imp.substring(IMPORT.length()).trim();

		if (imp.startsWith(STATIC))
			imp = imp.substring(IMPORT.length()).trim();

		globalStaticImports.add(imp);
	}

	public void addImport(Class<?> class1) {
		String _className = class1.getName();
		addImportLine(_className);
	}

	private static Set<String> globalImports = new HashSet<String>();
	private Set<String> imports = new HashSet<String>();
	private String contentType = "";

	/**
	 * 
	 * @param text
	 *            something like \"hello\"
	 * @param src
	 * @return
	 */
	public String addStaticText(String text, String src) {
		if (text != null && !text.isEmpty()) {
			if (this.trimStaticContent) {
				if (text.trim().length() == 0) {
					return null;
				}
			}
			this.statics.add(text);
			this.staticsSrc.add(src);
			return "static_" + (this.statics.size() - 1);
		} else
			return null;
	}

	/**
	 * add class level annotation
	 * 
	 * @param anno
	 */
	public static void addAnnotation(Class<? extends Annotation> anno) {
		typeAnnotations.add(anno);
	}

	static Set<Class<? extends Annotation>> typeAnnotations = new HashSet<Class<? extends Annotation>>();

	public void setContentType(String _contentType) {
		if (_contentType != null) {
			this.headers.put(CONTENT_TYPE, _contentType);
			this.contentType = _contentType;
		}
	}

	// String contentType;
	private boolean trimStaticContent = false;
	protected boolean hasActionInvocation;
	private Map<String, String> headers = new HashMap<String, String>();
	private List<TagDef> defTags = new ArrayList<TagDef>();
	public String renderArgs;
	// to support extends layout (arg1, arg2)
	public String superClassRenderArgs = "";
	protected int argsLineNum;
	private Boolean traceFile = null;

	public void turnOnStopwatch() {
		this.stopWatch = true;
	}

	/**
	 * suppress all NPE in expression ${} and display empty string
	 */
	public void suppressNull() {
		this.suppressNull = true;
	}

	public void addStaticImports(String im) {
		this.staticImports.add(im);
	}

	public void addImportLine(String line) {
		this.imports.add(line);
	}

	/**
	 * ignore static content that contains whitespace chars only, including
	 * space, tab, \n etc.
	 */
	public void trimStaticContent() {
		this.trimStaticContent = true;
	}

	public boolean getTrimStaticContent() {
		return this.trimStaticContent;
	}

	public void setHasActionInvocation() {
		this.hasActionInvocation = true;

	}

	public void setHeader(String name, String value) {
		this.headers.put(name, value);
	}

	public void printInitializer() {
		// now we use the headers var the template base, for slightly
		// performance penalty
		// pln("	private static final Map<String, String> headers = new HashMap<String, String>();");
		if (this.useWithPlay && this.headers.size() > 0) {
			// pln("	static {");
			pln("\t private void initHeaders() {");
			for (String k : this.headers.keySet()) {
				String v = this.headers.get(k);
				pln("\t\tputHeader(\"" + k + "\", \"" + v + "\");");
			}
			pln("\t\tsetContentType(\"" + this.contentType + "\");");
			pln("\t}");
		}
		pln("\t{");
			if (this.traceFile != null)
				if (this.traceFile)
					pln("\t\tsetTraceFile(true);");
				else
					pln("\t\tsetTraceFile(false);");
					
			pln("\t}");
		}

	public void addDefTag(TagDef tag) {
		this.defTags.add(tag);
	}

	protected void processDefTags() {
		for (TagDef tag : this.defTags) {
			String meth = tag.args.trim();
			if (meth.endsWith(")")) {
				pln("public String " + meth + " {");
			} else {
				pln("public String " + meth + "() {");
			}
			pln("StringBuilder sb = new StringBuilder();");
			pln("StringBuilder ori = getOut();");
			pln("this.setOut(sb);");
			if (this.useWithPlay)
				pln("TreeMap<Integer, cn.bran.japid.template.ActionRunner> parentActionRunners = actionRunners;\n" + 
						"actionRunners = new TreeMap<Integer, cn.bran.japid.template.ActionRunner>();" );
			
			pln(tag.getBodyText());
			pln("this.setOut(ori);");
			if (this.useWithPlay)
				pln("if (actionRunners.size() > 0) {\n" + 
						"	StringBuilder _sb2 = new StringBuilder();\n" + 
						"	int segStart = 0;\n" + 
						"	for (Map.Entry<Integer, cn.bran.japid.template.ActionRunner> _arEntry : actionRunners.entrySet()) {\n" + 
						"		int pos = _arEntry.getKey();\n" + 
						"		_sb2.append(sb.substring(segStart, pos));\n" + 
						"		segStart = pos;\n" + 
						"		cn.bran.japid.template.ActionRunner _a_ = _arEntry.getValue();\n" + 
						"		_sb2.append(_a_.run().getContent().toString());\n" + 
						"	}\n" + 
						"	_sb2.append(sb.substring(segStart));\n" + 
						"	actionRunners = parentActionRunners;\n" + 
						"	return _sb2.toString();\n" + 
						"} else {\n" + 
						"	actionRunners = parentActionRunners;\n" + 
						"	return sb.toString();\n" + 
						"}");
			else 
				pln("	return sb.toString();" );
			pln("}");
		}
	}

//	/**
//	 * @param t
//	 */
//	protected void declareTagInstance(Tag t) {
//		String tagClassName = t.tagName;
//		String var = t.getTagVarName();
//		
//		
//		if (tagClassName.equals("this")) {
//			tagClassName = this.getClassName();
//		}
//		
//		String decl = "final " + tagClassName + " " + var + " = new " + tagClassName + "(getOut());";
//		pln(decl);
//
//		// commented out. now runners are set just before use;
////		if (useWithPlay  && !tagClassName.equals("Each")) {
////			String addRunner = "{ " +  var + ".setActionRunners(getActionRunners()); }";
////			pln(addRunner);
////		}
//		pln();
//	}

	/**
	 * added field declarations such as request, response, errors
	 * Some of the implicit objects are defined in the JapidPlayAdapter
	 * Note: this basically removes the possibility to reuse the same instance of renderer to render multiple requests. 
	 */
	protected void addImplicitFields() {
		if (this.useWithPlay) {
			pln("\n// - add implicit fields with Play\n" +
					"boolean hasHttpContext = play.mvc.Http.Context.current.get() != null ? true : false;\n");
			pln("	final Request request = hasHttpContext? Implicit.request() : null;\n" + 
					"	final Response response = hasHttpContext ? Implicit.response() : null;\n" + 
					"	final Session session = hasHttpContext ? Implicit.session() : null;\n" + 
					"	final Flash flash = hasHttpContext ? Implicit.flash() : null;\n" + 
//					"	final Lang lang = hasHttpContext ? Implicit.lang() : null;\n" + // a performance hit. replace by a lang() call
					"	final play.Play _play = new play.Play(); \n" + 
					"");
			pln("// - end of implicit fields with Play \n\n");
		}
	}
	
	/**
	 * remove the plain text line between two consecutive script line, if the
	 * plain text is made of space chars only .
	 * 
	 * return true if that's the case
	 */
	public String removeLastSingleEmptyLine() {
		int last = this.staticsSrc.size() - 1;
		String s = this.staticsSrc.get(last);
		char[] charArray = s.toCharArray();
		for (char c : charArray) {
			if (!Character.isWhitespace(c))
				return null;
		}

		if (s.contains("\n")) {
			if (s.indexOf('\n') == s.lastIndexOf('\n')) {
				// it contains only one newline
				this.statics.remove(last);
				this.staticsSrc.remove(last);
				return s;
			} else
				return null;
		} else {
			this.statics.remove(last);
			this.staticsSrc.remove(last);
			return s;
		}

	}

	/**
	 * output the java code
	 * 
	 * @return
	 */
	public String generateCode() {
		printHeaders();
		printAnnotations();
		classDeclare();
		pln("{");
		embedSourceTemplateName();
		printInitializer();
		// buildStatics();
		if (this.useWithPlay) {
		addImplicitFields();
		}
		// now tags are local variables in dolayout for better safety
//		setupTagObjectsAsFields();
		
		addConstructors();

		renderMethod();

		layoutMethod();

		getterSetter();

		childLayout();

		processDefTags();

		p("}");

		return this.sb.toString();

	}

	abstract void renderMethod();

	abstract void layoutMethod();

	abstract void getterSetter();

	abstract void childLayout();

	@Override
	public String toString() {
		return generateCode();
	}

	/**
	 * @param p
	 */
	protected void addField(Parameter p) {
		// no need 
		String defaultVal = "=" /*+ JavaSyntaxTool.getDefault(p)*/;
		pln(TAB + "private " + p.getType() + " " + p.getId() + (defaultVal.equals("=") ? "":defaultVal) + "; " +getLineMarker());
	}

	protected String getLineMarker() {
//		return JapidAbstractCompiler.makeLineMarker(argsLineNum);
		return DirUtil.LINE_MARKER + this.argsLineNum + DirUtil.OF + this.originalTemplate;
	}

	public static void clearImports() {
		globalImports.clear();
		globalStaticImports.clear();
	}

	public void setArgsLineNum(int startLine) {
		this.argsLineNum = startLine;
	}

	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 */
	public void turnOnTraceFile() {
		this.traceFile = true;
	}

	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 */
	public void turnOffTraceFile() {
		this.traceFile = false;
	}

	/**
	 * @author Bing Ran (bing.ran@gmail.com)
	 * @param templateClassMetaData
	 */
	public void merge(AbstractTemplateClassMetaData a) {
		this.imports.addAll(a.imports);
		this.innersforTagCalls.addAll(a.innersforTagCalls);
		this.innersInvokeCalls.addAll(a.innersInvokeCalls);
	}

	protected void restOfBody() {
		pln(TAB + TAB + BEGIN_DO_LAYOUT + "(sourceTemplate);");
		pln(this.body);
		pln(TAB + TAB + END_DO_LAYOUT + "(sourceTemplate);");
		pln("\t}");
	}

}