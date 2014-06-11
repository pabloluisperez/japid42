/**
 * 
 */
package cn.bran.japid.template;

/**
 * @author bran
 *
 */
public class JapidTemplateNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -9156139486804972090L;
	private String templateName;
	private String searchingPath;
	public JapidTemplateNotFoundException(String _templateName, String _searchingPath) {
		super("Japid template not found: " + _templateName + ". Searching path was: " + _searchingPath);
		this.templateName = _templateName;
		this.searchingPath = _searchingPath;
	}
	/**
	 * @return the templateName
	 */
	public String getTemplateName() {
		return this.templateName;
	}
	/**
	 * @param _templateName the templateName to set
	 */
	public void setTemplateName(String _templateName) {
		this.templateName = _templateName;
	}
	/**
	 * @return the searchingPath
	 */
	public String getSearchingPath() {
		return this.searchingPath;
	}
	/**
	 * @param _searchingPath the searchingPath to set
	 */
	public void setSearchingPath(String _searchingPath) {
		this.searchingPath = _searchingPath;
	}
	
}
