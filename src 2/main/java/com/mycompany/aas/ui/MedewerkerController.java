package com.mycompany.aas.ui;

import com.mycompany.aas.AppState;
import com.mycompany.aas.dao.AannameDAO;
import com.mycompany.aas.dao.RolDAO;
import com.mycompany.aas.model.Aanname;

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

public class MedewerkerController {

    private final AannameDAO aannameDAO = new AannameDAO();
    private final RolDAO rolDAO = new RolDAO();

    private final BorderPane root = new BorderPane();

    private final TextField tfActorEmail = new TextField();
    private final TextField tfTargetEmail = new TextField();
    private final Button btnLaadAannames = new Button("Laad aannames");

    private final ObservableList<Aanname> aannames = FXCollections.observableArrayList();
    private final TableView<Aanname> table = new TableView<>(aannames);

    private final ComboBox<StatusItem> cbStatus = new ComboBox<>();
    private final Button btnOpslaan = new Button("Status opslaan");

    private final Label lblInfo = new Label("");

    public MedewerkerController() {
        buildUI();
        wireEvents();
    }

    public Parent getView() {
        return root;
    }

    private void buildUI() {
        Label title = new Label("Medewerker – Status van aanname wijzigen");
        title.setFont(Font.font(18));

        // === Actor email (medewerker) ===
        tfActorEmail.setPromptText("medewerker@aas.nl");
        String current = AppState.getInstance().getCurrentEmail();
        if (current != null && !current.isBlank()) {
            tfActorEmail.setText(current);
        }

        // === Target email (deelnemer) ===
        tfTargetEmail.setPromptText("deelnemer@aas.nl");

        // === Status dropdown ===
        cbStatus.getItems().setAll(
                new StatusItem(0, "In behandeling"),
                new StatusItem(1, "Goedgekeurd"),
                new StatusItem(2, "Afgekeurd")
        );
        cbStatus.getSelectionModel().selectFirst();

        btnOpslaan.setDisable(true);

        // === Form grid ===
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Medewerker e-mail:"), 0, 0);
        form.add(tfActorEmail, 1, 0);

        form.add(new Label("Deelnemer e-mail:"), 0, 1);
        form.add(tfTargetEmail, 1, 1);
        form.add(btnLaadAannames, 2, 1);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(160);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setMinWidth(140);
        form.getColumnConstraints().addAll(c0, c1, c2);

        // === Status rij (ONDER emailvelden) ===
        HBox statusRow = new HBox(10,
                new Label("Nieuwe status:"),
                cbStatus,
                btnOpslaan
        );
        statusRow.setAlignment(Pos.CENTER_LEFT);

        // === Top ===
        VBox top = new VBox(12,
                title,
                form,
                statusRow,
                lblInfo
        );
        top.setPadding(new Insets(12));

        // === Tabel ===
        configureTable();

        root.setTop(top);
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(0, 12, 12, 12));
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Aanname, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cd
                -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().getEmail()));

        TableColumn<Aanname, Integer> colKunstwerk = new TableColumn<>("Kunstwerk ID");
        colKunstwerk.setCellValueFactory(cd
                -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getKunstwerkId()));

        TableColumn<Aanname, LocalDateTime> colStart = new TableColumn<>("Startdatum");
        colStart.setCellValueFactory(cd
                -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getStartdatum()));

        TableColumn<Aanname, LocalDateTime> colEind = new TableColumn<>("Einddatum");
        colEind.setCellValueFactory(cd
                -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getEinddatum()));

        TableColumn<Aanname, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cd
                -> new javafx.beans.property.ReadOnlyStringWrapper(statusText(cd.getValue().getStatus())));

        TableColumn<Aanname, Integer> colType = new TableColumn<>("Aanname type");
        colType.setCellValueFactory(cd
                -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getAannametype()));

        table.getColumns().setAll(
                colEmail, colKunstwerk, colStart, colEind, colStatus, colType
        );

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            btnOpslaan.setDisable(sel == null);

            if (sel != null) {
                Integer s = sel.getStatus();
                int value = (s == null) ? 0 : s;
                cbStatus.getItems().stream()
                        .filter(it -> it.id == value)
                        .findFirst()
                        .ifPresent(it -> cbStatus.getSelectionModel().select(it));
            }
        });
    }

    private void wireEvents() {
        btnLaadAannames.setOnAction(e -> loadAannames());
        btnOpslaan.setOnAction(e -> saveStatus());
    }

    private void loadAannames() {
        lblInfo.setText("");

        String actor = safe(tfActorEmail.getText());
        String target = safe(tfTargetEmail.getText());

        if (actor.isEmpty() || target.isEmpty()) {
            showError("Vul zowel medewerker- als deelnemer-e-mail in.");
            return;
        }

        try {
            if (!rolDAO.isMedewerker(actor)) {
                showError("Deze gebruiker is geen medewerker.");
                return;
            }

            List<Aanname> rows = aannameDAO.listAannamesByEmail(target);
            aannames.setAll(rows);

            lblInfo.setText(rows.isEmpty()
                    ? "Geen aannames gevonden."
                    : "Aannames geladen: " + rows.size());

        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    private void saveStatus() {
        Aanname sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        String actor = safe(tfActorEmail.getText());
        StatusItem st = cbStatus.getValue();

        if (actor.isEmpty() || st == null) {
            showError("Ongeldige invoer.");
            return;
        }

        try {
            aannameDAO.updateStatusAsActor(
                    actor,
                    sel.getEmail(),
                    sel.getKunstwerkId(),
                    sel.getStartdatum(),
                    st.id
            );

            loadAannames();
            lblInfo.setText("Status succesvol bijgewerkt.");

        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }



    private String statusText(Integer s) {
        if (s == null) {
            return "-";
        }
        if (s == 0) {
            return "In behandeling";
        }
        if (s == 1) {
            return "Goedgekeurd";
        }
        if (s == 2) {
            return "Afgekeurd";
        }
        return String.valueOf(s);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR,
                (msg == null || msg.isBlank()) ? "Onbekende fout." : msg,
                ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private static final class StatusItem {

        final int id;
        final String label;

        StatusItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
