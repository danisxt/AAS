package com.mycompany.aas.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.aas.model.Kunstwerk;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Parent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.IntConsumer;

public class KunstwerkenMapView {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final WebView webView = new WebView();
    private final WebEngine engine = webView.getEngine();

    private volatile boolean loaded = false;
    private final Queue<String> pendingScripts = new ArrayDeque<>();

    private IntConsumer onMarkerSelected;

    public KunstwerkenMapView() {
        webView.setContextMenuEnabled(false);

        engine.getLoadWorker().stateProperty().addListener((obs, old, nw) -> {
            if (nw == Worker.State.SUCCEEDED) {
                loaded = true;

                Platform.runLater(() -> {
                    try {
                        JSObject window = (JSObject) engine.executeScript("window");
                        window.setMember("java", new JsBridge());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    flushPending();
                });
            }
        });

        engine.loadContent(html());
    }

    public Parent getView() {
        return webView;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setOnMarkerSelected(IntConsumer handler) {
        this.onMarkerSelected = handler;
    }

    public void initMap() {
        run("window.initMap && window.initMap();");
        redraw();
    }

    public void setMarkers(List<Kunstwerk> kunstwerken) {
        List<Marker> markers = new ArrayList<>();
        for (Kunstwerk k : kunstwerken) {
            if (k.getLat() == null || k.getLon() == null) {
                continue;
            }
            markers.add(new Marker(
                    k.getId(),
                    k.getLat(),
                    k.getLon(),
                    nvl(k.getTitel(), "Onbekend")
            ));
        }

        try {
            String json = JSON.writeValueAsString(markers);
            run("window.setMarkers(" + json + ");");
        } catch (Exception e) {
            e.printStackTrace();
        }

        redraw();
    }

    public void redraw() {
        run("window.redraw && window.redraw();");
    }

    private void flushPending() {
        while (!pendingScripts.isEmpty()) {
            String js = pendingScripts.poll();
            try {
                engine.executeScript(js);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void run(String js) {
        Platform.runLater(() -> {
            if (!loaded) {
                pendingScripts.add(js);
                return;
            }
            try {
                engine.executeScript(js);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String nvl(String s, String fb) {
        return (s == null || s.isBlank()) ? fb : s;
    }

    public class JsBridge {

        public void selectKunstwerk(int id) {
            if (onMarkerSelected != null) {
                onMarkerSelected.accept(id);
            }
        }
    }

    private static final class Marker {

        public final int id;
        public final double lat;
        public final double lon;
        public final String title;

        private Marker(int id, double lat, double lon, String title) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.title = title;
        }
    }

    private String html() {
        return "<!doctype html>\n"
                + "<html>\n"
                + "<head>\n"
                + "  <meta charset=\"utf-8\"/>\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n"
                + "  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.8.0/dist/leaflet.css\"/>\n"
                + "  <script src=\"https://unpkg.com/leaflet@1.8.0/dist/leaflet.js\"></script>\n"
                + "  <style>\n"
                + "    html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }\n"
                + "    #map { width: 100%; height: 100%; background: #ffffff; }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id=\"map\"></div>\n"
                + "\n"
                + "  <script>\n"
                + "    let map = null;\n"
                + "    let tiles = null;\n"
                + "    let layer = null;\n"
                + "    let pendingItems = null;\n"
                + "\n"
                + "    const eindhovenBounds = L.latLngBounds([51.4000, 5.4000], [51.5000, 5.5200]);\n"
                + "\n"
                + "    window.initMap = function () {\n"
                + "      if (map) return;\n"
                + "      map = L.map('map', { maxBounds: eindhovenBounds, maxBoundsViscosity: 1.0 })\n"
                + "        .setView([51.4416, 5.4697], 13);\n"
                + "      map.setMaxBounds(eindhovenBounds);\n"
                + "      tiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {\n"
                + "        minZoom: 12, maxZoom: 19, updateWhenIdle: true, updateWhenZooming: false\n"
                + "      }).addTo(map);\n"
                + "      layer = L.layerGroup().addTo(map);\n"
                + "      if (pendingItems) { const items = pendingItems; pendingItems = null; window.setMarkers(items); }\n"
                + "      setTimeout(function () { window.redraw(); }, 50);\n"
                + "    };\n"
                + "\n"
                + "    window.setMarkers = function (items) {\n"
                + "      if (!map) { pendingItems = items; return; }\n"
                + "      layer.clearLayers();\n"
                + "      items.forEach(function (it) {\n"
                + "        const marker = L.marker([it.lat, it.lon]).addTo(layer).bindPopup(it.title);\n"
                + "        marker.on('popupopen', function () {\n"
                + "          setTimeout(function () {\n"
                + "            if (window.java && window.java.selectKunstwerk) window.java.selectKunstwerk(it.id);\n"
                + "          }, 0);\n"
                + "        });\n"
                + "      });\n"
                + "      if (layer.getLayers().length > 0) {\n"
                + "        const g = L.featureGroup(layer.getLayers());\n"
                + "        map.fitBounds(g.getBounds().pad(0.15), { maxZoom: 15, animate: false });\n"
                + "        map.panInsideBounds(eindhovenBounds, { animate: false });\n"
                + "      }\n"
                + "      setTimeout(function () { window.redraw(); }, 50);\n"
                + "    };\n"
                + "\n"
                + "    window.redraw = function () {\n"
                + "      if (!map) return;\n"
                + "      map.invalidateSize(true);\n"
                + "      if (tiles) tiles.redraw();\n"
                + "    };\n"
                + "  </script>\n"
                + "</body>\n"
                + "</html>\n";
    }
}
