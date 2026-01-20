package com.example.transferprojekt.javafx.views;

import com.example.transferprojekt.javafx.utils.AsyncDatabaseTask;
import com.example.transferprojekt.javafx.utils.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Base class for all list-based views in the GUI.
 * Provides a standardized layout with a search bar, CRUD buttons, and a TableView.
 *
 * @param <T> The type of the data model displayed in the table
 */
public abstract class BaseView<T> extends BorderPane {

    protected final TableView<T> tableView = new TableView<>();
    protected final ObservableList<T> dataList = FXCollections.observableArrayList();
    protected List<T> allDataCached;

    protected TextField searchField;
    protected Button addButton;
    protected Button editButton;
    protected Button deleteButton;

    public BaseView() {
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(20));

        // Create Toolbar
        HBox toolbar = createToolbar();

        // Create Table
        setupTableView(tableView);
        tableView.setItems(dataList);

        // Selection Listener
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            updateButtonStates(hasSelection);
        });

        // Double Click Listener
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });

        // Layout
        VBox content = new VBox(15, toolbar, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        setCenter(content);
    }

    private HBox createToolbar() {
        searchField = new TextField();
        searchField.setPromptText("Suchen...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterData(newText));

        addButton = new Button("Hinzufügen");
        addButton.setOnAction(e -> onAdd());

        editButton = new Button("Bearbeiten");
        editButton.setDisable(true);
        editButton.setOnAction(e -> onEdit());

        deleteButton = new Button("Löschen");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> onDelete());

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);
        HBox toolbar = new HBox(20, searchField, buttonBox);
        HBox.setHgrow(searchField, Priority.NEVER);

        return toolbar;
    }

    protected void updateButtonStates(boolean hasSelection) {
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * Set up the columns for the table view.
     */
    protected abstract void setupTableView(TableView<T> table);

    /**
     * Provides the database call for loading data.
     */
    protected abstract Callable<List<T>> getLoadTask();

    /**
     * Defines how to filter the data.
     */
    protected abstract boolean filterMatch(T item, String searchText);

    /**
     * Logic for adding a new item.
     */
    protected abstract void onAdd();

    /**
     * Logic for editing the selected item.
     */
    protected abstract void onEdit();

    /**
     * Logic for deleting the selected item.
     */
    protected abstract void onDelete();

    /**
     * Returns the error message to display when loading fails.
     */
    protected abstract String getLoadErrorMessage();

    /**
     * Loads data from the database asynchronously.
     */
    protected void loadData() {
        setButtonsEnabled(false);

        AsyncDatabaseTask.run(
                getLoadTask(),
                this,
                data -> {
                    allDataCached = data;
                    dataList.clear();
                    dataList.addAll(data);
                    searchField.clear();
                    setButtonsEnabled(true);
                },
                error -> {
                    DialogUtils.showError("Fehler beim Laden", getLoadErrorMessage() + "\n" + error.getMessage());
                    setButtonsEnabled(true);
                }
        );
    }

    /**
     * Filters the cached data and updates the observable list.
     */
    private void filterData(String searchText) {
        if (allDataCached == null) return;

        if (searchText == null || searchText.trim().isEmpty()) {
            dataList.clear();
            dataList.addAll(allDataCached);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        List<T> filtered = allDataCached.stream()
                .filter(item -> filterMatch(item, lowerCaseFilter))
                .toList();

        dataList.clear();
        dataList.addAll(filtered);
    }

    protected void setButtonsEnabled(boolean enabled) {
        addButton.setDisable(!enabled);
        if (enabled) {
            updateButtonStates(tableView.getSelectionModel().getSelectedItem() != null);
        } else {
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }
}
