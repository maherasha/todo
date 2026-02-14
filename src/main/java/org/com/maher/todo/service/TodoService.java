package org.com.maher.todo.service;

import lombok.RequiredArgsConstructor;
import org.com.maher.todo.api.model.CreateTodoRequest;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.api.model.UpdateTodoRequest;
import org.com.maher.todo.exception.PastDueModificationException;
import org.com.maher.todo.exception.TodoNotFoundException;
import org.com.maher.todo.mapper.TodoItemMapper;
import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.com.maher.todo.repository.TodoItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoItemRepository repository;
    private final TodoItemMapper mapper;

    @Transactional
    public TodoResponse createTodo(CreateTodoRequest request) {
        TodoItem item = mapper.toEntity(request);
        item.setStatus(TodoStatus.NOT_DONE);
        item.setCreationDatetime(Instant.now());
        TodoItem saved = repository.save(item);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TodoResponse getTodoById(UUID id) {
        TodoItem item = repository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        return mapper.toResponse(item);
    }

    @Transactional
    public TodoResponse updateTodoDescription(UUID id, UpdateTodoRequest request) {
        TodoItem item = repository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        if (item.getStatus() == TodoStatus.PAST_DUE) {
            throw new PastDueModificationException(id);
        }
        item.setDescription(request.getDescription());
        TodoItem saved = repository.save(item);
        return mapper.toResponse(saved);
    }

    @Transactional
    public TodoResponse markTodoDone(UUID id) {
        TodoItem item = repository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        if (item.getStatus() == TodoStatus.PAST_DUE) {
            throw new PastDueModificationException(id);
        }
        item.setStatus(TodoStatus.DONE);
        item.setDoneDatetime(Instant.now());
        TodoItem saved = repository.save(item);
        return mapper.toResponse(saved);
    }

    @Transactional
    public TodoResponse markTodoNotDone(UUID id) {
        TodoItem item = repository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        if (item.getStatus() == TodoStatus.PAST_DUE) {
            throw new PastDueModificationException(id);
        }
        item.setStatus(TodoStatus.NOT_DONE);
        item.setDoneDatetime(null);
        TodoItem saved = repository.save(item);
        return mapper.toResponse(saved);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int markOverdueAsPastDueBatch(int batchSize) {
        return repository.markOverdueAsPastDueBatch(Instant.now(), batchSize);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodos(String status) {
        List<TodoItem> items;
        if ("all".equalsIgnoreCase(status)) {
            items = repository.findAll();
        } else {
            items = repository.findByStatus(TodoStatus.NOT_DONE);
        }
        return items.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
