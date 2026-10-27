package com.mycompany.aas.ui.admin;

import com.mycompany.aas.dao.admin.*;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class BeheerController {

    private final TabPane root = new TabPane();

    public BeheerController() {
        root.getTabs().add(tabEmail());
        root.getTabs().add(tabVoornaam());
        root.getTabs().add(tabAchternaam());
        root.getTabs().add(tabConditie());
        root.getTabs().add(tabStatus());
        root.getTabs().add(tabAannameType());
        root.getTabs().add(tabKunstwerkIds()); 
    }

    public Parent getView() {
        return root;
    }

    private Tab tabEmail() {
        EmailadresDAO dao = new EmailadresDAO();
        LookupCrudPane<String> pane = new LookupCrudPane<>("Emailadressen (CRUD)", new LookupCrudPane.CrudService<String>() {
            @Override
            public void create(String value) throws Exception {
                dao.create(value);
            }

            @Override
            public List<String> listAll() throws Exception {
                return dao.listAll();
            }

            @Override
            public void update(String oldValue, String newValue) throws Exception {
                dao.update(oldValue, newValue);
            }

            @Override
            public void delete(String value) throws Exception {
                dao.delete(value);
            }

            @Override
            public String format(String value) {
                return value;
            }

            @Override
            public String parse(String raw) {
                if (raw == null || raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vul een email in.");
                }
                return raw.trim();
            }

            @Override
            public String hintText() {
                return "bijv. test@aas.nl";
            }
        });
        Tab t = new Tab("Email", pane.getView());
        t.setClosable(false);
        return t;
    }

    private Tab tabVoornaam() {
        VoornaamDAO dao = new VoornaamDAO();
        LookupCrudPane<String> pane = new LookupCrudPane<>("Voornamen (CRUD)", new LookupCrudPane.CrudService<String>() {
            @Override
            public void create(String value) throws Exception {
                dao.create(value);
            }

            @Override
            public List<String> listAll() throws Exception {
                return dao.listAll();
            }

            @Override
            public void update(String oldValue, String newValue) throws Exception {
                dao.update(oldValue, newValue);
            }

            @Override
            public void delete(String value) throws Exception {
                dao.delete(value);
            }

            @Override
            public String format(String value) {
                return value;
            }

            @Override
            public String parse(String raw) {
                if (raw == null || raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vul een voornaam in.");
                }
                return raw.trim();
            }

            @Override
            public String hintText() {
                return "bijv. Dani";
            }
        });
        Tab t = new Tab("Voornaam", pane.getView());
        t.setClosable(false);
        return t;
    }

    private Tab tabAchternaam() {
        AchternaamDAO dao = new AchternaamDAO();
        LookupCrudPane<String> pane = new LookupCrudPane<>("Achternamen (CRUD)", new LookupCrudPane.CrudService<String>() {
            @Override
            public void create(String value) throws Exception {
                dao.create(value);
            }

            @Override
            public List<String> listAll() throws Exception {
                return dao.listAll();
            }

            @Override
            public void update(String oldValue, String newValue) throws Exception {
                dao.update(oldValue, newValue);
            }

            @Override
            public void delete(String value) throws Exception {
                dao.delete(value);
            }

            @Override
            public String format(String value) {
                return value;
            }

            @Override
            public String parse(String raw) {
                if (raw == null || raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vul een achternaam in.");
                }
                return raw.trim();
            }

            @Override
            public String hintText() {
                return "bijv. Schot";
            }
        });
        Tab t = new Tab("Achternaam", pane.getView());
        t.setClosable(false);
        return t;
    }

    private Tab tabConditie() {
        ConditieDAO dao = new ConditieDAO();
        LookupCrudPane<String> pane = new LookupCrudPane<>("Conditie (CRUD)", new LookupCrudPane.CrudService<String>() {
            @Override
            public void create(String value) throws Exception {
                dao.create(value);
            }

            @Override
            public List<String> listAll() throws Exception {
                return dao.listAll();
            }

            @Override
            public void update(String oldValue, String newValue) throws Exception {
                dao.update(oldValue, newValue);
            }

            @Override
            public void delete(String value) throws Exception {
                dao.delete(value);
            }

            @Override
            public String format(String value) {
                return value;
            }

            @Override
            public String parse(String raw) {
                if (raw == null || raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vul een conditie in.");
                }
                return raw.trim();
            }

            @Override
            public String hintText() {
                return "bijv. Goed";
            }
        });
        Tab t = new Tab("Conditie", pane.getView());
        t.setClosable(false);
        return t;
    }

    private Tab tabStatus() {
        StatusDAO dao = new StatusDAO();
        LookupCrudPane<Integer> pane = new LookupCrudPane<>("Statussen (CRUD)", new LookupCrudPane.CrudService<Integer>() {
            @Override
            public void create(Integer value) throws Exception {
                dao.create(value);
            }

            @Override
            public List<Integer> listAll() throws Exception {
                return dao.listAll();
            }

            @Override
            public void update(Integer oldValue, Integer newValue) throws Exception {
                dao.update(oldValue, newValue);
            }

            @Override
            public void delete(Integer value) throws Exception {
                dao.delete(value);
            }

            @Override
            public String format(Integer value) {
                return String.valueOf(value);
            }

            @Override
            public Integer parse(String raw) {
                if (raw == null || raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vul een status (getal) in.");
                }
                try {
                    int v = Integer.parseInt(raw.trim());
                    if (v < 0 || v > 127) {
                        throw new IllegalArgumentException("Status moet tussen 0 en 127 liggen (TINYINT).");
                    }
                    return v;
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Status moet een getal zijn.");
                }
            }

            @Override
            public String hintText() {
                return "bijv. 0";
            }
        });
        Tab t = new Tab("Status", pane.getView());
        t.setClosable(false);
        return t;
    }

    private Tab tabAannameType() {
        AannameTypeDAO dao = new AannameTypeDAO();
        LookupCrudPane<Integer> pane = new LookupCrudPane<>("AannameTypes (CRUD)", new LookupCrudPane.CrudService<Integer>() {
            @Override
            public void create(Integer value) throws Exception {
                dao.create(value);
            }

            @Override
            public List<Integer> listAll() throws Exception {
                return dao.listAll();
            }

            @Override
            public void update(Integer oldValue, Integer newValue) throws Exception {
                dao.update(oldValue, newValue);
            }

            @Override
            public void delete(Integer value) throws Exception {
                dao.delete(value);
            }

            @Override
            public String format(Integer value) {
                return String.valueOf(value);
            }

            @Override
            public Integer parse(String raw) {
                if (raw == null || raw.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vul een aannametype (getal) in.");
                }
                try {
                    int v = Integer.parseInt(raw.trim());
                    if (v < 0 || v > 127) {
                        throw new IllegalArgumentException("Aannametype moet tussen 0 en 127 liggen (TINYINT).");
                    }
                    return v;
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Aannametype moet een getal zijn.");
                }
            }

            @Override
            public String hintText() {
                return "bijv. 0";
            }
        });
        Tab t = new Tab("AannameType", pane.getView());
        t.setClosable(false);
        return t;
    }

 
    private Tab tabKunstwerkIds() {
        KunstwerkIdDAO dao = new KunstwerkIdDAO();

        Label title = new Label("Kunstwerk ID's (read-only)");
        Button btnRefresh = new Button("Vernieuwen");
        Label lblInfo = new Label("-");

        ListView<Integer> list = new ListView<>();
        list.setPlaceholder(new Label("Geen kunstwerk_id's gevonden."));
        list.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.valueOf(item));
            }
        });

        Runnable refresh = () -> {
            try {
                List<Integer> ids = dao.listAll();
                list.getItems().setAll(ids);
                lblInfo.setText("Aantal: " + ids.size());
            } catch (Exception ex) {
                lblInfo.setText("Fout bij laden");
                Alert a = new Alert(Alert.AlertType.ERROR,
                        ex.getMessage() == null ? "Onbekende fout." : ex.getMessage(),
                        ButtonType.OK);
                a.setHeaderText(null);
                a.showAndWait();
            }
        };

        btnRefresh.setOnAction(e -> refresh.run());

        HBox top = new HBox(12, title, btnRefresh, lblInfo);
        top.setPadding(new Insets(12));
        HBox.setHgrow(lblInfo, Priority.ALWAYS);

        VBox pane = new VBox(10, top, list);
        pane.setPadding(new Insets(0, 12, 12, 12));
        VBox.setVgrow(list, Priority.ALWAYS);

        // initial load
        refresh.run();

        Tab t = new Tab("KunstwerkIDs", pane);
        t.setClosable(false);
        return t;
    }
}
