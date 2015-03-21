package com.vector.amazonaws;

public class UploadInterruptedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UploadInterruptedException() {
		super();
	}

	public UploadInterruptedException(String message) {
		super(message);
	}

	public UploadInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UploadInterruptedException(Throwable cause) {
		super(cause);
	}
}
