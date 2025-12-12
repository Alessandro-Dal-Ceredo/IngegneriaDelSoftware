package it.unive.raccoltapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Classe modello (POJO) che rappresenta una riga della tabella 'post' su Supabase.
 * Gson usa le annotazioni @SerializedName per mappare i nomi delle colonne JSON ai campi della classe.
 */
public class Post {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    /**
     * Costruttore per creare un nuovo Post da inviare al database.
     * L'ID non è incluso perché viene generato automaticamente da PostgreSQL.
     * @param title Il titolo del post.
     */
    public Post(String title) {
        this.title = title;
    }

    // Metodi getter e setter per accedere ai campi privati

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}