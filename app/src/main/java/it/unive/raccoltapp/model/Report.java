package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Report implements Serializable {

    @SerializedName("id")
    private Long id;

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

    @SerializedName("priority")
    private Priority priority;

    @SerializedName("date")
    private String date;

    @SerializedName("type")
    private TypeOfReport type;

    @SerializedName("users_info")
    private UserInfo authorInfo;

    @SerializedName("images")
    private List<ImageResponse> images;

    public Report(String title, String description, String street, String city, Long idUser, Priority priority, String date, TypeOfReport type) {
        this.title = title;
        this.description = description;
        this.street = street;
        this.city = city;
        this.idUser = idUser;
        this.priority = priority;
        this.date = date;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public UserInfo getAuthorInfo() { return authorInfo; }
    public Double getLatitudine() { return latitudine; }
    public Double getLongitudine() { return longitudine; }
    public Long getIdUser() { return idUser; }
    public Priority getPriority() { return priority; }
    public List<ImageResponse> getImages() { return images; }
    public String getDate() { return date; }
    public TypeOfReport getType() { return type; }
}
