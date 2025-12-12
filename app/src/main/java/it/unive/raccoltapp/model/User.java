package it.unive.raccoltapp.model;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id; // Modificato da int a String per supportare l'UUID di Supabase

    @SerializedName("name")
    private String name;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    // Nota: È sconsigliato gestire le password in questo modo.
    // L'autenticazione di Supabase dovrebbe essere usata per gestire gli utenti in modo sicuro.
    @SerializedName("password")
    private String password;

    public User(String name, String username, String email, String password){
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getId() { // Modificato il tipo di ritorno
        return id;
    }

    public void setId(String id) { // Modificato il tipo del parametro
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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