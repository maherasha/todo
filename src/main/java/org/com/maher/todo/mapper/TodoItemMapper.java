package org.com.maher.todo.mapper;

import org.com.maher.todo.api.model.CreateTodoRequest;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper
public interface TodoItemMapper {

    TodoResponse toResponse(TodoItem entity);

    List<TodoResponse> toResponseList(List<TodoItem> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDatetime", ignore = true)
    @Mapping(target = "doneDatetime", ignore = true)
    TodoItem toEntity(CreateTodoRequest request);

    default TodoResponse.StatusEnum mapStatus(TodoStatus status) {
        return status != null ? TodoResponse.StatusEnum.fromValue(status.name()) : null;
    }

    default LocalDateTime mapInstantToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    default Instant mapLocalDateTimeToInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toInstant(ZoneOffset.UTC) : null;
    }
}
