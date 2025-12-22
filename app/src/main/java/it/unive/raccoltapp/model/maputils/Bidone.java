package it.unive.raccoltapp.model.maputils;

public class Bidone {
    private double lat;
    private double lon;
    private String nome;
    private String tipo; // Es. "CARTA", "PLASTICA_LATTINE", "VETRO", "ORGANICO", "PANNOLINI", "SECCO"

    public Bidone(double lat, double lon, String nome, String tipo) {
        this.lat = lat;
        this.lon = lon;
        this.nome = tipo;
        this.tipo = tipo;
    }

    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
}
