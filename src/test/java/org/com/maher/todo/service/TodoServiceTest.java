package org.com.maher.todo.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoItemRepository repository;

    @Mock
    private TodoItemMapper mapper;

    @InjectMocks
    private TodoService todoService;

    // --- createTodo ---

    @Test
    void createTodo_setsStatusToNotDoneAndCreationTime() {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setDescription("Buy groceries");
        request.setDueDatetime(LocalDateTime.of(2026, 3, 15, 10, 0));

        TodoItem mappedEntity = new TodoItem();
        TodoItem savedEntity = new TodoItem();
        savedEntity.setId(UUID.randomUUID());
        TodoResponse expectedResponse = new TodoResponse();

        when(mapper.toEntity(request)).thenReturn(mappedEntity);
        when(repository.save(mappedEntity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        TodoResponse result = todoService.createTodo(request);

        assertThat(result).isSameAs(expectedResponse);
        assertThat(mappedEntity.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(mappedEntity.getCreationDatetime()).isNotNull();
    }

    // --- getTodoById ---

    @Test
    void getTodoById_returnsResponseWhenFound() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        TodoResponse expectedResponse = new TodoResponse();

        when(repository.findById(id)).thenReturn(Optional.of(item));
        when(mapper.toResponse(item)).thenReturn(expectedResponse);

        assertThat(todoService.getTodoById(id)).isSameAs(expectedResponse);
    }

    @Test
    void getTodoById_throwsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.getTodoById(id))
                .isInstanceOf(TodoNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // --- updateTodoDescription ---

    @Test
    void updateDescription_updatesAndReturnsResponse() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setStatus(TodoStatus.NOT_DONE);

        UpdateTodoRequest request = new UpdateTodoRequest();
        request.setDescription("Updated description");

        TodoItem savedItem = new TodoItem();
        TodoResponse expectedResponse = new TodoResponse();

        when(repository.findById(id)).thenReturn(Optional.of(item));
        when(repository.save(item)).thenReturn(savedItem);
        when(mapper.toResponse(savedItem)).thenReturn(expectedResponse);

        TodoResponse result = todoService.updateTodoDescription(id, request);

        assertThat(result).isSameAs(expectedResponse);
        assertThat(item.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateDescription_throwsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.updateTodoDescription(id, new UpdateTodoRequest()))
                .isInstanceOf(TodoNotFoundException.class);
    }

    @Test
    void updateDescription_rejectsPastDueItem() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setStatus(TodoStatus.PAST_DUE);

        when(repository.findById(id)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> todoService.updateTodoDescription(id, new UpdateTodoRequest()))
                .isInstanceOf(PastDueModificationException.class)
                .hasMessageContaining(id.toString());

        verify(repository, never()).save(any());
    }

    // --- markTodoDone ---

    @Test
    void markDone_setsStatusAndDoneDatetime() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setStatus(TodoStatus.NOT_DONE);

        TodoItem savedItem = new TodoItem();
        TodoResponse expectedResponse = new TodoResponse();

        when(repository.findById(id)).thenReturn(Optional.of(item));
        when(repository.save(item)).thenReturn(savedItem);
        when(mapper.toResponse(savedItem)).thenReturn(expectedResponse);

        Instant before = Instant.now();
        TodoResponse result = todoService.markTodoDone(id);

        assertThat(result).isSameAs(expectedResponse);
        assertThat(item.getStatus()).isEqualTo(TodoStatus.DONE);
        assertThat(item.getDoneDatetime()).isAfterOrEqualTo(before);
    }

    @Test
    void markDone_rejectsPastDueItem() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setStatus(TodoStatus.PAST_DUE);

        when(repository.findById(id)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> todoService.markTodoDone(id))
                .isInstanceOf(PastDueModificationException.class);

        verify(repository, never()).save(any());
    }

    // --- markTodoNotDone ---

    @Test
    void markNotDone_setsStatusAndClearsDoneDatetime() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setStatus(TodoStatus.DONE);
        item.setDoneDatetime(Instant.now());

        TodoItem savedItem = new TodoItem();
        TodoResponse expectedResponse = new TodoResponse();

        when(repository.findById(id)).thenReturn(Optional.of(item));
        when(repository.save(item)).thenReturn(savedItem);
        when(mapper.toResponse(savedItem)).thenReturn(expectedResponse);

        TodoResponse result = todoService.markTodoNotDone(id);

        assertThat(result).isSameAs(expectedResponse);
        assertThat(item.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(item.getDoneDatetime()).isNull();
    }

    @Test
    void markNotDone_rejectsPastDueItem() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setStatus(TodoStatus.PAST_DUE);

        when(repository.findById(id)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> todoService.markTodoNotDone(id))
                .isInstanceOf(PastDueModificationException.class);

        verify(repository, never()).save(any());
    }

    // --- markOverdueAsPastDueBatch ---

    @Test
    void markOverdueBatch_delegatesToRepositoryAndReturnsCount() {
        when(repository.markOverdueAsPastDueBatch(any(Instant.class), eq(1000)))
                .thenReturn(5);

        int count = todoService.markOverdueAsPastDueBatch(1000);

        assertThat(count).isEqualTo(5);
        verify(repository).markOverdueAsPastDueBatch(any(Instant.class), eq(1000));
    }

    @Test
    void markOverdueBatch_returnsZeroWhenNoneOverdue() {
        when(repository.markOverdueAsPastDueBatch(any(Instant.class), eq(1000)))
                .thenReturn(0);

        int count = todoService.markOverdueAsPastDueBatch(1000);

        assertThat(count).isZero();
    }

    // --- getTodos (offset pagination) ---

    @Test
    void getTodos_defaultReturnsNotDoneItemsOnly() {
        TodoItem item = new TodoItem();
        TodoResponse response = new TodoResponse();

        when(repository.findByStatus(eq(TodoStatus.NOT_DONE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));
        when(mapper.toResponseList(List.of(item))).thenReturn(List.of(response));

        TodoPageResponse result = todoService.getTodos(null, 0, 100);

        assertThat(result.getItems()).containsExactly(response);
        assertThat(result.getTotalCount()).isEqualTo(1L);
        verify(repository).findByStatus(eq(TodoStatus.NOT_DONE), any(Pageable.class));
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getTodos_statusAllReturnsAllItems() {
        TodoItem item = new TodoItem();
        TodoResponse response = new TodoResponse();

        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item)));
        when(mapper.toResponseList(List.of(item))).thenReturn(List.of(response));

        TodoPageResponse result = todoService.getTodos("all", 0, 100);

        assertThat(result.getItems()).containsExactly(response);
        assertThat(result.getTotalCount()).isEqualTo(1L);
        verify(repository).findAll(any(Pageable.class));
        verify(repository, never()).findByStatus(any(), any(Pageable.class));
    }

    @Test
    void getTodos_respectsCustomSize() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        todoService.getTodos("all", 0, 10);

        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    void getTodos_respectsPage() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        todoService.getTodos("all", 2, 25);

        verify(repository).findAll(any(Pageable.class));
    }

    @Test
    void getTodos_emptyResultReturnsTotalCountZero() {
        when(repository.findByStatus(eq(TodoStatus.NOT_DONE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        TodoPageResponse result = todoService.getTodos(null, 0, 100);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalCount()).isZero();
    }
}