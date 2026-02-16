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

    @Test
    void markOverdueAsPastDueBatch_updatesOverdueNotDoneItems() {
        repository.deleteAll();
        repository.saveAll(List.of(
                createItem("Overdue 1", TodoStatus.NOT_DONE, Instant.now().minus(2, ChronoUnit.DAYS)),
                createItem("Overdue 2", TodoStatus.NOT_DONE, Instant.now().minus(1, ChronoUnit.DAYS)),
                createItem("Future task", TodoStatus.NOT_DONE, Instant.now().plus(7, ChronoUnit.DAYS)),
                createItem("Already done", TodoStatus.DONE, Instant.now().minus(1, ChronoUnit.DAYS)),
                createItem("Already past due", TodoStatus.PAST_DUE, Instant.now().minus(1, ChronoUnit.DAYS))
        ));

        int updated = repository.markOverdueAsPastDueBatch(Instant.now(), 100);

        assertThat(updated).isEqualTo(2);
        List<TodoItem> allItems = repository.findAll();
        assertThat(allItems)
                .filteredOn(i -> i.getStatus() == TodoStatus.PAST_DUE)
                .hasSize(3); // 2 newly marked + 1 already past due
        assertThat(allItems)
                .filteredOn(i -> i.getDescription().equals("Future task"))
                .allMatch(i -> i.getStatus() == TodoStatus.NOT_DONE);
    }

    @Test
    void markOverdueAsPastDueBatch_respectsBatchSize() {
        repository.deleteAll();
        repository.saveAll(List.of(
                createItem("Overdue 1", TodoStatus.NOT_DONE, Instant.now().minus(3, ChronoUnit.DAYS)),
                createItem("Overdue 2", TodoStatus.NOT_DONE, Instant.now().minus(2, ChronoUnit.DAYS)),
                createItem("Overdue 3", TodoStatus.NOT_DONE, Instant.now().minus(1, ChronoUnit.DAYS))
        ));

        int updated = repository.markOverdueAsPastDueBatch(Instant.now(), 2);

        assertThat(updated).isEqualTo(2);
    }

    @Test
    void markOverdueAsPastDueBatch_returnsZeroWhenNothingToUpdate() {
        repository.deleteAll();
        repository.save(createItem("Future task", TodoStatus.NOT_DONE, Instant.now().plus(7, ChronoUnit.DAYS)));

        int updated = repository.markOverdueAsPastDueBatch(Instant.now(), 100);

        assertThat(updated).isZero();
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