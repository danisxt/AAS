package com.mycompany.aas;

import com.mycompany.aas.ui.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new MainController().getView(), 1100, 750);
        stage.setTitle("AAS - Kunst Adoptie Systeem");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
