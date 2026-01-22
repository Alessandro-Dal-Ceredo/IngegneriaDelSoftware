package it.unive.raccoltapp.model;

import java.util.ArrayList;
import java.util.List;

public enum TypeOfReport {
    ABANDONED_WASTE("Rifiuti abbandonati"),
    OVERFLOWING_BIN("Cassonetto strapieno"),
    BULKY_WASTE("Rifiuto ingombrante"),
    ILLEGAL_DUMPING("Discarica abusiva"),
    DAMAGED_BIN("Cassonetto danneggiato"),
    UNSORTED_WASTE("Errato conferimento"),
    MISSED_COLLECTION("Mancata raccolta"),
    OTHER("Altro");

    private final String italianName;

    TypeOfReport(String italianName) {
        this.italianName = italianName;
    }

    @Override
    public String toString() {
        return italianName;
    }

    public static List<String> getItalianNames() {
        List<String> names = new ArrayList<>();
        for (TypeOfReport type : TypeOfReport.values()) {
            names.add(type.toString());
        }
        return names;
    }

    public static TypeOfReport fromItalianName(String italianName) {
        for (TypeOfReport type : TypeOfReport.values()) {
            if (type.toString().equalsIgnoreCase(italianName)) {
                return type;
            }
        }
        return null;
    }
}