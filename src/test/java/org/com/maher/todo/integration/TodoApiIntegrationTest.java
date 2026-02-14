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
                            {"description": "Integration test task", "dueDatetime": "2026-12-31T23:59:59"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("not done"))
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
                .andExpect(jsonPath("$.status").value("done"))
                .andExpect(jsonPath("$.doneDatetime").isNotEmpty());

        // 5. Mark as not done
        mockMvc.perform(patch("/api/v1/todos/{id}/not-done", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("not done"))
                .andExpect(jsonPath("$.doneDatetime").isEmpty());

        // 6. List defaults to not-done items
        mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void getTodoById_returns404ForNonExistentId() throws Exception {
        mockMvc.perform(get("/api/v1/todos/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
