package it.unive.raccoltapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
    private FusedLocationProviderClient fusedLocationClient;

    // Launcher per la richiesta dei permessi di localizzazione
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    enableMyLocation(); // Se il permesso è concesso, abilita la localizzazione
                } else {
                    Toast.makeText(getContext(), "Permesso di localizzazione negato", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 1. Tenta di abilitare la localizzazione dell'utente sulla mappa
        enableMyLocation();

        // 2. Carica i bidoni dal file GPX
        loadBidoni();
    }

    private void enableMyLocation() {
        // Controlla se il permesso è già stato concesso
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                // Ottieni l'ultima posizione nota e centra la mappa
                fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    }
                });
            }
        } else {
            // Altrimenti, richiede il permesso
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadBidoni() {
        try {
            InputStream is = getResources().openRawResource(R.raw.bidoni_verona);
            List<Bidone> bidoni = GpxParser.parseBidoni(is);

            for (Bidone b : bidoni) {
                LatLng pos = new LatLng(b.getLat(), b.getLon());
                MarkerOptions marker = new MarkerOptions().position(pos).title(b.getNome());

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

            // Muove la camera solo se non è stato possibile ottenere la posizione dell'utente
            mMap.getUiSettings().setZoomControlsEnabled(true);

        } catch (Exception e) {
            Log.e("MapFragment", "Errore durante il parsing dei bidoni", e);
        }
    }

    private BitmapDescriptor getScaledIcon(int drawableRes, int widthDp, int heightDp) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableRes);
        if (drawable == null) return BitmapDescriptorFactory.defaultMarker();

        float density = getResources().getDisplayMetrics().density;
        int widthPx = (int) (widthDp * density);
        int heightPx = (int) (heightDp * density);

        Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, widthPx, heightPx);
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
