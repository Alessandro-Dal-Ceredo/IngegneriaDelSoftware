package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;

public class ReportResponse {

    @SerializedName("id")
    private Long id;

    public Long getId() {
        return id;
    }
}
