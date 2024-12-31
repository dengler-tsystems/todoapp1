package com.example.application.data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodoDto {

    private Long id;
    private int version;
    private String title;
    private String description;
    private TodoStatus status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime endDate;
    private String grp;

     public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public TodoStatus getStatus() {
        return status;
    }
    public void setStatus(TodoStatus status) {
        this.status = status;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public String getGrp() {
        return grp;
    }
    public void setGrp(String grp) {
        this.grp = grp;
    }

}
