package org.com.maher.todo.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TodoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullTodoLifecycle() throws Exception {
        // 1. Create a todo
        MvcResult createResult = mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"description": "Integration test task", "dueDatetime": "2026-12-31T23:59:59Z"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NOT_DONE"))
                .andExpect(jsonPath("$.doneDatetime").isEmpty())
                .andReturn();

        String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        // 2. Get by ID
        mockMvc.perform(get("/api/v1/todos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Integration test task"));

        // 3. Update description
        mockMvc.perform(put("/api/v1/todos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"description": "Updated integration task"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated integration task"));

        // 4. Mark as done
        mockMvc.perform(patch("/api/v1/todos/{id}/done", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.doneDatetime").isNotEmpty());

        // 5. Mark as not done
        mockMvc.perform(patch("/api/v1/todos/{id}/not-done", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_DONE"))
                .andExpect(jsonPath("$.doneDatetime").isEmpty());

        // 6. List defaults to not-done items (paginated response)
        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void getTodos_pagination() throws Exception {
        // Create 3 items
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/v1/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"description\": \"Task " + i + "\", \"dueDatetime\": \"2026-12-31T23:59:59Z\"}"))
                    .andExpect(status().isCreated());
        }

        // Page 0: size=2
        mockMvc.perform(get("/api/v1/todos").param("status", "all").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(3));

        // Page 1: size=2
        mockMvc.perform(get("/api/v1/todos").param("status", "all").param("size", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalCount").value(3));
    }

    @Test
    void createTodo_rejectsPastDueDatetime() throws Exception {
        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"description": "Past task", "dueDatetime": "2020-01-01T10:00:00Z"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("dueDatetime must be in the future"));
    }

    @Test
    void getTodoById_returns404ForNonExistentId() throws Exception {
        mockMvc.perform(get("/api/v1/todos/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
