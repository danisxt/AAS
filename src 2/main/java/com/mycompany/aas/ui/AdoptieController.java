package com.mycompany.aas.ui;

import com.mycompany.aas.AppState;
import com.mycompany.aas.api.KunstwerkenLiveRepository;
import com.mycompany.aas.api.KunstwerkenRepository;
import com.mycompany.aas.dao.AannameDAO;
import com.mycompany.aas.dao.RolDAO;
import com.mycompany.aas.model.Aanname;
import com.mycompany.aas.model.Kunstwerk;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AdoptieController {

    private static final int SEARCH_LIMIT = 50;

    private final KunstwerkenRepository repo = new KunstwerkenLiveRepository();
    private final AannameDAO aannameDAO = new AannameDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final KunstwerkenMapView mapView = new KunstwerkenMapView();

    private final BorderPane root = new BorderPane();

    private final TextField tfZoek = new TextField();
    private final Button btnZoek = new Button("Zoek");
    private final Label lblStatus = new Label();

    private final TextField tfEmail = new TextField();
    private final Label lblGebruikerStatus = new Label("");

    // Start/Eind voor aanmaken
    private final DatePicker dpStart = new DatePicker();
    private final TextField tfStartTime = new TextField(); // HH:mm
    private final DatePicker dpEind = new DatePicker();    // optional
    private final TextField tfEindTime = new TextField();  // HH:mm

    // AannameType kiezen 
    private final ComboBox<AannameTypeItem> cbAannameType = new ComboBox<>();

    private final Button btnAdopteer = new Button("Adoptie aanmaken");
    private final ListView<Kunstwerk> list = new ListView<Kunstwerk>();

    // Kunstwerk details
    private final Label lblTitel = new Label("-");
    private final Label lblKunstenaar = new Label("-");
    private final Label lblLocatie = new Label("-");
    private final Label lblMaterialen = new Label("-");
    private final TextArea taOmschrijving = new TextArea();

    // Adoptie details (db)
    private final Label lblAannameStart = new Label("-");
    private final Label lblAannameEind = new Label("-");
    private final Label lblAannameStatus = new Label("-");
    private final Label lblAannameType = new Label("-");

    // Overzicht "Mijn aannames"
    private final TableView<Aanname> tblAannames = new TableView<>();
    private final Label lblAannamesInfo = new Label("Mijn adopties: -");

    // Einddatum wijzigen (op bestaande aanname)
    private final DatePicker dpWijzigEind = new DatePicker();     // leeg = NULL
    private final TextField tfWijzigEindTime = new TextField();   // HH:mm
    private final Button btnSaveEind = new Button("Opslaan einddatum");
    private final Button btnBeëindigNu = new Button("Beëindig (nu)");
    private final Button btnAnnuleer = new Button("Annuleer (alleen zonder rapporten)");

    // Huidige gekozen aanname 
    private Aanname currentAanname = null;

    private SplitPane centerSplit;
    private SplitPane rightSplit;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("H:mm");

    public AdoptieController() {
        buildUI();
        wireEvents();

        mapView.setOnMarkerSelected(this::selectById);

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    mapView.initMap();
                    mapView.redraw();
                    doSearch("");

                    String current = AppState.getInstance().getCurrentEmail();
                    if (current != null && !current.isBlank()) {
                        tfEmail.setText(current);
                    }

                    initDefaults();
                    validateUserAndUpdateUI();
                    refreshAannameInfo();
                    loadMijnAannames(); 
                });
            }
        });
    }

    public Parent getView() {
        return root;
    }

    private void initDefaults() {
        LocalDateTime now = LocalDateTime.now();
        dpStart.setValue(now.toLocalDate());
        tfStartTime.setText(now.toLocalTime().withSecond(0).withNano(0).format(timeFmt));
        dpEind.setValue(null);
        tfEindTime.setText("12:00");

        dpWijzigEind.setValue(null);
        tfWijzigEindTime.setText("12:00");

      
        cbAannameType.getItems().setAll(
                new AannameTypeItem(0, "Adoptie"),
                new AannameTypeItem(1, "Onderhoud"),
                new AannameTypeItem(2, "Sponsoring")
        );
        cbAannameType.getSelectionModel().selectFirst();
    }

    private void buildUI() {
        // TOP 
        tfZoek.setPromptText("Zoek op titel, kunstenaar, wijk of adres...");
        HBox top = new HBox(10, tfZoek, btnZoek);
        top.setPadding(new Insets(12));
        HBox.setHgrow(tfZoek, Priority.ALWAYS);

        VBox topWrap = new VBox(6, top, lblStatus);
        root.setTop(topWrap);

        // LINKS 
        Label lblEmail = new Label("Email gebruiker:");
        tfEmail.setPromptText("email@aas.nl");
        lblGebruikerStatus.setWrapText(true);

        // aanmaken velden
        Label lblType = new Label("Soort aanmelding:");
        cbAannameType.setPrefWidth(220);

        Label lblStart = new Label("Startdatum:");
        dpStart.setPromptText("yyyy-mm-dd");
        tfStartTime.setPromptText("HH:mm");
        HBox startRow = new HBox(8, dpStart, tfStartTime);
        HBox.setHgrow(dpStart, Priority.ALWAYS);

        Label lblEind = new Label("Einddatum (optioneel):");
        dpEind.setPromptText("yyyy-mm-dd");
        tfEindTime.setPromptText("HH:mm");
        HBox eindRow = new HBox(8, dpEind, tfEindTime);
        HBox.setHgrow(dpEind, Priority.ALWAYS);

        Label lblHint = new Label("Tijdformaat: UUR:MINUUT (bijv. 09:30). Laat einddatum leeg voor 'onbepaald'.");
        lblHint.setWrapText(true);

        btnAdopteer.setMaxWidth(Double.MAX_VALUE);

        // overzicht tabel 
        Label lblOverzicht = new Label("Mijn Adopties:");
        lblOverzicht.setStyle("-fx-font-weight: bold;");
        lblAannamesInfo.setWrapText(true);

        configureAannamesTable();

        // einddatum wijzigen sectie
        Label lblWijzig = new Label("Einddatum wijzigen (geselecteerde adoptie):");
        lblWijzig.setStyle("-fx-font-weight: bold;");

        dpWijzigEind.setPromptText("yyyy-mm-dd (leeg = NULL)");
        tfWijzigEindTime.setPromptText("HH:mm");
        HBox wijzigRow = new HBox(8, dpWijzigEind, tfWijzigEindTime);
        HBox.setHgrow(dpWijzigEind, Priority.ALWAYS);

        btnSaveEind.setMaxWidth(Double.MAX_VALUE);
        btnBeëindigNu.setMaxWidth(Double.MAX_VALUE);
        btnAnnuleer.setMaxWidth(Double.MAX_VALUE);

        // linker bovenpaneel scrollbaar
        VBox leftTopContent = new VBox(
                8,
                lblEmail, tfEmail, lblGebruikerStatus,
                new Separator(),
                new Label("Nieuwe adoptie:"),
                labeledNode("Soort aanmelding:", cbAannameType),
                lblStart, startRow,
                lblEind, eindRow,
                lblHint,
                btnAdopteer,
                new Separator(),
                lblOverzicht,
                lblAannamesInfo,
                tblAannames,
                new Separator(),
                lblWijzig,
                wijzigRow,
                btnSaveEind,
                btnBeëindigNu,
                btnAnnuleer
        );
        leftTopContent.setPadding(new Insets(12));

        ScrollPane leftScroll = new ScrollPane(leftTopContent);
        leftScroll.setFitToWidth(true);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // lijst kunstwerken
        list.setPrefWidth(350);
        VBox left = new VBox(leftScroll, list);
        VBox.setVgrow(list, Priority.ALWAYS);

        list.setCellFactory(lv -> new ListCell<Kunstwerk>() {
            @Override
            protected void updateItem(Kunstwerk item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String t = nvl(item.getTitel(), "(geen titel)");
                String w = item.getWijk() == null ? "" : (" • " + item.getWijk());
                setText(t + w);
            }
        });

        // RECHTs kaart + details 
        StackPane mapBox = new StackPane(mapView.getView());
        mapBox.setMinSize(0, 0);
        mapBox.setPrefHeight(400);

        mapView.getWebView().setMinSize(0, 0);
        mapView.getWebView().setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        lblTitel.setFont(Font.font(18));
        lblTitel.setWrapText(true);

        taOmschrijving.setEditable(false);
        taOmschrijving.setWrapText(true);

        VBox detail = new VBox(8,
                lblTitel,
                new Separator(),
                labeled("Kunstenaar:", lblKunstenaar),
                labeled("Locatie:", lblLocatie),
                labeled("Materialen:", lblMaterialen),
                new Separator(),
                new Label("Adoptiegegevens (database):"),
                labeled("Start:", lblAannameStart),
                labeled("Eind:", lblAannameEind),
                labeled("Status:", lblAannameStatus),
                labeled("Type:", lblAannameType),
                new Separator(),
                new Label("Omschrijving:"),
                taOmschrijving
        );
        detail.setPadding(new Insets(12));
        VBox.setVgrow(taOmschrijving, Priority.ALWAYS);

        rightSplit = new SplitPane(mapBox, detail);
        rightSplit.setOrientation(Orientation.VERTICAL);
        rightSplit.setDividerPositions(0.6);

        centerSplit = new SplitPane(left, rightSplit);
        centerSplit.setDividerPositions(0.38);

        root.setCenter(centerSplit);
        BorderPane.setMargin(centerSplit, new Insets(0, 12, 12, 12));

        javafx.animation.PauseTransition redrawDebounce
                = new javafx.animation.PauseTransition(javafx.util.Duration.millis(60));
        redrawDebounce.setOnFinished(e -> mapView.redraw());
        mapBox.widthProperty().addListener((o, ov, nv) -> redrawDebounce.playFromStart());
        mapBox.heightProperty().addListener((o, ov, nv) -> redrawDebounce.playFromStart());
    }

    private void configureAannamesTable() {
        tblAannames.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblAannames.setPrefHeight(220);

        TableColumn<Aanname, Integer> colKunstwerk = new TableColumn<>("kunstwerk_id");
        colKunstwerk.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getKunstwerkId()));

        TableColumn<Aanname, LocalDateTime> colStart = new TableColumn<>("start");
        colStart.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getStartdatum()));

        TableColumn<Aanname, LocalDateTime> colEind = new TableColumn<>("eind");
        colEind.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cd.getValue().getEinddatum()));

        TableColumn<Aanname, String> colStatus = new TableColumn<>("status");
        colStatus.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(statusToText(cd.getValue().getStatus())));

        TableColumn<Aanname, String> colType = new TableColumn<>("type");
        colType.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(typeToText(cd.getValue().getAannametype())));

        tblAannames.getColumns().setAll(colKunstwerk, colStart, colEind, colStatus, colType);

        // klik op aanname > selecteer kunstwerk in lijst + update details + zet currentAanname
        tblAannames.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            currentAanname = sel;
            validateActionButtons();

            if (sel != null) {
                
                if (sel.getEinddatum() == null) {
                    dpWijzigEind.setValue(null);
                } else {
                    dpWijzigEind.setValue(sel.getEinddatum().toLocalDate());
                    tfWijzigEindTime.setText(sel.getEinddatum().toLocalTime().withSecond(0).withNano(0).format(timeFmt));
                }

                selectKunstwerkInList(sel.getKunstwerkId());
                refreshAannameInfo(); 
            }
        });

        // dubbelklik= scroll lijst kunstwerken 
        tblAannames.setRowFactory(tv -> {
            TableRow<Aanname> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    Aanname a = row.getItem();
                    selectKunstwerkInList(a.getKunstwerkId());
                }
            });
            return row;
        });
    }

    private HBox labeled(String title, Label value) {
        Label l = new Label(title);
        l.setMinWidth(90);
        value.setWrapText(true);
        HBox box = new HBox(8, l, value);
        HBox.setHgrow(value, Priority.ALWAYS);
        return box;
    }

    private HBox labeledNode(String title, Control node) {
        Label l = new Label(title);
        l.setMinWidth(110);
        HBox box = new HBox(8, l, node);
        HBox.setHgrow(node, Priority.ALWAYS);
        return box;
    }

    private void wireEvents() {
        btnZoek.setOnAction(e -> doSearch(tfZoek.getText()));
        tfZoek.setOnAction(e -> doSearch(tfZoek.getText()));

        list.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            showDetails(nw);
            mapView.redraw();

            // Als user een kunstwerk selecteert, laten we currentAanname staan als die bij dit kunstwerk hoort;
            // anders fallback naar latest aanname voor dit kunstwerk.
            refreshAannameInfo();

            validateUserAndUpdateUI();
        });

        tfEmail.textProperty().addListener((obs, old, nw) -> {
            AppState.getInstance().setCurrentEmail(nw);
            validateUserAndUpdateUI();
            refreshAannameInfo();
            loadMijnAannames();
        });

        // validatie bij input wijziging 
        dpStart.valueProperty().addListener((o, ov, nv) -> validateUserAndUpdateUI());
        tfStartTime.textProperty().addListener((o, ov, nv) -> validateUserAndUpdateUI());
        dpEind.valueProperty().addListener((o, ov, nv) -> validateUserAndUpdateUI());
        tfEindTime.textProperty().addListener((o, ov, nv) -> validateUserAndUpdateUI());
        cbAannameType.valueProperty().addListener((o, ov, nv) -> validateUserAndUpdateUI());

        // einddatum wijzigen controls
        dpWijzigEind.valueProperty().addListener((o, ov, nv) -> validateActionButtons());
        tfWijzigEindTime.textProperty().addListener((o, ov, nv) -> validateActionButtons());

        btnAdopteer.setOnAction(e -> createAanname());
        btnSaveEind.setOnAction(e -> saveEinddatumWijziging());
        btnBeëindigNu.setOnAction(e -> beeindigNu());
        btnAnnuleer.setOnAction(e -> annuleerAanname());

        validateUserAndUpdateUI();
        validateActionButtons();
    }

    private void validateUserAndUpdateUI() {
        boolean hasSelection = list.getSelectionModel().getSelectedItem() != null;

        String email = tfEmail.getText();
        if (email == null || email.trim().isEmpty()) {
            lblGebruikerStatus.setText("Vul een email in.");
            btnAdopteer.setDisable(true);
            return;
        }
        String clean = email.trim();

        boolean isVrijwilliger;
        boolean isMedewerker;
        try {
            isVrijwilliger = rolDAO.isVrijwilliger(clean);
            isMedewerker = rolDAO.isMedewerker(clean);
        } catch (SQLException ex) {
            lblGebruikerStatus.setText("Database fout bij gebruikerscheck: " + safeMsg(ex.getMessage()));
            btnAdopteer.setDisable(true);
            return;
        }

        if (!isVrijwilliger && !isMedewerker) {
            lblGebruikerStatus.setText("Onbekende gebruiker/rol. Registreer eerst.");
            btnAdopteer.setDisable(true);
            return;
        }

        // vrijwilliger OF medewerker mag aanmaken > toestaan voor beide
        lblGebruikerStatus.setText(isVrijwilliger ? "Actief als Vrijwilliger: " + clean : "Actief als Medewerker: " + clean);

        // start/eind validatie
        LocalDateTime start;
        LocalDateTime eind;
        try {
            start = readStartDateTime();
        } catch (IllegalArgumentException ex) {
            lblGebruikerStatus.setText("Startdatum ongeldig: " + ex.getMessage());
            btnAdopteer.setDisable(true);
            return;
        }

        try {
            eind = readEindDateTimeOrNull();
        } catch (IllegalArgumentException ex) {
            lblGebruikerStatus.setText("Einddatum ongeldig: " + ex.getMessage());
            btnAdopteer.setDisable(true);
            return;
        }

        if (eind != null && eind.isBefore(start)) {
            lblGebruikerStatus.setText("Einddatum mag niet vóór startdatum liggen.");
            btnAdopteer.setDisable(true);
            return;
        }

        // type is verplicht
        if (cbAannameType.getValue() == null) {
            btnAdopteer.setDisable(true);
            return;
        }

        btnAdopteer.setDisable(!hasSelection);
    }

    private void validateActionButtons() {
        boolean hasAanname = currentAanname != null;
        btnSaveEind.setDisable(!hasAanname);
        btnBeëindigNu.setDisable(!hasAanname);
        btnAnnuleer.setDisable(!hasAanname);
    }

    private void doSearch(String query) {
        lblStatus.setText("Laden...");

        Task<List<Kunstwerk>> task = new Task<List<Kunstwerk>>() {
            @Override
            protected List<Kunstwerk> call() throws Exception {
                return repo.search(query, SEARCH_LIMIT, 0);
            }
        };

        task.setOnSucceeded(e -> {
            List<Kunstwerk> results = task.getValue();
            list.getItems().setAll(results);
            lblStatus.setText("Resultaten: " + results.size());

            mapView.setMarkers(results);
            mapView.redraw();

            if (!results.isEmpty()) {
                list.getSelectionModel().select(0);
            } else {
                showDetails(null);
            }

            refreshAannameInfo();
            validateUserAndUpdateUI();
        });

        task.setOnFailed(e -> {
            lblStatus.setText("Fout bij laden");
            Throwable ex = task.getException();
            ex.printStackTrace();
            showError(safeMsg(ex.getMessage()));
        });

        new Thread(task, "kunstwerken-search").start();
    }

    private void selectById(int id) {
        Platform.runLater(() -> {
            Kunstwerk current = list.getSelectionModel().getSelectedItem();
            if (current != null && current.getId() == id) {
                return;
            }

            for (Kunstwerk k : list.getItems()) {
                if (k.getId() == id) {
                    list.getSelectionModel().select(k);
                    list.scrollTo(k);
                    break;
                }
            }
        });
    }

    private void selectKunstwerkInList(int kunstwerkId) {
        for (Kunstwerk k : list.getItems()) {
            if (k.getId() == kunstwerkId) {
                list.getSelectionModel().select(k);
                list.scrollTo(k);
                break;
            }
        }
    }

    private void showDetails(Kunstwerk k) {
        if (k == null) {
            lblTitel.setText("-");
            lblKunstenaar.setText("-");
            lblLocatie.setText("-");
            lblMaterialen.setText("-");
            taOmschrijving.setText("");

            lblAannameStart.setText("-");
            lblAannameEind.setText("-");
            lblAannameStatus.setText("-");
            lblAannameType.setText("-");
            return;
        }

        lblTitel.setText(nvl(k.getTitel(), "(geen titel)"));
        lblKunstenaar.setText(nvl(k.getKunstenaarVolledigeNaam(), "(onbekend)"));

        String locatie = nvl(k.getWijk(), "");
        if (k.getAdres() != null && !k.getAdres().isBlank()) {
            locatie = (locatie.isBlank() ? "" : (locatie + " – ")) + k.getAdres();
        }
        lblLocatie.setText(locatie.isBlank() ? "-" : locatie);

        lblMaterialen.setText(
                (k.getMaterialen() == null || k.getMaterialen().isEmpty())
                ? "(onbekend)"
                : String.join(", ", k.getMaterialen())
        );

        taOmschrijving.setText(nvl(k.getOmschrijving(), ""));
    }

    //laadt 'mijn aannames' in de tabel op basis van email
    private void loadMijnAannames() {
        String email = tfEmail.getText();
        if (email == null || email.trim().isEmpty()) {
            tblAannames.getItems().clear();
            lblAannamesInfo.setText("Mijn adopties: -");
            return;
        }

        String clean = email.trim();

        Task<List<Aanname>> task = new Task<List<Aanname>>() {
            @Override
            protected List<Aanname> call() throws Exception {
                return aannameDAO.listAannamesByEmail(clean);
            }
        };

        task.setOnSucceeded(e -> {
            List<Aanname> rows = task.getValue();
            tblAannames.getItems().setAll(rows);
            lblAannamesInfo.setText("Mijn adopties: " + rows.size());

            // als currentAanname niet meer bestaat, reset
            if (currentAanname != null) {
                boolean stillThere = false;
                for (Aanname a : rows) {
                    if (samePk(a, currentAanname)) {
                        stillThere = true;
                        break;
                    }
                }
                if (!stillThere) {
                    currentAanname = null;
                }
            }

            validateActionButtons();
        });

        task.setOnFailed(e -> {
            tblAannames.getItems().clear();
            lblAannamesInfo.setText("Mijn adopties: fout bij laden");
            Throwable ex = task.getException();
            ex.printStackTrace();
        });

        new Thread(task, "aannames-load").start();
    }

    private boolean samePk(Aanname a, Aanname b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getEmail().equals(b.getEmail())
                && a.getKunstwerkId() == b.getKunstwerkId()
                && a.getStartdatum().equals(b.getStartdatum());
    }

    //zet de aanname-details rechts (start/eind/status/type). Prioriteit: 1
    //als tabelselectie currentAanname heeft en kunstwerk matcht > toon die 2
    //anders: toon latest aanname voor (email + geselecteerd kunstwerk)
     
    private void refreshAannameInfo() {
        Kunstwerk k = list.getSelectionModel().getSelectedItem();
        String email = tfEmail.getText();

        if (k == null || email == null || email.trim().isEmpty()) {
            lblAannameStart.setText("-");
            lblAannameEind.setText("-");
            lblAannameStatus.setText("-");
            lblAannameType.setText("-");
            return;
        }

        String cleanEmail = email.trim();

        // als currentAanname bij dit kunstwerk hoort en email klopt, toon direct
        if (currentAanname != null
                && currentAanname.getEmail().equals(cleanEmail)
                && currentAanname.getKunstwerkId() == k.getId()) {

            setAannameLabels(currentAanname);
            return;
        }

        try {
            Aanname a = aannameDAO.findLatestAanname(cleanEmail, k.getId());
            
            if (a == null) {
                lblAannameStart.setText("(geen)");
                lblAannameEind.setText("-");
                lblAannameStatus.setText("-");
                lblAannameType.setText("-");
                return;
            }
            setAannameLabels(a);
        } catch (SQLException ex) {
            lblAannameStart.setText("?");
            lblAannameEind.setText("?");
            lblAannameStatus.setText("DB fout");
            lblAannameType.setText("?");
        }
    }

    private void setAannameLabels(Aanname a) {
        lblAannameStart.setText(formatDt(a.getStartdatum()));
        lblAannameEind.setText(a.getEinddatum() == null ? "-" : formatDt(a.getEinddatum()));
        lblAannameStatus.setText(statusToText(a.getStatus()));
        lblAannameType.setText(typeToText(a.getAannametype()));
    }

    private void createAanname() {
        Kunstwerk k = list.getSelectionModel().getSelectedItem();
        if (k == null) {
            return;
        }

        String email = tfEmail.getText();
        if (email == null || email.isBlank()) {
            showError("Email is verplicht");
            return;
        }

        String clean = email.trim();

        try {
            // mag vrijwilliger of medewerker 
            if (!rolDAO.isVrijwilliger(clean) && !rolDAO.isMedewerker(clean)) {
                showError("Alleen een Vrijwilliger of Medewerker kan een aanname aanmaken.");
                return;
            }

            LocalDateTime start = readStartDateTime();
            LocalDateTime eind = readEindDateTimeOrNull();
            if (eind != null && eind.isBefore(start)) {
                showError("Einddatum mag niet vóór startdatum liggen.");
                return;
            }

            AannameTypeItem typeItem = cbAannameType.getValue();
            if (typeItem == null) {
                showError("Kies de soort aanmelding.");
                return;
            }

            aannameDAO.createAanname(
                    clean,
                    k.getId(),
                    start,
                    eind,
                    0, 
                    typeItem.id 
            );

            showInfo("Adoptie aangemaakt voor " + clean);

            // refresh overzicht + details
            loadMijnAannames();
            refreshAannameInfo();

        } catch (Exception ex) {
            showError(safeMsg(ex.getMessage()));
        }
    }

    private void saveEinddatumWijziging() {
        if (currentAanname == null) {
            showError("Selecteer eerst een adoptie in het overzicht.");
            return;
        }

        try {
            LocalDateTime newEind = readWijzigEindDateTimeOrNull(); // null = einddatum verwijderen

            LocalDateTime start = currentAanname.getStartdatum();
            if (newEind != null && newEind.isBefore(start)) {
                showError("Einddatum mag niet vóór startdatum liggen.");
                return;
            }

            aannameDAO.setEinddatum(
                    currentAanname.getEmail(),
                    currentAanname.getKunstwerkId(),
                    currentAanname.getStartdatum(),
                    newEind
            );

            showInfo("Einddatum bijgewerkt.");
            loadMijnAannames();
            refreshAannameInfo();

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError(safeMsg(ex.getMessage()));
        }
    }

    private void beeindigNu() {
        if (currentAanname == null) {
            showError("Selecteer eerst een adoptie in het overzicht.");
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(currentAanname.getStartdatum())) {
                showError("Kan niet beëindigen: 'nu' ligt vóór de startdatum.");
                return;
            }

            aannameDAO.setEinddatum(
                    currentAanname.getEmail(),
                    currentAanname.getKunstwerkId(),
                    currentAanname.getStartdatum(),
                    now
            );

            showInfo("Beëindigd (einddatum = nu).");
            loadMijnAannames();
            refreshAannameInfo();

        } catch (SQLException ex) {
            showError(safeMsg(ex.getMessage()));
        }
    }

    private void annuleerAanname() {
        if (currentAanname == null) {
            showError("Selecteer eerst een adoptie in het overzicht.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Weet je zeker dat je deze adoptie wilt annuleren?\n\n"
                + "Dit verwijdert de adoptie definitief.\n"
                + "Dit kan ALLEEN als er nog geen rapporten bestaan.",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setHeaderText(null);

        ButtonType res = confirm.showAndWait().orElse(ButtonType.NO);
        if (res != ButtonType.YES) {
            return;
        }

        try {
            aannameDAO.deleteAannameIfNoRapporten(
                    currentAanname.getEmail(),
                    currentAanname.getKunstwerkId(),
                    currentAanname.getStartdatum()
            );

            showInfo("Adoptie geannuleerd.");
            currentAanname = null;
            validateActionButtons();

            loadMijnAannames();
            refreshAannameInfo();

        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError(safeMsg(ex.getMessage()));
        }
    }

    private LocalDateTime readStartDateTime() {
        LocalDate d = dpStart.getValue();
        if (d == null) {
            throw new IllegalArgumentException("Kies een startdatum.");
        }
        LocalTime t = parseTime(tfStartTime.getText(), "starttijd");
        return LocalDateTime.of(d, t);
    }

    private LocalDateTime readEindDateTimeOrNull() {
        LocalDate d = dpEind.getValue();
        if (d == null) {
            return null;
        }
        LocalTime t = parseTime(tfEindTime.getText(), "eindtijd");
        return LocalDateTime.of(d, t);
    }

    private LocalDateTime readWijzigEindDateTimeOrNull() {
        LocalDate d = dpWijzigEind.getValue();
        if (d == null) {
            return null;
        }
        LocalTime t = parseTime(tfWijzigEindTime.getText(), "eindtijd");
        return LocalDateTime.of(d, t);
    }

    private LocalTime parseTime(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Vul " + fieldName + " in (HH:mm).");
        }
        String s = raw.trim();
        try {
            return LocalTime.parse(s, timeFmt);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Gebruik formaat HH:mm (bijv. 09:30).");
        }
    }

    private String formatDt(LocalDateTime dt) {
        if (dt == null) {
            return "-";
        }
        return dt.format(dtf);
    }

    private String statusToText(Integer status) {
        if (status == null) {
            return "-";
        }
        if (status.intValue() == 0) {
            return "0 - in behandeling";
        }
        if (status.intValue() == 1) {
            return "1 - goedgekeurd";
        }
        if (status.intValue() == 2) {
            return "2 - afgekeurd";
        }
        return status + " - onbekend";
    }

    private String typeToText(int type) {
        if (type == 0) {
            return "0 - adoptie";
        }
        if (type == 1) {
            return "1 - onderhoud";
        }
        if (type == 2) {
            return "2 - sponsoring";
        }
        return type + " - onbekend";
    }

    private String nvl(String s, String fb) {
        return (s == null || s.isBlank()) ? fb : s;
    }

    private String safeMsg(String msg) {
        return (msg == null || msg.isBlank()) ? "Onbekende fout." : msg;
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // kleine helper class voor ComboBox
    private static final class AannameTypeItem {

        final int id;
        final String label;

        AannameTypeItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
