package org.com.maher.todo.repository;

import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TodoItemRepository extends JpaRepository<TodoItem, UUID> {

    List<TodoItem> findByStatus(TodoStatus status);

    List<TodoItem> findByStatusAndDueDatetimeBefore(TodoStatus status, Instant before);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE todo_item SET status = 'PAST_DUE', version = version + 1 WHERE id IN (
                SELECT id FROM todo_item
                WHERE status = 'NOT_DONE' AND due_datetime < :now
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            """, nativeQuery = true)
    int markOverdueAsPastDueBatch(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize);
}
