package io.tokra.audio;
 
public class PlayException extends Exception {
 
	private static final long serialVersionUID = 1L;

	public PlayException(String message) {
	super(message);
    }
 
    public PlayException(Throwable cause) {
	super(cause);
    }
 
    public PlayException(String message, Throwable cause) {
	super(message, cause);
    }
 
}