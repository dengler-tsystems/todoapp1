package com.example.application.services;

import com.example.application.data.Todo;
import com.example.application.data.TodoDto;
import com.example.application.data.TodoRepository;
import com.example.application.data.TodoStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoDtoService {
    private long batchEntityId = 100;
    private final TodoRepository repository;

    public TodoDtoService(TodoRepository repository) {
        this.repository = repository;
    }

    public Optional<TodoDto> get(Long id) {
        Optional<Todo> todoRef = repository.findById(id);
        return todoRef.map(this::convToDto);
    }

    public TodoDto upsert(TodoDto dto) {
        if (dto.getId() == null) {
            Todo newTodo = new Todo();
            updateEntityData(dto, newTodo, false);
            // saveAndFlush is needed instead of save, if only save is used the DTO will contain the old version value
            Todo addedTodoEntity = repository.saveAndFlush(newTodo);
            System.out.println("added todo with id=" + addedTodoEntity.getId());
            return convToDto(addedTodoEntity);
        } else {
            Optional<Todo> todoEntityRef = repository.findById(dto.getId());
            Todo todoEntityFromDb = todoEntityRef.orElseThrow(RuntimeException::new);
            if (todoEntityFromDb.getVersion() != dto.getVersion()) {
                throw new ObjectOptimisticLockingFailureException(Todo.class.getName(), todoEntityFromDb.getId());
            } else {
                updateEntityData(dto, todoEntityFromDb, false);
                int versionTodoEntityFromDb = todoEntityFromDb.getVersion();
                // saveAndFlush is needed instead of save, if only save is used the DTO will contain the old version value
                Todo updatedTodoEntity = repository.saveAndFlush(todoEntityFromDb);
                System.out.println("version of todoEntityFromDb=" + versionTodoEntityFromDb);
                System.out.println("version of updatedTodoEntity=" + updatedTodoEntity.getVersion());
                return convToDto(updatedTodoEntity);
            }
        }

    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<TodoDto> list(Pageable pageable) {
        Page<Todo> todoPage = repository.findAll(pageable);
        return convToDtoPage(pageable, todoPage);
    }

    public Page<TodoDto> list(Pageable pageable, Specification<Todo> filter) {
        Page<Todo> todoPage = repository.findAll(filter, pageable);
        return convToDtoPage(pageable, todoPage);
    }

    public void updateAllTodosToDone() {
        repository.updateAllTodosToDone();
    }

    public void removeAllTodos() {
        repository.removeAllTodos();
    }

    public int count() {
        return (int) repository.count();
    }

    public void addSomeTodosAsBatch() {
        List<Todo> todoBatch = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Todo todo = new Todo();
            todo.setTitle("batch-t-" + batchEntityId);
            todo.setDescription("batch-d-" + batchEntityId);
            todo.setStatus(TodoStatus.PENDING);
            todo.setGrp("batch");
            todoBatch.add(todo);
            batchEntityId++;
        }
        repository.saveAll(todoBatch);
    }

    private Page<TodoDto> convToDtoPage(Pageable pageable, Page<Todo> todoPage) {
        List<TodoDto> dtoList = convToDtoList(todoPage.stream().toList());
        PageImpl<TodoDto> dtoPage = new PageImpl<>(dtoList, pageable, todoPage.getTotalPages());
        return dtoPage;

    }

    private TodoDto convToDto(Todo todo) {
        TodoDto result = new TodoDto();
        result.setId(todo.getId());
        result.setVersion(todo.getVersion());
        result.setTitle(todo.getTitle());
        result.setDescription(todo.getDescription());
        result.setStatus(todo.getStatus());
        result.setStartDate(todo.getStartDate());
        result.setDueDate(todo.getDueDate());
        result.setEndDate(todo.getEndDate());
        result.setGrp(todo.getGrp());
        return result;
    }

    private List<TodoDto> convToDtoList(List<Todo> todoList) {
        List<TodoDto> result = new ArrayList<>(todoList.size());
        todoList.forEach(todo -> result.add(convToDto(todo)));
        return result;
    }

    private void updateEntityData(TodoDto todoDto, Todo todo, boolean withdId) {
        if (withdId) {
            todo.setId(todoDto.getId());
        }
        todo.setTitle(todoDto.getTitle());
        todo.setDescription(todoDto.getDescription());
        todo.setStatus(todoDto.getStatus());
        todo.setStartDate(todoDto.getStartDate());
        todo.setDueDate(todoDto.getDueDate());
        todo.setEndDate(todoDto.getEndDate());
        todo.setGrp(todoDto.getGrp());
    }

}
