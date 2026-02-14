package org.com.maher.todo.exception;

import java.util.UUID;

public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(UUID id) {
        super("Todo item not found with id: " + id);
    }
}
