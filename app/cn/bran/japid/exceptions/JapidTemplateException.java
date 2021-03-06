/**
 * 
 */
package cn.bran.japid.exceptions;

import java.util.TreeMap;

import cn.bran.japid.compiler.JapidCompilationException;

/**
 * @author bran
 *
 */
public class JapidTemplateException extends JapidRuntimeException {
	private static final long serialVersionUID = 1L;
	/**
	 * @param string
	 */
	public JapidTemplateException(String _title, String _description, int _errLineNum, String sourceName, String sourceCode) {
		super(_title, _description);
		this.errLineNum = _errLineNum;
		this.soureName = sourceName;
		this.srcCode = sourceCode;
		this.interestingLines = getInterestingLines();
	}
	
	/**
	 * @param e2
	 */
	public JapidTemplateException(Throwable _e) {
		super(_e.getClass().getName(), _e.getMessage());
	}

	public Throwable e;
	public Integer errLineNum; // 1-based
	public String soureName;
	public String srcCode;
	public TreeMap<Integer, String> interestingLines = new TreeMap<Integer, String>();
	public Integer errLinePosInInterestingLines;
	
	private TreeMap<Integer, String> getInterestingLines() {
		TreeMap<Integer, String> re = new TreeMap<Integer, String>();
		if (this.srcCode != null && this.srcCode.length() > 0) {
			String[] lines = this.srcCode.split("\n");
			int size = lines.length;
			int start = this.errLineNum - 4;
			start = start > 0 ? start : 1;
			this.errLinePosInInterestingLines = this.errLineNum - start;
			int end = this.errLineNum + 4;
			end = end < size ? end : size;
			for (int i = start; i <= end; i++) {
				re.put(i, lines[i - 1]);
			}
		}
		return re;
	}

	/**
	 * @author Bing Ran (bing.ran@hotmail.com)
	 * @param jce
	 * @return
	 */
	public static JapidTemplateException from(JapidCompilationException jce) {
		return new JapidTemplateException(
				"Error: " + jce.getMessage(),
				jce.getLocation(),
				jce.getLineNumber(),
				jce.getTemplateName(),
				jce.getTemplateSrc());
	}
}
