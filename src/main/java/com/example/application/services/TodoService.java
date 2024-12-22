package com.example.application.services;

import com.example.application.data.Todo;
import com.example.application.data.TodoRepository;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public Optional<Todo> get(Long id) {
        return repository.findById(id);
    }

    public Todo update(Todo entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Todo> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Todo> list(Pageable pageable, Specification<Todo> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
