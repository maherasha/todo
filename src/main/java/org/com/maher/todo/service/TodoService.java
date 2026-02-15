package org.com.maher.todo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.maher.todo.api.model.CreateTodoRequest;
import org.com.maher.todo.api.model.TodoPageResponse;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.api.model.UpdateTodoRequest;
import org.com.maher.todo.exception.PastDueModificationException;
import org.com.maher.todo.exception.TodoNotFoundException;
import org.com.maher.todo.mapper.TodoItemMapper;
import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.com.maher.todo.repository.TodoItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoItemRepository repository;
    private final TodoItemMapper mapper;

    @Transactional
    public TodoResponse createTodo(CreateTodoRequest request) {
        if (request.getDueDatetime() != null
                && !request.getDueDatetime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("dueDatetime must be in the future");
        }
        TodoItem item = mapper.toEntity(request);
        item.setStatus(TodoStatus.NOT_DONE);
        item.setCreationDatetime(Instant.now());
        TodoItem saved = repository.save(item);
        log.debug("Created todo id={}", saved.getId());
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
        log.info("Todo id={} marked as DONE", id);
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
        log.info("Todo id={} marked as NOT_DONE", id);
        return mapper.toResponse(saved);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int markOverdueAsPastDueBatch(int batchSize) {
        return repository.markOverdueAsPastDueBatch(Instant.now(), batchSize);
    }

    @Transactional(readOnly = true)
    public TodoPageResponse getTodos(String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<TodoItem> result = "all".equalsIgnoreCase(status)
                ? repository.findAll(pageable)
                : repository.findByStatus(TodoStatus.NOT_DONE, pageable);

        TodoPageResponse response = new TodoPageResponse();
        response.setItems(mapper.toResponseList(result.getContent()));
        response.setTotalCount(result.getTotalElements());
        return response;
    }
}
