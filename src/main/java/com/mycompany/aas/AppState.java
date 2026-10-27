package com.mycompany.aas;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
//"inlog onthouder"
public final class AppState {

    private static final AppState INSTANCE = new AppState();

    private final StringProperty currentEmail = new SimpleStringProperty("");

    private AppState() {
    }

    public static AppState getInstance() {
        return INSTANCE;
    }

    public StringProperty currentEmailProperty() {
        return currentEmail;
    }

    public String getCurrentEmail() {
        return currentEmail.get();
    }

    public void setCurrentEmail(String email) {
        currentEmail.set(email == null ? "" : email.trim());
    }
}
