package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Report implements Serializable {

    @SerializedName("id")
    private Long id; // FIX: Modificato da long a Long per permettere il valore null

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("street")
    private String street;

    @SerializedName("city")
    private String city;

    @SerializedName("latitudine")
    private Double latitudine;

    @SerializedName("longitudine")
    private Double longitudine;

    @SerializedName("id_user")
    private Long idUser;

    @SerializedName("users_info")
    private UserInfo authorInfo;

    // Costruttore per la creazione di una nuova segnalazione
    public Report(String title, String description, String street, String city, Long idUser) {
        this.title = title;
        this.description = description;
        this.street = street;
        this.city = city;
        this.idUser = idUser;
        // L'ID non viene impostato, rimane null e verrà generato dal DB
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public UserInfo getAuthorInfo() { return authorInfo; }
    public Double getLatitudine() { return latitudine; }
    public Double getLongitudine() { return longitudine; }
    public Long getIdUser() { return idUser; }
}
