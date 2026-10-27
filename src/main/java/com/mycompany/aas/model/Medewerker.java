package com.mycompany.aas.model;

public class Medewerker extends Deelnemer {

    public Medewerker(String email, String voornaam, String achternaam, boolean actief) {
        super(email, voornaam, achternaam, actief);
    }

    @Override
    public Rol getRol() {
        return Rol.MEDEWERKER;
    }
}
