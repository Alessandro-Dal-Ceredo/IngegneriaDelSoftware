package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Modello per la risposta di login dall'API di Supabase.
 * Contiene il token di accesso e i dati dell'utente.
 */
public class LoginResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("user")
    private User user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}