package it.unive.raccoltapp.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Modello per inviare i dati di registrazione (email, password e metadati) all'API di Supabase.
 */
public class SignUpCredentials {

    private String email;
    private String password;
    private Map<String, String> data; // Campo speciale per i metadati dell'utente

    public SignUpCredentials(String email, String password, String name, String username) {
        this.email = email;
        this.password = password;

        // Inseriamo i dati extra nel campo 'data' come richiesto da Supabase
        this.data = new HashMap<>();
        this.data.put("name", name);
        this.data.put("username", username);
    }

    // Getters e Setters

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

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}