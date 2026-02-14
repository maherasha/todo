package org.com.maher.todo.exception;

import java.util.UUID;

public class PastDueModificationException extends RuntimeException {

    public PastDueModificationException(UUID id) {
        super("Todo item is past due and cannot be modified: " + id);
    }
}
