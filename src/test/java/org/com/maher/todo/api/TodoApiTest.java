package org.com.maher.todo.api;

import org.com.maher.todo.api.model.TodoPageResponse;
import org.com.maher.todo.api.model.TodoResponse;
import org.com.maher.todo.exception.PastDueModificationException;
import org.com.maher.todo.exception.TodoNotFoundException;
import org.com.maher.todo.service.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(TodosApiDelegateImpl.class)
class TodoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    private static final UUID TODO_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    // --- POST /api/v1/todos ---

    @Test
    void createTodo_returns201WithValidRequest() throws Exception {
        TodoResponse response = buildResponse(TODO_ID, "Buy groceries", TodoResponse.StatusEnum.NOT_DONE);
        when(todoService.createTodo(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"description": "Buy groceries", "dueDatetime": "2026-03-15T10:00:00"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TODO_ID.toString()))
                .andExpect(jsonPath("$.description").value("Buy groceries"))
                .andExpect(jsonPath("$.status").value("NOT_DONE"));
    }

    @Test
    void createTodo_returns400WhenDescriptionMissing() throws Exception {
        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"dueDatetime": "2026-03-15T10:00:00"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    // --- GET /api/v1/todos/{id} ---

    @Test
    void getTodoById_returns200WhenFound() throws Exception {
        TodoResponse response = buildResponse(TODO_ID, "Buy groceries", TodoResponse.StatusEnum.NOT_DONE);
        when(todoService.getTodoById(TODO_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/todos/{id}", TODO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TODO_ID.toString()))
                .andExpect(jsonPath("$.description").value("Buy groceries"));
    }

    @Test
    void getTodoById_returns404WhenNotFound() throws Exception {
        when(todoService.getTodoById(TODO_ID)).thenThrow(new TodoNotFoundException(TODO_ID));

        mockMvc.perform(get("/api/v1/todos/{id}", TODO_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Todo item not found with id: " + TODO_ID));
    }

    // --- GET /api/v1/todos ---

    @Test
    void getTodos_returns200WithPaginatedResponse() throws Exception {
        TodoResponse response = buildResponse(TODO_ID, "Buy groceries", TodoResponse.StatusEnum.NOT_DONE);
        TodoPageResponse pageResponse = new TodoPageResponse();
        pageResponse.setItems(List.of(response));
        pageResponse.setTotalCount(1L);
        when(todoService.getTodos(null, 0, 100)).thenReturn(pageResponse);
        // Generated controller injects defaults: page=0, size=100

        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(TODO_ID.toString()))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    // --- PUT /api/v1/todos/{id} ---

    @Test
    void updateDescription_returns200WithValidRequest() throws Exception {
        TodoResponse response = buildResponse(TODO_ID, "Updated", TodoResponse.StatusEnum.NOT_DONE);
        when(todoService.updateTodoDescription(eq(TODO_ID), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/todos/{id}", TODO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"description": "Updated"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void updateDescription_returns409WhenPastDue() throws Exception {
        when(todoService.updateTodoDescription(eq(TODO_ID), any()))
                .thenThrow(new PastDueModificationException(TODO_ID));

        mockMvc.perform(put("/api/v1/todos/{id}", TODO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"description": "Updated"}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateDescription_returns400WhenDescriptionEmpty() throws Exception {
        mockMvc.perform(put("/api/v1/todos/{id}", TODO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"description": ""}
                            """))
                .andExpect(status().isBadRequest());
    }

    // --- PATCH /api/v1/todos/{id}/done ---

    @Test
    void markDone_returns200() throws Exception {
        TodoResponse response = buildResponse(TODO_ID, "Buy groceries", TodoResponse.StatusEnum.DONE);
        when(todoService.markTodoDone(TODO_ID)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/todos/{id}/done", TODO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void markDone_returns409WhenPastDue() throws Exception {
        when(todoService.markTodoDone(TODO_ID)).thenThrow(new PastDueModificationException(TODO_ID));

        mockMvc.perform(patch("/api/v1/todos/{id}/done", TODO_ID))
                .andExpect(status().isConflict());
    }

    // --- PATCH /api/v1/todos/{id}/not-done ---

    @Test
    void markNotDone_returns200() throws Exception {
        TodoResponse response = buildResponse(TODO_ID, "Buy groceries", TodoResponse.StatusEnum.NOT_DONE);
        when(todoService.markTodoNotDone(TODO_ID)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/todos/{id}/not-done", TODO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_DONE"));
    }

    @Test
    void markNotDone_returns404WhenNotFound() throws Exception {
        when(todoService.markTodoNotDone(TODO_ID)).thenThrow(new TodoNotFoundException(TODO_ID));

        mockMvc.perform(patch("/api/v1/todos/{id}/not-done", TODO_ID))
                .andExpect(status().isNotFound());
    }

    // --- Optimistic locking ---

    @Test
    void markDone_returns409WhenOptimisticLockConflict() throws Exception {
        when(todoService.markTodoDone(TODO_ID))
                .thenThrow(new org.springframework.dao.OptimisticLockingFailureException("Version conflict"));

        mockMvc.perform(patch("/api/v1/todos/{id}/done", TODO_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("The item was modified by another request. Please retry."));
    }

    // --- Helper ---

    private TodoResponse buildResponse(UUID id, String description, TodoResponse.StatusEnum status) {
        TodoResponse response = new TodoResponse();
        response.setId(id);
        response.setDescription(description);
        response.setStatus(status);
        response.setCreationDatetime(LocalDateTime.of(2026, 2, 14, 10, 0));
        response.setDueDatetime(LocalDateTime.of(2026, 3, 15, 10, 0));
        return response;
    }
}