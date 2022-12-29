package org.example.lib.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectAlreadyExistException extends RuntimeException {
    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @since 1.4
     */
    public ObjectAlreadyExistException(String message) {
        super(message);
    }
}

