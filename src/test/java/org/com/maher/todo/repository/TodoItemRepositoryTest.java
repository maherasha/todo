package org.com.maher.todo.repository;

import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TodoItemRepositoryTest {

    @Autowired
    private TodoItemRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.saveAll(List.of(
                createItem("Active task", TodoStatus.NOT_DONE, Instant.now().plus(7, ChronoUnit.DAYS)),
                createItem("Completed task", TodoStatus.DONE, Instant.now().plus(7, ChronoUnit.DAYS)),
                createItem("Overdue task", TodoStatus.PAST_DUE, Instant.now().minus(1, ChronoUnit.DAYS))
        ));
    }

    @Test
    void findByStatusAndDueDatetimeBefore_filtersCorrectly() {
        List<TodoItem> result = repository.findByStatusAndDueDatetimeBefore(
                TodoStatus.NOT_DONE, Instant.now().plus(30, ChronoUnit.DAYS));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Active task");
    }

    @Test
    void findByStatusAndDueDatetimeBefore_returnsEmptyWhenNoMatch() {
        List<TodoItem> result = repository.findByStatusAndDueDatetimeBefore(
                TodoStatus.NOT_DONE, Instant.now().minus(30, ChronoUnit.DAYS));

        assertThat(result).isEmpty();
    }

    private TodoItem createItem(String description, TodoStatus status, Instant dueDate) {
        TodoItem item = new TodoItem();
        item.setDescription(description);
        item.setStatus(status);
        item.setCreationDatetime(Instant.now());
        item.setDueDatetime(dueDate);
        return item;
    }
}