package com.example.application.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class TodoRepositoryTest {
    private static final int TODOS_CREATED = 4;
    @Autowired
    private TodoRepository todoRepository;
    private Map<String, Todo> idMap = new HashMap<>();

    @BeforeEach
    public void setUp() {
        // Initialize test data before each test method
        todoRepository.deleteAll();
        long batchEntityId = 100L;
        List<Todo> todoBatch = new ArrayList<>();
        for (int i = 0; i < TODOS_CREATED; i++) {
            Todo todo = new Todo();
            todo.setTitle("batch-t-" + batchEntityId);
            todo.setDescription("batch-d-" + batchEntityId);
            todo.setStatus(TodoStatus.PENDING);
            todo.setGrp("batch");
            todoBatch.add(todo);
            batchEntityId++;
        }
        List<Todo> savedTodoBatch = todoRepository.saveAll(todoBatch);
        savedTodoBatch.forEach(savedTodo -> idMap.put(savedTodo.getTitle(), savedTodo));
    }

    @Test
    void testCount() {
        long todosCount = todoRepository.count();
        assertEquals(TODOS_CREATED, todosCount);
    }

    @Test
    void testFindById() {
        String todo1Title = "batch-t-100";
        long todoId1 = idMap.get(todo1Title).getId();
        Todo todo = todoRepository.findById(todoId1).orElse(null);
        assertNotNull(todo);
        assertEquals(todo1Title, todo.getTitle());
    }
}
