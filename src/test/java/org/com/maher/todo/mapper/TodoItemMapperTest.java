package org.com.maher.todo.mapper;

import org.com.maher.todo.api.model.CreateTodoRequest;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.api.model.TodoResponse.StatusEnum;
import org.com.maher.todo.model.TodoItem;
import org.com.maher.todo.model.TodoStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TodoItemMapperTest {

    private final TodoItemMapper mapper = Mappers.getMapper(TodoItemMapper.class);

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        TodoItem item = new TodoItem();
        item.setId(UUID.randomUUID());
        item.setDescription("Test task");
        item.setStatus(TodoStatus.NOT_DONE);
        item.setCreationDatetime(Instant.parse("2026-02-14T10:00:00Z"));
        item.setDueDatetime(Instant.parse("2026-03-15T10:00:00Z"));
        item.setDoneDatetime(null);

        TodoResponse response = mapper.toResponse(item);

        assertThat(response.getId()).isEqualTo(item.getId());
        assertThat(response.getDescription()).isEqualTo("Test task");
        assertThat(response.getStatus()).isEqualTo(TodoResponse.StatusEnum.NOT_DONE);
        assertThat(response.getCreationDatetime()).isEqualTo(
                OffsetDateTime.of(2026, 2, 14, 10, 0, 0, 0, ZoneOffset.UTC));
        assertThat(response.getDueDatetime()).isEqualTo(
                OffsetDateTime.of(2026, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC));
        assertThat(response.getDoneDatetime()).isNull();
    }

    @Test
    void toEntity_mapsRequestFieldsAndIgnoresOthers() {
        CreateTodoRequest request = new CreateTodoRequest();
        request.setDescription("New task");
        request.setDueDatetime(OffsetDateTime.of(2026, 3, 15, 10, 0, 0, 0, ZoneOffset.UTC));

        TodoItem entity = mapper.toEntity(request);

        assertThat(entity.getDescription()).isEqualTo("New task");
        assertThat(entity.getDueDatetime()).isEqualTo(
                Instant.parse("2026-03-15T10:00:00Z"));
        assertThat(entity.getId()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getCreationDatetime()).isNull();
        assertThat(entity.getDoneDatetime()).isNull();
    }

    @Test
    void mapStatus_handlesNullGracefully() {
        assertThat(mapper.mapStatus(null)).isNull();
    }

    @Test
    void mapInstantToOffsetDateTime_handlesNullGracefully() {
        assertThat(mapper.mapInstantToOffsetDateTime(null)).isNull();
    }

    @Test
    void mapOffsetDateTimeToInstant_handlesNullGracefully() {
        assertThat(mapper.mapOffsetDateTimeToInstant(null)).isNull();
    }
}