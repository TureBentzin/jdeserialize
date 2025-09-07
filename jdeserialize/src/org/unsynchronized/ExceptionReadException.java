package org.unsynchronized;

import java.io.IOException;
import java.io.Serial;

/**
 * Exception used to signal that an exception object was successfully read from the 
 * stream.  This object holds a reference to the serialized exception object.
 */
public class ExceptionReadException extends IOException {
    @Serial
    public static final long serialVersionUID = 2277356908919221L;
    public IContent exception;

    /**
     * Constructor.
     * @param content the serialized exception object that was read
     */
    public ExceptionReadException(IContent content) {
        super("serialized exception read during stream");
        this.exception = content;
    }

    /**
     * Gets the Exception object that was thrown.
     * @return the content representing the serialized exception object
     */
    public IContent getExceptionObject() {
        return exception;
    }
}

