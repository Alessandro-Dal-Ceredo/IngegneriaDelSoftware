package it.unive.raccoltapp.model;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GpxParser {

    // Nomi costanti per le categorie usate dall'app
    public static final String T_CARTA = "CARTA";
    public static final String T_PLASTICA_LATTINE = "PLASTICA_LATTINE";
    public static final String T_VETRO = "VETRO";
    public static final String T_ORGANICO = "ORGANICO";
    public static final String T_PANNOLINI = "PANNOLINI";
    public static final String T_SECCO = "SECCO";

    public static List<Bidone> parseBidoni(InputStream inputStream) throws Exception {
        List<Bidone> bidoni = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, null);

        int eventType = parser.getEventType();
        Double lat = null, lon = null;
        String name = null;
        String desc = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tag = parser.getName();

            if (eventType == XmlPullParser.START_TAG) {
                if ("wpt".equalsIgnoreCase(tag)) {
                    String latStr = parser.getAttributeValue(null, "lat");
                    String lonStr = parser.getAttributeValue(null, "lon");
                    if (latStr != null && lonStr != null) {
                        lat = Double.parseDouble(latStr);
                        lon = Double.parseDouble(lonStr);
                    }
                } else if ("name".equalsIgnoreCase(tag)) {
                    name = parser.nextText();
                } else if ("desc".equalsIgnoreCase(tag)) {
                    desc = parser.nextText();
                }
            } else if (eventType == XmlPullParser.END_TAG && "wpt".equalsIgnoreCase(tag)) {
                if (lat != null && lon != null) {
                    // parse desc in mappa chiave->valore
                    Map<String,String> descMap = parseDescToMap(desc);
                    String tipo = detectCategoriaFromDesc(descMap);
                    // se non abbiamo nome, possiamo usare il nodo come nome
                    String finalName = (name != null && !name.trim().isEmpty()) ? name : descMap.getOrDefault("name", "");
                    bidoni.add(new Bidone(lat, lon, finalName, tipo));
                }
                // reset
                lat = lon = null;
                name = null;
                desc = null;
            }

            eventType = parser.next();
        }

        return bidoni;
    }

    // Trasforma il testo <desc> (che contiene linee "chiave=valore") in una Map
    private static Map<String,String> parseDescToMap(String desc) {
        Map<String,String> map = new HashMap<>();
        if (desc == null) return map;
        // alcuni GPX hanno nuove linee; consideriamo anche separatori diversi
        String[] lines = desc.split("[\\r\\n]+");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // probabile formato "key=value"
            int eq = trimmed.indexOf('=');
            if (eq > 0) {
                String k = trimmed.substring(0, eq).trim().toLowerCase();
                String v = trimmed.substring(eq + 1).trim().toLowerCase();
                map.put(k, v);
            } else {
                // se non ci sono '=' potremmo avere parole libere (es: "vetro"), quindi proviamo a inferirle
                String k = trimmed.toLowerCase();
                map.put(k, "yes");
            }
        }
        return map;
    }

    // Dalla mappa di attributi decide la categoria principale (scegliendo una priorità)
    private static String detectCategoriaFromDesc(Map<String,String> descMap) {
        // Controlli per pannolini
        if (hasYes(descMap, "recycling:diapers", "diapers", "recycling:diaper")) {
            return T_PANNOLINI;
        }

        // ORGANICO (biowaste/compost/organic/food)
        if (hasYes(descMap, "recycling:organic", "organic", "recycling:food", "biowaste", "compost", "organico")) {
            return T_ORGANICO;
        }

        // VETRO
        if (hasYes(descMap, "recycling:glass", "recycling:glass_bottles", "glass", "glass_bottles")) {
            return T_VETRO;
        }

        // PLASTICA / LATTINE
        if (hasYes(descMap,
                "recycling:plastic", "recycling:plastic_bottles", "recycling:pet", "recycling:pet_bottles",
                "recycling:cans", "recycling:beverage_cartons", "plastic", "pet", "cans")) {
            return T_PLASTICA_LATTINE;
        }

        // CARTA
        if (hasYes(descMap, "recycling:paper", "paper", "recycling:cardboard", "cardboard")) {
            return T_CARTA;
        }

        // SECCO / waste
        if (hasYes(descMap, "recycling:waste", "waste", "residual", "recycling:other")) {
            return T_SECCO;
        }

        // fallback: se c'è "amenity=recycling" ma non dettagli, viene secco (o lasciare SECCO)
        if (descMap.containsKey("amenity") && "recycling".equalsIgnoreCase(descMap.get("amenity"))) {
            return T_SECCO;
        }

        // default
        return T_SECCO;
    }

    // helper: controlla se una delle chiavi indicate è presente con valore "yes" o simili
    private static boolean hasYes(Map<String,String> map, String... keys) {
        for (String k : keys) {
            String v = map.get(k.toLowerCase());
            if (v == null) continue;
            v = v.trim().toLowerCase();
            if (v.equals("yes") || v.equals("true") || v.equals("1") || v.equals("y") || v.equals("si") || v.equals("sì")) {
                return true;
            }
            // a volte la chiave è presente con valore "only" o "collection" o senza valore -> consideriamo qualsiasi valore che NON sia "no"
            if (!v.equals("no") && !v.equals("false") && !v.equals("0")) {
                // Se la chiave è ad esempio "glass_bottles=yes" già catturata, ma qui consideriamo "glass" -> "vetro"
                // Evitiamo falsi positivi: solo treat as yes se valore non 'no' (es. "glass=yes", "glass_bottles=yes")
                return true;
            }
        }
        return false;
    }
}
