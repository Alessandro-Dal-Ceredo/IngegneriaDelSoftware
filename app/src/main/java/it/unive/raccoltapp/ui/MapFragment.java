package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.util.List;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.model.Bidone;
import it.unive.raccoltapp.model.GpxParser;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return root;
    }

    private BitmapDescriptor getScaledIcon(int drawableRes, int widthDp, int heightDp) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableRes);
        if (drawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        // converte dp → px
        float density = getResources().getDisplayMetrics().density;
        int widthPx = (int) (widthDp * density);
        int heightPx = (int) (heightDp * density);

        Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, widthPx, heightPx);
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        try {
            InputStream is = getResources().openRawResource(R.raw.bidoni_verona);
            List<Bidone> bidoni = GpxParser.parseBidoni(is);

            for (Bidone b : bidoni) {
                LatLng pos = new LatLng(b.getLat(), b.getLon());
                MarkerOptions marker = new MarkerOptions()
                        .position(pos)
                        .title(b.getNome());

                switch (b.getTipo()) {
                    case GpxParser.T_CARTA:
                        marker.icon(getScaledIcon(R.drawable.bidone_carta, 36, 36));
                        break;

                    case GpxParser.T_PLASTICA_LATTINE:
                        marker.icon(getScaledIcon(R.drawable.bidone_plastica, 36, 36));
                        break;

                    case GpxParser.T_VETRO:
                        marker.icon(getScaledIcon(R.drawable.bidone_vetro, 36, 36));
                        break;

                    case GpxParser.T_ORGANICO:
                        marker.icon(getScaledIcon(R.drawable.bidone_organico, 36, 36));
                        break;

                    case GpxParser.T_PANNOLINI:
                        marker.icon(getScaledIcon(R.drawable.bidone_pannolini, 36, 36));
                        break;

                    case GpxParser.T_SECCO:
                    default:
                        marker.icon(getScaledIcon(R.drawable.bidone_secco, 36, 36));
                        break;
                }


                mMap.addMarker(marker);
            }

            if (!bidoni.isEmpty()) {
                Bidone primo = bidoni.get(0);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(primo.getLat(), primo.getLon()), 13f));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private com.google.android.gms.maps.model.BitmapDescriptor getIcon(int drawableRes,
                                                                       float fallbackHue) {
        if (ContextCompat.getDrawable(requireContext(), drawableRes) != null) {
            return BitmapDescriptorFactory.fromResource(drawableRes);
        } else {
            return BitmapDescriptorFactory.defaultMarker(fallbackHue);
        }

    }
}
