package com.mycompany.aas.model;

import java.time.LocalDateTime;

public class Aanname {

    private final String email;
    private final int kunstwerkId;
    private final LocalDateTime startdatum;
    private final LocalDateTime einddatum; 
    private final Integer status;          
    private final int aannametype;        

    public Aanname(String email, int kunstwerkId, LocalDateTime startdatum,
            LocalDateTime einddatum, Integer status, int aannametype) {
        this.email = email;
        this.kunstwerkId = kunstwerkId;
        this.startdatum = startdatum;
        this.einddatum = einddatum;
        this.status = status;
        this.aannametype = aannametype;
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
    
    public LocalDateTime setStartdatum() {
        return startdatum;
    }

    public LocalDateTime getEinddatum() {
        return einddatum;
    }
    
    public LocalDateTime setEinndatum() {
        return einddatum;
    }

    public Integer getStatus() {
        return status;
    }
    
    public Integer setStatus() {
        return status;
    }
    

    public int getAannametype() {
        return aannametype;
    }
    
    public int setAannametype() {
        return aannametype;
    }

    @Override
    public String toString() {
        return "Kunstwerk " + kunstwerkId + " | start: " + startdatum + " | type: " + aannametype;
    }
}
