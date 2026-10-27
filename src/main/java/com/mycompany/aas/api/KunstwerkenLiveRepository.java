package com.mycompany.aas.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.aas.model.Kunstwerk;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KunstwerkenLiveRepository implements KunstwerkenRepository {

    private static final String BASE
            = "https://data.eindhoven.nl/api/records/1.0/search/?dataset=kunstwerken";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Kunstwerk> search(String query, int limit, int offset) throws Exception {
        String q = query == null ? "" : query.trim();

        String url = BASE
                + "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8)
                + "&rows=" + Math.max(1, limit)
                + "&start=" + Math.max(0, offset);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("API error: HTTP " + resp.statusCode());
        }

        JsonNode root = mapper.readTree(resp.body());
        JsonNode records = root.path("records");
        if (!records.isArray()) {
            throw new IllegalStateException("Onverwacht API formaat: 'records' is geen array");
        }

        List<Kunstwerk> result = new ArrayList<>();
        for (JsonNode r : records) {
            result.add(KunstwerkMapper.fromRecord(r));
        }
        return result;
    }
}
