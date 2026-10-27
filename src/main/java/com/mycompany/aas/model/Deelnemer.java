package com.mycompany.aas.model;

import java.util.Objects;

public abstract class Deelnemer {

    private final String email;
    private String voornaam;
    private String achternaam;
    private boolean actief; // voor "account inactive"

    protected Deelnemer(String email, String voornaam, String achternaam, boolean actief) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is verplicht");
        }
        this.email = email.trim();
        this.voornaam = voornaam;
        this.achternaam = achternaam;
        this.actief = actief;
    }

    public String getEmail() {
        return email;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public void setVoornaam(String voornaam) {
        this.voornaam = voornaam;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public void setAchternaam(String achternaam) {
        this.achternaam = achternaam;
    }

    public boolean isActief() {
        return actief;
    }

    public void setActief(boolean actief) {
        this.actief = actief;
    }

    // dit maakt polymorfisme zichtbaar in code
    public abstract Rol getRol();

    public enum Rol {
        VRIJWILLIGER,
        MEDEWERKER
    }

    @Override
    public String toString() {
        return getRol() + ": " + email + " (" + voornaam + " " + achternaam + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deelnemer)) {
            return false;
        }
        Deelnemer that = (Deelnemer) o;
        return email.equalsIgnoreCase(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email.toLowerCase());
    }
}
