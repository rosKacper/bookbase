package com.bookbase.ui.views.authors;

import com.bookbase.backend.entity.Author;
import com.bookbase.backend.service.AuthorService;
import com.bookbase.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Calendar;


@Route(value = "Authors", layout = MainLayout.class)
@PageTitle("Authors | Bookbase")
@CssImport("./styles/shared-styles.css")
public class AuthorsView extends VerticalLayout {

    private final TextField filterFirstName = new TextField();
    private final TextField filterSecondName = new TextField();
    private final AuthorForm authorForm;
    private final Grid<Author> grid = new Grid<>(Author.class);
    private final AuthorService authorService;

    public AuthorsView(AuthorService authorService){
        this.authorService = authorService;
        addClassName("list-view");
        setSizeFull();
        configureGrid();

        authorForm = new AuthorForm();
        authorForm.addListener(AuthorForm.SaveEvent.class, this::saveAuthor);
        authorForm.addListener(AuthorForm.CloseEvent.class, closeEvent -> closeForm());
        authorForm.addListener(AuthorForm.DeleteEvent.class, this::deleteAuthor);

        Div content = new Div(grid, authorForm);
        content.addClassName("content");
        content.setSizeFull();

        add(
                new H1("Authors"),
                getToolBar(),
                content
        );
        updateList();
        closeForm();
    }

    private HorizontalLayout getToolBar() {
        filterFirstName.setPlaceholder("Filter by first name");
        filterSecondName.setPlaceholder("Filter by second name");
        filterFirstName.setClearButtonVisible(true);
        filterSecondName.setClearButtonVisible(true);
        filterFirstName.setValueChangeMode(ValueChangeMode.LAZY);
        filterSecondName.setValueChangeMode(ValueChangeMode.LAZY);
        filterFirstName.addValueChangeListener(e -> updateListFirstName());
        filterSecondName.addValueChangeListener(e -> updateListSecondName());

        Button addAuthorButton = new Button("Add new author", buttonClickEvent -> addAuthor());
        addAuthorButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        return new HorizontalLayout(filterFirstName, filterSecondName, addAuthorButton);
    }

    private void addAuthor() {
        grid.asSingleSelect().clear();
        editAuthor(new Author());
    }

    private void deleteAuthor(AuthorForm.DeleteEvent deleteEvent) {
        authorService.delete(deleteEvent.getAuthor());
        updateList();
        closeForm();
    }

    private void saveAuthor(AuthorForm.SaveEvent saveEvent) {
        authorService.save(saveEvent.getAuthor());
        updateList();
        closeForm();
    }

    private void closeForm() {
        authorForm.setAuthor(null);
        authorForm.setVisible(false);
        removeClassName("editing");
    }

    private void editAuthor(Author author) {
        if(author == null)
            closeForm();
        else {
            authorForm.setAuthor(author);
            authorForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void configureGrid() {
        grid.addClassName("author-grid");
        grid.setSizeFull();
        grid.removeColumnByKey("fullName");
//        grid.removeColumnByKey("Best Book");
        grid.removeColumnByKey("rating");
        grid.removeColumnByKey("birthYear");

        grid.addColumn(author -> Calendar.getInstance().get(Calendar.YEAR) - author.getBirthYear())
                .setHeader("age");

        grid.addColumn(author -> {
            double rating = author.getRating();
            if(rating == 0) {
                return "-";
            }
            else {
                return rating;
            }
        }).setHeader("Average rating");


//        grid.addColumn(author ->  {
//            String best = author.getBestBookTitle();
//            if(best == null)
//                return "-";
//            else
//                return best;
//        }).setHeader("Best book");

//        grid.asSingleSelect().addValueChangeListener(event -> editAuthor(event.getValue()));

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editAuthor(event.getOldValue()));
    }

    private void updateList() {
        grid.setItems(authorService.findAll());
    }

    private void updateListFirstName() { grid.setItems(authorService.findAllByFirstName(filterFirstName.getValue()));}

    private void updateListSecondName() { grid.setItems(authorService.findAllBySecondName(filterSecondName.getValue()));}
}