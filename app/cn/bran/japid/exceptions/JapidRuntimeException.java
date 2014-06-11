package cn.bran.japid.exceptions;

public class JapidRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public String title;
	public String description;

	/**
	 * @param title2
	 * @param description2
	 */
	public JapidRuntimeException(String _title, String descr) {
		super(descr);
		this.title = _title;
		this.description = descr;
	}

}
