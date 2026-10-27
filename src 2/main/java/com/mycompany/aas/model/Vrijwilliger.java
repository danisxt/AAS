package com.mycompany.aas.model;

public class Vrijwilliger extends Deelnemer {

    public Vrijwilliger(String email, String voornaam, String achternaam, boolean actief) {
        super(email, voornaam, achternaam, actief);
    }

    @Override
    public Rol getRol() {
        return Rol.VRIJWILLIGER;
    }
}
