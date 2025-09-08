package org.unsynchronized;

import java.io.IOException;
import java.io.Serial;

/**
 * Exception that denotes that data in the stream did not conform to the constraints
 * imposed by the specification.
 */
public class ValidityException extends IOException {
    @Serial
    private static final long serialVersionUID = 2277356908919241L;

    public ValidityException(String msg) {
        super(msg);
    }
}

