package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;

public class ImageResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("image")
    private String image;

    public Long getId() {
        return id;
    }

    public String getImage() {
        return image;
    }
}
