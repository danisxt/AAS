package com.mycompany.aas.model;

import java.time.LocalDateTime;

public class Rapport {

    private final String email;
    private final int kunstwerkId;
    private final LocalDateTime startdatum;
    private final LocalDateTime aangemaaktOp;
    private final String conditie;

    public Rapport(String email, int kunstwerkId, LocalDateTime startdatum, LocalDateTime aangemaaktOp, String conditie) {
        this.email = email;
        this.kunstwerkId = kunstwerkId;
        this.startdatum = startdatum;
        this.aangemaaktOp = aangemaaktOp;
        this.conditie = conditie;
    }

    public String getEmail() {
        return email;
    }

    public int getKunstwerkId() {
        return kunstwerkId;
    }

    public LocalDateTime getStartdatum() {
        return startdatum;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public String getConditie() {
        return conditie;
    }
}
