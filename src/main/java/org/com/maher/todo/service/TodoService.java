package org.com.maher.todo.service;

import lombok.RequiredArgsConstructor;
import org.com.maher.todo.api.model.CreateTodoRequest;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.mapper.TodoItemMapper;
import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.com.maher.todo.repository.TodoItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoItemRepository repository;
    private final TodoItemMapper mapper;

    public TodoResponse createTodo(CreateTodoRequest request) {
        TodoItem item = mapper.toEntity(request);
        item.setStatus(TodoStatus.NOT_DONE);
        item.setCreationDatetime(Instant.now());
        TodoItem saved = repository.save(item);
        return mapper.toResponse(saved);
    }
}
