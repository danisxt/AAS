package com.mycompany.aas.ui;

import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {

    private final TabPane root = new TabPane();

    public MainController() {

        // Registratie 
        Tab tabRegistratie = new Tab("Registratie");
        tabRegistratie.setClosable(false);
        tabRegistratie.setContent(new RegistratieView().build());

        // Adoptie 
        AdoptieController adoptie = new AdoptieController();
        Tab tabAdoptie = new Tab("Adoptie", adoptie.getView());
        tabAdoptie.setClosable(false);

        // Rapport 
        Tab tabRapport = new Tab("Rapport");
        tabRapport.setClosable(false);
        tabRapport.setContent(new RapportTabView().build());

        // Status (medewerker)
        Tab tabMedewerker = new Tab("Medewerker", new MedewerkerController().getView());
        tabMedewerker.setClosable(false);;

        //Beheer
        Tab tabBeheer = new Tab("Beheer", new com.mycompany.aas.ui.admin.BeheerController().getView());
        tabBeheer.setClosable(false);

        root.getTabs().addAll(tabRegistratie, tabAdoptie, tabRapport, tabMedewerker, tabBeheer);

    }

    public Parent getView() {
        return root;
    }
}
