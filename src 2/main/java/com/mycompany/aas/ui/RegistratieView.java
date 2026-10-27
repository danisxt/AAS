package com.mycompany.aas.ui;

import com.mycompany.aas.AppState;
import com.mycompany.aas.dao.GebruikerDAO;
import com.mycompany.aas.dao.RolDAO;
import com.mycompany.aas.model.Deelnemer;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class RegistratieView {

    private final GebruikerDAO gebruikerDAO = new GebruikerDAO();
    private final RolDAO rolDAO = new RolDAO();

    public Parent build() {
        Label title = new Label("Registratie");
        title.setFont(Font.font(18));

        TextField tfEmail = new TextField();
        tfEmail.setPromptText("email@aas.nl");

        TextField tfVoornaam = new TextField();
        tfVoornaam.setPromptText("Voornaam");

        TextField tfAchternaam = new TextField();
        tfAchternaam.setPromptText("Achternaam");

        RadioButton rbVrijwilliger = new RadioButton("Vrijwilliger");
        RadioButton rbMedewerker = new RadioButton("Medewerker");
        ToggleGroup tg = new ToggleGroup();
        rbVrijwilliger.setToggleGroup(tg);
        rbMedewerker.setToggleGroup(tg);
        rbVrijwilliger.setSelected(true);

        Button btnMaak = new Button("Maak gebruiker aan");
        Button btnToon = new Button("Toon gegevens");
        Button btnUpdate = new Button("Wijzig naam");
        Button btnDeactivate = new Button("Maak account inactief");

        Label lblRole = new Label("-");
        Label lblStored = new Label("-");
        lblStored.setWrapText(true);

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);

        gp.add(new Label("Email:"), 0, 0);
        gp.add(tfEmail, 1, 0);

        gp.add(new Label("Voornaam:"), 0, 1);
        gp.add(tfVoornaam, 1, 1);

        gp.add(new Label("Achternaam:"), 0, 2);
        gp.add(tfAchternaam, 1, 2);

        gp.add(new Label("Rol (bij aanmaken):"), 0, 3);
        gp.add(new VBox(6, rbVrijwilliger, rbMedewerker), 1, 3);

        HBox buttons = new HBox(10, btnMaak, btnToon, btnUpdate, btnDeactivate);
        gp.add(buttons, 1, 4);

        gp.add(new Label("Rolstatus:"), 0, 5);
        gp.add(lblRole, 1, 5);

        gp.add(new Label("Opgeslagen gegevens:"), 0, 6);
        gp.add(lblStored, 1, 6);

        VBox root = new VBox(12, title, gp);
        root.setPadding(new Insets(14));

        // Prefill vanuit AppState
        String existing = AppState.getInstance().getCurrentEmail();
        if (existing != null && !existing.isBlank()) {
            tfEmail.setText(existing);
        }

        // CREATE
        btnMaak.setOnAction(e -> {
            try {
                String email = requireEmail(tfEmail.getText());
                String voornaam = requireText(tfVoornaam.getText(), "Voornaam is verplicht.");
                String achternaam = requireText(tfAchternaam.getText(), "Achternaam is verplicht.");

                gebruikerDAO.createGebruiker(email, voornaam, achternaam);

               
                if (rbVrijwilliger.isSelected()) {
                    rolDAO.setVrijwilliger(email);
                } else {
                    rolDAO.setMedewerker(email);
                }

                AppState.getInstance().setCurrentEmail(email);

                refreshView(email, tfVoornaam, tfAchternaam, lblRole, lblStored);
                showInfo("Gelukt", "Gebruiker aangemaakt: " + email);
            } catch (Exception ex) {
                showError("Mislukt", ex.getMessage());
            }
        });

        // READ
        btnToon.setOnAction(e -> {
            try {
                String email = requireEmail(tfEmail.getText());
                AppState.getInstance().setCurrentEmail(email);
                refreshView(email, tfVoornaam, tfAchternaam, lblRole, lblStored);
            } catch (Exception ex) {
                showError("Fout", ex.getMessage());
            }
        });

        // UPDATE (naam)
        btnUpdate.setOnAction(e -> {
            try {
                String email = requireEmail(tfEmail.getText());
                String voornaam = requireText(tfVoornaam.getText(), "Voornaam is verplicht.");
                String achternaam = requireText(tfAchternaam.getText(), "Achternaam is verplicht.");

                gebruikerDAO.updateNaam(email, voornaam, achternaam);
                refreshView(email, tfVoornaam, tfAchternaam, lblRole, lblStored);

                showInfo("Bijgewerkt", "Naamgegevens zijn bijgewerkt.");
            } catch (Exception ex) {
                showError("Update mislukt", ex.getMessage());
            }
        });

        // DEACTIVATE
        btnDeactivate.setOnAction(e -> {
            final String email;
            try {
                email = requireEmail(tfEmail.getText());
            } catch (Exception ex) {
                showError("Fout", ex.getMessage());
                return;
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Weet je zeker dat je dit account inactief wilt maken?\n\n"
                    + "De gebruiker blijft bestaan, maar verliest zijn rol (kan geen adopties/rapporten meer beheren).",
                    ButtonType.YES, ButtonType.NO
            );
            confirm.setHeaderText(null);

            ButtonType res = confirm.showAndWait().orElse(ButtonType.NO);
            if (res != ButtonType.YES) {
                return;
            }

            try {
                rolDAO.deactivate(email);

                // toon user inclusief "inactief" (includeInactive=true)
                refreshView(email, tfVoornaam, tfAchternaam, lblRole, lblStored);

                if (email.equalsIgnoreCase(AppState.getInstance().getCurrentEmail())) {
                    AppState.getInstance().setCurrentEmail("");
                }

                showInfo("Inactief", "Account is nu inactief (rol verwijderd).");
            } catch (Exception ex) {
                showError("Deactiveren mislukt", ex.getMessage());
            }
        });

        // Init read als er al email is ingevuld
        if (tfEmail.getText() != null && !tfEmail.getText().trim().isEmpty()) {
            try {
                refreshView(requireEmail(tfEmail.getText()), tfVoornaam, tfAchternaam, lblRole, lblStored);
            } catch (Exception ignored) {
                // stil
            }
        }

        return root;
    }

    private void refreshView(String email,
            TextField tfVoornaam,
            TextField tfAchternaam,
            Label lblRole,
            Label lblStored) throws Exception {

        // includeInactive=true > laat inactieve users nog zien
        Deelnemer d = gebruikerDAO.findByEmail(email, true);

        if (d == null) {
            lblRole.setText("-");
            lblStored.setText("Geen gebruiker gevonden voor: " + email);
            return;
        }

        tfVoornaam.setText(d.getVoornaam());
        tfAchternaam.setText(d.getAchternaam());

        // rolstatus tonen via model 
        String roleText;
        if (!d.isActief()) {
            roleText = "Inactief (geen rol)";
        } else {
            
            Deelnemer.Rol r = d.getRol();
            roleText = (r == Deelnemer.Rol.MEDEWERKER) ? "Medewerker" : "Vrijwilliger";
        }
        lblRole.setText(roleText);

        lblStored.setText(
                "Email: " + d.getEmail()
                + "\nVoornaam: " + d.getVoornaam()
                + "\nAchternaam: " + d.getAchternaam()
                + "\nActief: " + (d.isActief() ? "Ja" : "Nee")
        );
    }

    private String requireEmail(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is verplicht.");
        }
        return s.trim();
    }

    private String requireText(String s, String msgIfEmpty) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(msgIfEmpty);
        }
        return s.trim();
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
