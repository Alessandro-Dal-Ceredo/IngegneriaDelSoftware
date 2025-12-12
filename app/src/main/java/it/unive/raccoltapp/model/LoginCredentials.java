package it.unive.raccoltapp.model;

/**
 * Modello per inviare le credenziali di login (email e password) all'API di Supabase.
 */
public class LoginCredentials {

    private String email;
    private String password;

    public LoginCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}