package com.mycompany.aas.model;

import java.util.List;

public class Kunstwerk {

    private int id;
    private String titel;

    private String kunstenaarVoornaam;
    private String kunstenaarAchternaam;

    private String wijk;
    private String adres;

    private Double lat;
    private Double lon;

    private String omschrijving;
    private List<String> materialen;

    // foto (wordt niet meer gebruikt)
    private String fotoId;
    private String fotoFilename;
    private String fotoUrl;

   
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getKunstenaarVoornaam() {
        return kunstenaarVoornaam;
    }

    public void setKunstenaarVoornaam(String v) {
        this.kunstenaarVoornaam = v;
    }

    public String getKunstenaarAchternaam() {
        return kunstenaarAchternaam;
    }

    public void setKunstenaarAchternaam(String a) {
        this.kunstenaarAchternaam = a;
    }

    public String getWijk() {
        return wijk;
    }

    public void setWijk(String wijk) {
        this.wijk = wijk;
    }

    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }

    public List<String> getMaterialen() {
        return materialen;
    }

    public void setMaterialen(List<String> materialen) {
        this.materialen = materialen;
    }

    public String getFotoId() {
        return fotoId;
    }

    public void setFotoId(String fotoId) {
        this.fotoId = fotoId;
    }

    public String getFotoFilename() {
        return fotoFilename;
    }

    public void setFotoFilename(String fotoFilename) {
        this.fotoFilename = fotoFilename;
    }

    // helpers 
    public String getKunstenaarVolledigeNaam() {
        String v = kunstenaarVoornaam == null ? "" : kunstenaarVoornaam.trim();
        String a = kunstenaarAchternaam == null ? "" : kunstenaarAchternaam.trim();
        String full = (v + " " + a).trim();
        return full.isEmpty() ? null : full;
    }
    
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
