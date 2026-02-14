package org.com.maher.todo.api;

import lombok.RequiredArgsConstructor;
import org.com.maher.todo.api.model.CreateTodoRequest;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.api.model.UpdateTodoRequest;
import org.com.maher.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodosApiDelegateImpl implements TodosApiDelegate {

    private final TodoService todoService;

    @Override
    public ResponseEntity<TodoResponse> createTodo(CreateTodoRequest createTodoRequest) {
        TodoResponse response = todoService.createTodo(createTodoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<TodoResponse> getTodoById(UUID id) {
        TodoResponse response = todoService.getTodoById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TodoResponse> updateTodoDescription(UUID id, UpdateTodoRequest updateTodoRequest) {
        TodoResponse response = todoService.updateTodoDescription(id, updateTodoRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TodoResponse> markTodoDone(UUID id) {
        TodoResponse response = todoService.markTodoDone(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<TodoResponse>> getTodos(String status) {
        List<TodoResponse> responses = todoService.getTodos(status);
        return ResponseEntity.ok(responses);
    }
}
