package org.com.maher.todo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TodoStatus {

    NOT_DONE("not done"),
    DONE("done"),
    PAST_DUE("past due");

    private final String value;

    TodoStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TodoStatus fromValue(String value) {
        for (TodoStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
