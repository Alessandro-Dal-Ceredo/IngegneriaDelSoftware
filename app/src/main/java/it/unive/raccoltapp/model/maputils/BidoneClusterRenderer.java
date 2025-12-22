package it.unive.raccoltapp.model.maputils;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.Map;

import it.unive.raccoltapp.R;

public class BidoneClusterRenderer
        extends DefaultClusterRenderer<BidoneItem> {

    private final Context context;
    private final Map<String, BitmapDescriptor> iconCache = new HashMap<>();

    public BidoneClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<BidoneItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(BidoneItem item,
                                               MarkerOptions markerOptions) {

        markerOptions
                .icon(getIconForType(item.getTipo()))
                .title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<BidoneItem> cluster) {
        // cluster solo se > 4 elementi
        return cluster.getSize() > 4;
    }

    private BitmapDescriptor getIconForType(String tipo) {
        if (iconCache.containsKey(tipo)) {
            return iconCache.get(tipo);
        }

        int res;
        switch (tipo) {
            case GpxParser.T_CARTA:
                res = R.drawable.bidone_carta;
                break;
            case GpxParser.T_PLASTICA_LATTINE:
                res = R.drawable.bidone_plastica;
                break;
            case GpxParser.T_VETRO:
                res = R.drawable.bidone_vetro;
                break;
            case GpxParser.T_ORGANICO:
                res = R.drawable.bidone_organico;
                break;
            case GpxParser.T_PANNOLINI:
                res = R.drawable.bidone_pannolini;
                break;
            case GpxParser.T_SECCO:
            default:
                res = R.drawable.bidone_secco;
        }

        BitmapDescriptor icon =
                IconUtils.getScaledIcon(context, res, 36);
        iconCache.put(tipo, icon);
        return icon;
    }
}
