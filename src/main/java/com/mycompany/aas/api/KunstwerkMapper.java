package com.mycompany.aas.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.aas.model.Kunstwerk;

import java.util.ArrayList;
import java.util.List;

public final class KunstwerkMapper {

    private static final String[] MATERIAAL_KEYS = {
        "materiaal_1", "materiaal_2", "materiaal_3", "materiaal_4", "materiaal_5"
    };

    private KunstwerkMapper() {
    }

    public static Kunstwerk fromRecord(JsonNode record) {
        JsonNode f = record.path("fields");

        Kunstwerk k = new Kunstwerk();
        k.setId(f.path("id").asInt());
        k.setTitel(text(f, "titel_kunstwerk"));

        k.setKunstenaarVoornaam(text(f, "voornaam_kunstenaar"));
        k.setKunstenaarAchternaam(text(f, "achternaam_kunstenaar"));

        k.setWijk(text(f, "wijk"));
        k.setAdres(text(f, "adres"));

        k.setLat(number(f, "latitude"));
        k.setLon(number(f, "longitude"));

        k.setOmschrijving(text(f, "omschrijving"));

        List<String> mats = new ArrayList<>();
        for (String key : MATERIAAL_KEYS) {
            String v = text(f, key);
            if (v != null) {
                mats.add(v);
            }
        }
        k.setMaterialen(mats);

        
        return k;
    }

    private static String text(JsonNode n, String key) {
        JsonNode v = n.get(key);
        if (v == null || v.isNull()) {
            return null;
        }

        String s = v.asText(null);
        if (s == null) {
            return null;
        }

        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static Double number(JsonNode n, String key) {
        JsonNode v = n.get(key);
        if (v == null || v.isNull()) {
            return null;
        }

        if (v.isNumber()) {
            return v.asDouble();
        }

        String s = v.asText(null);
        if (s == null) {
            return null;
        }

        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
