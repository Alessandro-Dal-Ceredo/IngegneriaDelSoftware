package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("username")
    private String username;

    @SerializedName("city")
    private String city;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getCity() { return city; }
}
