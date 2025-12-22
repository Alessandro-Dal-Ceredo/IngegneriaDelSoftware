package it.unive.raccoltapp.model.maputils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class BidoneItem implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String tipo;

    public BidoneItem(double lat, double lon, String title, String tipo) {
        this.position = new LatLng(lat, lon);
        this.title = title;
        this.tipo = tipo;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return tipo;
    }

    @Override
    public Float getZIndex() {
        return null;
    }

    public String getTipo() {
        return tipo;
    }
}
