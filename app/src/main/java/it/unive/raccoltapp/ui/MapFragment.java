package it.unive.raccoltapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
import com.google.maps.android.clustering.ClusterManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.databinding.FragmentMapBinding;
import it.unive.raccoltapp.model.maputils.Bidone;
import it.unive.raccoltapp.model.maputils.BidoneClusterRenderer;
import it.unive.raccoltapp.model.maputils.BidoneItem;
import it.unive.raccoltapp.model.maputils.GpxParser;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private GoogleMap mMap;
    private FragmentMapBinding binding;
    // MODIFICA QUESTA RIGA
    private FusedLocationProviderClient fusedLocationClient; // Invece di locationClient

    private ClusterManager<BidoneItem> clusterManager;
    private final List<BidoneItem> allItems = new ArrayList<>();

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
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(inflater, container, false);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireContext());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupFilters();

        // ✅ FILTER DROPDOWN TOGGLE
        binding.filterHeader.setOnClickListener(v -> {
            if (binding.filterContent.getVisibility() == View.VISIBLE) {
                binding.filterContent.setVisibility(View.GONE);
            } else {
                binding.filterContent.setVisibility(View.VISIBLE);
            }
        });

        return binding.getRoot();
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Abilita i controlli dello zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);

        enableMyLocation();

        clusterManager = new ClusterManager<>(requireContext(), mMap);
        clusterManager.setRenderer(
                new BidoneClusterRenderer(requireContext(), mMap, clusterManager)
        );

        mMap.setOnCameraIdleListener(() -> {
            clusterManager.onCameraIdle();
            applyFilters();
        });

        mMap.setOnMarkerClickListener(clusterManager);

        loadBidoni();
    }

    private void loadBidoni() {
        try {
            InputStream is = getResources().openRawResource(R.raw.bidoni_veneto);
            List<Bidone> bidoni = GpxParser.parseBidoni(is);

            allItems.clear();
            for (Bidone b : bidoni) {
                allItems.add(new BidoneItem(
                        b.getLat(),
                        b.getLon(),
                        b.getNome(),
                        b.getTipo()
                ));
            }

            applyFilters();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        binding.cbCarta.setOnCheckedChangeListener((v, c) -> applyFilters());
        binding.cbPlastica.setOnCheckedChangeListener((v, c) -> applyFilters());
        binding.cbVetro.setOnCheckedChangeListener((v, c) -> applyFilters());
        binding.cbOrganico.setOnCheckedChangeListener((v, c) -> applyFilters());
        binding.cbPannolini.setOnCheckedChangeListener((v, c) -> applyFilters());
        binding.cbSecco.setOnCheckedChangeListener((v, c) -> applyFilters());
    }

    private void applyFilters() {
        if (clusterManager == null || mMap == null) return;

        Set<String> enabled = new HashSet<>();
        if (binding.cbCarta.isChecked()) enabled.add(GpxParser.T_CARTA);
        if (binding.cbPlastica.isChecked()) enabled.add(GpxParser.T_PLASTICA_LATTINE);
        if (binding.cbVetro.isChecked()) enabled.add(GpxParser.T_VETRO);
        if (binding.cbOrganico.isChecked()) enabled.add(GpxParser.T_ORGANICO);
        if (binding.cbPannolini.isChecked()) enabled.add(GpxParser.T_PANNOLINI);
        if (binding.cbSecco.isChecked()) enabled.add(GpxParser.T_SECCO);

        clusterManager.clearItems();
        for (BidoneItem item : allItems) {
            if (enabled.contains(item.getTipo())) {
                clusterManager.addItem(item);
            }
        }
        clusterManager.cluster();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
