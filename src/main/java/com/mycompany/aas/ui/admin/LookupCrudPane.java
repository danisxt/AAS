package com.mycompany.aas.ui.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;

public class LookupCrudPane<T> {

    public interface CrudService<T> {

        void create(T value) throws Exception;

        List<T> listAll() throws Exception;

        void update(T oldValue, T newValue) throws Exception;

        void delete(T value) throws Exception;

        String format(T value);

        T parse(String raw) throws IllegalArgumentException;

        String hintText();
    }

    private final CrudService<T> service;
    private final String title;

    private final BorderPane root = new BorderPane();

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ListView<T> listView = new ListView<>(items);

    private final TextField tfValue = new TextField();
    private final TextField tfNewValue = new TextField();

    private final Button btnCreate = new Button("Create");
    private final Button btnUpdate = new Button("Update");
    private final Button btnDelete = new Button("Delete");
    private final Button btnRefresh = new Button("Refresh");

    public LookupCrudPane(String title, CrudService<T> service) {
        this.title = title;
        this.service = service;
        buildUI();
        wireEvents();
        refresh();
    }

    public Parent getView() {
        return root;
    }

    private void buildUI() {
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font(18));

        Label lblValue = new Label("Waarde:");
        tfValue.setPromptText(service.hintText());

        Label lblNewValue = new Label("Nieuwe waarde (voor Update):");
        tfNewValue.setPromptText(service.hintText());

        // Links: invoer + knoppen
        VBox left = new VBox(10,
                lblTitle,
                new Separator(),
                lblValue, tfValue,
                lblNewValue, tfNewValue,
                new Separator(),
                btnCreate,
                btnUpdate,
                btnDelete,
                btnRefresh
        );
        left.setPadding(new Insets(12));
        left.setPrefWidth(360);
        left.setMaxWidth(360);

        btnCreate.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnRefresh.setMaxWidth(Double.MAX_VALUE);

        // Rechts: lijst
        Label lblList = new Label("Bestaande waarden:");
        lblList.setStyle("-fx-font-weight: bold;");

        listView.setCellFactory(lv -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : service.format(item));
            }
        });

        VBox right = new VBox(8, lblList, listView);
        right.setPadding(new Insets(12));
        VBox.setVgrow(listView, Priority.ALWAYS);

        SplitPane split = new SplitPane(left, right);
        split.setDividerPositions(0.35);

        root.setCenter(split);

        // selectie > zet value field
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                tfValue.setText(service.format(sel));
            }
        });
    }

    private void wireEvents() {
        btnRefresh.setOnAction(e -> refresh());

        btnCreate.setOnAction(e -> {
            try {
                T v = service.parse(tfValue.getText());
                service.create(v);
                refresh();
                showInfo("Create gelukt");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        btnUpdate.setOnAction(e -> {
            try {
                T oldV = service.parse(tfValue.getText());
                T newV = service.parse(tfNewValue.getText());
                service.update(oldV, newV);
                refresh();
                showInfo("Update gelukt");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        btnDelete.setOnAction(e -> {
            try {
                T v = service.parse(tfValue.getText());

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Weet je zeker dat je deze waarde wilt verwijderen?\n\n" + service.format(v),
                        ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText(null);

                if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                    return;
                }

                service.delete(v);
                refresh();
                showInfo("Delete gelukt");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void refresh() {
        try {
            items.setAll(service.listAll());
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, (msg == null || msg.isBlank()) ? "Onbekende fout." : msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
