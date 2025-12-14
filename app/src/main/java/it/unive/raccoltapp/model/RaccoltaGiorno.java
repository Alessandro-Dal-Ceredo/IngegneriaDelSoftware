package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RaccoltaGiorno {
    
    @SerializedName("data")
    public String data;
    
    @SerializedName("giorno")
    public String giorno;
    
    @SerializedName("tipologie")
    public List<String> tipologie;
}
