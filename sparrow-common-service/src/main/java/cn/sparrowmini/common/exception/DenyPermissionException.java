package cn.sparrowmini.common.exception;

public class DenyPermissionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DenyPermissionException(String message) {
		super(message);
	}
}
