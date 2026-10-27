package com.mycompany.aas.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.aas.model.Kunstwerk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KunstwerkenJsonRepository implements KunstwerkenRepository {

    private final ObjectMapper mapper = new ObjectMapper();
    private List<Kunstwerk> cache; 

    @Override
    public List<Kunstwerk> search(String query, int limit, int offset) throws Exception {
        ensureLoaded();

        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        List<Kunstwerk> filtered = new ArrayList<>();
        for (Kunstwerk k : cache) {
            if (q.isEmpty() || matches(k, q)) {
                filtered.add(k);
            }
        }

        int from = Math.max(0, offset);
        if (from >= filtered.size()) {
            return List.of();
        }

        int to = Math.min(filtered.size(), from + Math.max(1, limit));
        return filtered.subList(from, to);
    }

    private boolean matches(Kunstwerk k, String q) {
        return contains(k.getTitel(), q)
                || contains(k.getKunstenaarVolledigeNaam(), q)
                || contains(k.getWijk(), q)
                || contains(k.getAdres(), q)
                || contains(k.getOmschrijving(), q);
    }

    private boolean contains(String field, String q) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(q);
    }

    private void ensureLoaded() throws Exception {
        if (cache != null) {
            return;
        }

        try (InputStream is = getClass().getResourceAsStream("/kunstwerken.json")) {
            if (is == null) {
                throw new IllegalStateException("kunstwerken.json niet gevonden in src/main/resources/");
            }

            JsonNode root = mapper.readTree(is);

            
            JsonNode recordsNode;
            if (root.isArray()) {
                recordsNode = root;
            } else {
                recordsNode = root.path("records");
            }

            if (recordsNode == null || !recordsNode.isArray()) {
                throw new IllegalStateException("Verwacht JSON array of root.records array in kunstwerken.json");
            }

            List<Kunstwerk> result = new ArrayList<>();
            for (JsonNode record : recordsNode) {
                
                result.add(KunstwerkMapper.fromRecord(record));
            }

            cache = result;
        }
    }
}
