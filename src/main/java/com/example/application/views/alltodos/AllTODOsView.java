package com.example.application.views.alltodos;

import com.example.application.data.TodoDto;
import com.example.application.data.TodoStatus;
import com.example.application.services.TodoDtoService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("All TODOs")
@Route("/:todoID?/:action?(edit)")
@Menu(order = 0, icon = LineAwesomeIconUrl.COLUMNS_SOLID)
@RouteAlias("")
public class AllTODOsView extends Div implements BeforeEnterObserver {

    private final String TODO_ID = "todoID";
    private final String TODO_EDIT_ROUTE_TEMPLATE = "/%s/edit";

    private final Grid<TodoDto> grid = new Grid<>(TodoDto.class, false);

    private TextField title;
    private TextField description;
    private ComboBox<TodoStatus> status;
    private DatePicker startDate;
    private DatePicker dueDate;
    private DateTimePicker endDate;
    private TextField grp;
    private TextField version;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button updateAllTodosToDone = new Button("Update all Todos to done");
    private final Button removeAllTodos = new Button("Remove all Todos");

    private final BeanValidationBinder<TodoDto> binder;

    private TodoDto todo;

    private final TodoDtoService todoService;

    public AllTODOsView(TodoDtoService todoService) {
        this.todoService = todoService;
        addClassNames("all-tod-os-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("title").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);
        grid.addColumn("status").setAutoWidth(true);
        grid.addColumn("startDate").setAutoWidth(true);
        grid.addColumn("dueDate").setAutoWidth(true);
        grid.addColumn("endDate").setAutoWidth(true);
        grid.addColumn("grp").setAutoWidth(true);
        grid.addColumn(
                new NativeButtonRenderer<>("Remove",
                        clickedItem -> {
                            todoService.delete(clickedItem.getId());
                            refreshGrid();
                        })
        ).setAutoWidth(true);

        grid.setItems(query -> todoService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(TODO_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AllTODOsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(TodoDto.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        updateAllTodosToDone.addClickListener(e -> {
            clearForm();
            todoService.updateAllTodosToDone();
            refreshGrid();
        });

        removeAllTodos.addClickListener(e -> {
            clearForm();
            todoService.removeAllTodos();
            refreshGrid();
        });

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.todo == null) {
                    this.todo = new TodoDto();
                    version.setValue("0");
                }
                binder.writeBean(this.todo);
                todoService.upsert(this.todo);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(AllTODOsView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            } catch (RuntimeException runtimeException) {
                Notification.show(runtimeException.toString());
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> todoId = event.getRouteParameters().get(TODO_ID).map(Long::parseLong);
        if (todoId.isPresent()) {
            Optional<TodoDto> todoFromBackend = todoService.get(todoId.get());
            if (todoFromBackend.isPresent()) {
                populateForm(todoFromBackend.get());
            } else {
                Notification.show(String.format("The requested todo was not found, ID = %s", todoId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AllTODOsView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        title = new TextField("Title");
        description = new TextField("Description");
        status = new ComboBox<>("Status");
        status.setItems(TodoStatus.values());
        startDate = new DatePicker("Start Date");
        dueDate = new DatePicker("Due Date");
        endDate = new DateTimePicker("End Date");
        endDate.setStep(Duration.ofSeconds(1));
        grp = new TextField("Group");
        version = new TextField("Version");
        version.setReadOnly(true);
        formLayout.add(title, description, status, startDate, dueDate, endDate, grp, version);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGlobalButtonLayout(Div wrapper) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        updateAllTodosToDone.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        removeAllTodos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(updateAllTodosToDone, removeAllTodos);
        wrapper.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        createGlobalButtonLayout(wrapper);
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(TodoDto value) {
        this.todo = value;
        binder.readBean(this.todo);

    }
}
