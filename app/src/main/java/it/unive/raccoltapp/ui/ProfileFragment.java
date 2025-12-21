package it.unive.raccoltapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.databinding.FragmentProfileBinding;
import it.unive.raccoltapp.model.UserInfo;
import it.unive.raccoltapp.network.API_MANAGER;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    // Launcher per la richiesta dei permessi di localizzazione
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation(); // Se il permesso è concesso, ottieni la posizione
                } else {
                    Toast.makeText(getContext(), "Permesso di localizzazione negato", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();

        binding.buttonLogout.setOnClickListener(v -> {
            API_MANAGER.getInstance().logout();
            Toast.makeText(getContext(), "Logout effettuato", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.action_global_to_LoginFragment);
        });

        // Imposta il listener per l'icona di localizzazione
        binding.tilAddress.setEndIconOnClickListener(v -> {
            // Controlla se il permesso è già stato concesso
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                // Altrimenti, richiede il permesso
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void loadUserProfile() {
        String email = API_MANAGER.getInstance().getUserEmail();
        if (email != null) {
            binding.tvUserEmail.setText(email);
            binding.etEmail.setText(email);
        }

        API_MANAGER.getInstance().getUserInfo(new Callback<List<UserInfo>>() {
            @Override
            public void onResponse(Call<List<UserInfo>> call, Response<List<UserInfo>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    UserInfo userInfo = response.body().get(0);
                    binding.tvUserName.setText(userInfo.getUsername());
                    binding.etName.setText(userInfo.getName());
                }
            }

            @Override
            public void onFailure(Call<List<UserInfo>> call, Throwable t) {
                Log.e("ProfileFragment", "Errore nel caricamento del profilo", t);
            }
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Controllo di sicurezza
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                // Converte le coordinate in un indirizzo
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        // Imposta l'indirizzo nel campo di testo
                        binding.etAddress.setText(address.getAddressLine(0));
                    } else {
                        Toast.makeText(getContext(), "Indirizzo non trovato", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e("ProfileFragment", "Errore Geocoder", e);
                    Toast.makeText(getContext(), "Servizio di geocoding non disponibile", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Impossibile ottenere la posizione", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
