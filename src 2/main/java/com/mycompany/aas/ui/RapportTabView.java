package com.mycompany.aas.ui;

import com.mycompany.aas.AppState;
import com.mycompany.aas.dao.AannameDAO;
import com.mycompany.aas.dao.RapportDAO;
import com.mycompany.aas.model.Aanname;
import com.mycompany.aas.model.Rapport;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class RapportTabView {

    private final AannameDAO aannameDAO = new AannameDAO();
    private final RapportDAO rapportDAO = new RapportDAO();

    private final ObservableList<Aanname> aannames = FXCollections.observableArrayList();
    private final ObservableList<String> condities = FXCollections.observableArrayList();
    private final ObservableList<Rapport> rapporten = FXCollections.observableArrayList();

    public Parent build() {
        Label title = new Label("Rapporten");
        title.setFont(Font.font(18));

        // ===== Links: form =====
        TextField emailField = new TextField();
        emailField.setPromptText("email@aas.nl");
        emailField.textProperty().bindBidirectional(AppState.getInstance().currentEmailProperty());

        Button loadAannamesBtn = new Button("Laad adopties");

        ComboBox<Aanname> aannameCombo = new ComboBox<>(aannames);
        aannameCombo.setPrefWidth(320);

        ComboBox<String> conditieCombo = new ComboBox<>(condities);
        conditieCombo.setPrefWidth(200);

        Button saveBtn = new Button("Sla rapport op");
        Button updateBtn = new Button("Wijzig geselecteerd");
        Button deleteBtn = new Button("Verwijder geselecteerd");

        // knoppen alleen actief bij selectie in tabel
        TableView<Rapport> table = buildTable();
        updateBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        // bij selectie in tabel: conditieCombo mee zetten
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                conditieCombo.getSelectionModel().select(sel.getConditie());
            }
        });

        // Email rij: links email veld, rechts knop "Laad aannames"
        Label lblEmail = new Label("Email:");
        lblEmail.setMinWidth(70);
        HBox emailRow = new HBox(10, lblEmail, emailField, loadAannamesBtn);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        // Adoptie dropdown onder elkaar
        Label lblAanname = new Label("Adoptie:");
        lblAanname.setMinWidth(70);
        VBox aannameBox = new VBox(6, lblAanname, aannameCombo);

        // Conditie onder elkaar
        Label lblConditie = new Label("Conditie:");
        lblConditie.setMinWidth(70);
        VBox conditieBox = new VBox(6, lblConditie, conditieCombo);

        // Actieknoppen onder conditie
        VBox actionButtons = new VBox(8, saveBtn, updateBtn, deleteBtn);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setMaxWidth(Double.MAX_VALUE);

        // links
        VBox leftForm = new VBox(12,
                emailRow,
                new Separator(),
                aannameBox,
                conditieBox,
                actionButtons
        );
        leftForm.setPadding(new Insets(12));
        leftForm.setPrefWidth(420);
        leftForm.setMaxWidth(420);

        //rechts 
        VBox rightTable = new VBox(10, table);
        rightTable.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);

        // Split: links form, rechts tabel
        SplitPane split = new SplitPane(leftForm, rightTable);
        split.setDividerPositions(0.33);

        VBox root = new VBox(10, title, split);
        root.setPadding(new Insets(12));
        VBox.setVgrow(split, Priority.ALWAYS);

        
        try {
            List<String> opts = rapportDAO.listConditieOpties();
            condities.setAll(opts);
            if (!opts.isEmpty()) {
                conditieCombo.getSelectionModel().selectFirst();
            }
        } catch (SQLException ex) {
            showError("Database fout", ex.getMessage());
        }

        // events
        loadAannamesBtn.setOnAction(e -> {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            if (email.isEmpty()) {
                showInfo("Email ontbreekt", "Vul een email in om aannames te laden.");
                return;
            }

            try {
                List<Aanname> lijst = aannameDAO.listAannamesByEmail(email);
                aannames.setAll(lijst);

                if (lijst.isEmpty()) {
                    rapporten.clear();
                    showInfo("Geen aannames", "Geen aannames gevonden voor: " + email);
                    return;
                }

                aannameCombo.getSelectionModel().selectFirst();
                refreshRapporten(aannameCombo.getValue(), table);

            } catch (SQLException ex) {
                showError("Database fout", ex.getMessage());
            }
        });

        aannameCombo.setOnAction(e -> {
            Aanname a = aannameCombo.getValue();
            if (a != null) {
                refreshRapporten(a, table);
            }
        });

        // CREATE
        saveBtn.setOnAction(e -> {
            Aanname a = aannameCombo.getValue();
            if (a == null) {
                showInfo("Geen adoptie", "Kies eerst een adoptie.");
                return;
            }

            String conditie = conditieCombo.getValue();
            if (conditie == null || conditie.isBlank()) {
                showInfo("Geen conditie", "Kies een conditie (Goed/Matig/Slecht).");
                return;
            }

            try {
                rapportDAO.createRapport(
                        a.getEmail(),
                        a.getKunstwerkId(),
                        a.getStartdatum(),
                        LocalDateTime.now(),
                        conditie
                );
                refreshRapporten(a, table);
                showInfo("Opgeslagen", "Rapport is opgeslagen.");
            } catch (SQLException ex) {
                showError("Opslaan mislukt", ex.getMessage());
            }
        });

        // UPDATE
        updateBtn.setOnAction(e -> {
            Aanname a = aannameCombo.getValue();
            Rapport sel = table.getSelectionModel().getSelectedItem();
            if (a == null || sel == null) {
                showInfo("Selecteer", "Kies een adoptie en selecteer een rapport.");
                return;
            }

            String newConditie = conditieCombo.getValue();
            if (newConditie == null || newConditie.isBlank()) {
                showInfo("Geen conditie", "Kies een conditie.");
                return;
            }

            try {
                rapportDAO.updateRapportConditie(
                        sel.getEmail(),
                        sel.getKunstwerkId(),
                        sel.getStartdatum(),
                        sel.getAangemaaktOp(),
                        newConditie
                );
                refreshRapporten(a, table);
                showInfo("Bijgewerkt", "Rapport is bijgewerkt.");
            } catch (SQLException ex) {
                showError("Update mislukt", ex.getMessage());
            }
        });

        // DELETE
        deleteBtn.setOnAction(e -> {
            Aanname a = aannameCombo.getValue();
            Rapport sel = table.getSelectionModel().getSelectedItem();
            if (a == null || sel == null) {
                showInfo("Selecteer", "Kies een adoptie en selecteer een rapport.");
                return;
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Weet je zeker dat je dit rapport wilt verwijderen?\n\n"
                    + "Aangemaakt op: " + sel.getAangemaaktOp() + "\nConditie: " + sel.getConditie(),
                    ButtonType.YES, ButtonType.NO
            );
            confirm.setHeaderText(null);

            ButtonType res = confirm.showAndWait().orElse(ButtonType.NO);
            if (res != ButtonType.YES) {
                return;
            }

            try {
                rapportDAO.deleteRapport(
                        sel.getEmail(),
                        sel.getKunstwerkId(),
                        sel.getStartdatum(),
                        sel.getAangemaaktOp()
                );
                refreshRapporten(a, table);
                showInfo("Verwijderd", "Rapport is verwijderd.");
            } catch (SQLException ex) {
                showError("Delete mislukt", ex.getMessage());
            }
        });

        // Optioneel: automatisch laden als er al email staat
        String existing = AppState.getInstance().getCurrentEmail();
        if (existing != null && !existing.isBlank()) {
      
        }

        return root;
    }

    private TableView<Rapport> buildTable() {
        TableView<Rapport> table = new TableView<>(rapporten);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Rapport, LocalDateTime> colTime = new TableColumn<>("Aangemaakt op");
        colTime.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getAangemaaktOp()));

        TableColumn<Rapport, String> colCond = new TableColumn<>("Conditie");
        colCond.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getConditie()));

        table.getColumns().addAll(colTime, colCond);

        
        table.setPrefWidth(900);
        table.setPrefHeight(600);

        return table;
    }

    private void refreshRapporten(Aanname a, TableView<Rapport> table) {
        try {
            rapporten.setAll(rapportDAO.listRapporten(a.getEmail(), a.getKunstwerkId(), a.getStartdatum()));
            table.getSelectionModel().clearSelection();
        } catch (SQLException ex) {
            showError("Database fout", ex.getMessage());
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg == null ? "Onbekende fout." : msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
