package org.com.maher.todo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "todo_item", indexes = {
        @Index(name = "idx_status_due_datetime", columnList = "status, due_datetime")
})
@Getter
@Setter
@NoArgsConstructor
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TodoStatus status;

    @Column(nullable = false)
    private Instant creationDatetime;

    @Column(nullable = false)
    private Instant dueDatetime;

    private Instant doneDatetime;
}
