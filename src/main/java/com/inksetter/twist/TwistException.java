package com.inksetter.twist;

/**
 * The base exception class for all Twist exceptions.  Applications should
 * extend this exception to allow for application-specific exceptions.
 *
 * @author  Derek Inksetter
 */
public class TwistException extends Exception {

    /**
     * @param message
     */
    public TwistException(String message) {
        super(message);
    }
    
    /**
     * Wraps another exception in a Twist exception.
     * @param message
     * @param t
     */
    public TwistException(String message, Throwable t) {
        super(message, t);
    }

    private static final long serialVersionUID = 1L;
}
