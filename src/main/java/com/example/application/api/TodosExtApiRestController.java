package com.example.application.api;

import com.example.application.data.TodoDto;
import com.example.application.data.TodoStatus;
import com.example.application.openapi.api.TodosApi;
import com.example.application.openapi.model.PagedToDoResponse;
import com.example.application.openapi.model.ToDo;
import com.example.application.services.TodoDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class TodosExtApiRestController implements TodosApi {
    @Autowired
    private TodoDtoService todoDtoService;

    public ResponseEntity<Void> createTodo(ToDo toDo) {
        todoDtoService.upsert(conv(toDo));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public ResponseEntity<Void> deleteTodo(Long id) {
        todoDtoService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<PagedToDoResponse> fetchTodosPaged(
            Integer page,
            Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TodoDto> todoPage = todoDtoService.list(pageRequest);
        PagedToDoResponse response = new PagedToDoResponse();
        response.size(todoPage.getSize());
        response.content(conv(todoPage));
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ToDo> getTodo(Long id) {
        Optional<TodoDto> todoDtoOptional = todoDtoService.get(id);
        if (todoDtoOptional.isPresent()) {
            ToDo todo = conv(todoDtoOptional.get());
            return ResponseEntity.ok(todo);
        } else {
            throw new RuntimeException("object not found");
        }
    }

    public ResponseEntity<Void> updateTodo(
            ToDo toDo) {
        todoDtoService.upsert(conv(toDo));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private List<ToDo> conv(Page<TodoDto> todoPage) {
        List<ToDo> result = new ArrayList<ToDo>();
        todoPage.getContent().forEach(todoEntry -> result.add(conv(todoEntry)));
        return result;
    }

    private ToDo conv(TodoDto todoDto) {
        ToDo todo = new ToDo();
        todo.setId(todoDto.getId());
        todo.setVersion(todoDto.getVersion());
        todo.setTitle(todoDto.getTitle());
        todo.setDescription(todoDto.getDescription());
        todo.setGrp(todoDto.getGrp());
        todo.status(ToDo.StatusEnum.valueOf(todoDto.getStatus().toString()));
        todo.setStartDate(todoDto.getStartDate());
        todo.setEndDate(todoDto.getEndDate());
        todo.setDueDate(todoDto.getDueDate());
        return todo;
    }

    private TodoDto conv(ToDo todo) {
        TodoDto todoDto = new TodoDto();
        todoDto.setId(todo.getId());
        if (todo.getVersion() != null) {
            todoDto.setVersion(todo.getVersion());

        }
        todoDto.setTitle(todo.getTitle());
        todoDto.setDescription(todo.getDescription());
        todoDto.setGrp(todo.getGrp());
        todoDto.setStatus(TodoStatus.valueOf(todo.getStatus().toString()));
        todoDto.setStartDate(todo.getStartDate());
        todoDto.setEndDate(todo.getEndDate());
        todoDto.setDueDate(todo.getDueDate());
        return todoDto;
    }

}
