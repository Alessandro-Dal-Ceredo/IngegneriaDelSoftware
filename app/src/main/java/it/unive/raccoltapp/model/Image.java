package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;

public class Image {

    @SerializedName("id")
    private Long id;

    @SerializedName("image")
    private String image;

    @SerializedName("id_report")
    private Long idReport;

    public Image(String image, Long idReport) {
        this.image = image;
        this.idReport = idReport;
    }

    public Long getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public Long getIdReport() {
        return idReport;
    }
}
