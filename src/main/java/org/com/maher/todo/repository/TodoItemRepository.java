package org.com.maher.todo.repository;

import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TodoItemRepository extends JpaRepository<TodoItem, UUID> {

    List<TodoItem> findByStatus(TodoStatus status);

    List<TodoItem> findByStatusAndDueDatetimeBefore(TodoStatus status, Instant before);
}
