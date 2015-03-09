package org.birenheide.bf.ed.template;

public class VariableEvaluationException extends RuntimeException {

	private static final long serialVersionUID = 4176213381777065561L;

	public VariableEvaluationException() {
	}

	public VariableEvaluationException(String message) {
		super(message);
	}

	public VariableEvaluationException(Throwable cause) {
		super(cause);
	}

	public VariableEvaluationException(String message, Throwable cause) {
		super(message, cause);
	}

	public VariableEvaluationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
