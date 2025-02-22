package com.example.application.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TodoRepository extends JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {
    @Modifying
    @Query("update Todo t set t.status = 'DONE'")
    void updateAllTodosToDone();

    @Modifying
    @Query("delete Todo t")
    void removeAllTodos();
}
