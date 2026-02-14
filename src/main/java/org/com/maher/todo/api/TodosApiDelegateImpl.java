package org.com.maher.todo.api;

import lombok.RequiredArgsConstructor;
import org.com.maher.todo.api.model.CreateTodoRequest;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodosApiDelegateImpl implements TodosApiDelegate {

    private final TodoService todoService;

    @Override
    public ResponseEntity<TodoResponse> createTodo(CreateTodoRequest createTodoRequest) {
        TodoResponse response = todoService.createTodo(createTodoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
