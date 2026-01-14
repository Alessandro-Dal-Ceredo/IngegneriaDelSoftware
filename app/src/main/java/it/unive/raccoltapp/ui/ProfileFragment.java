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
import it.unive.raccoltapp.network.API_MANAGER;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
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

        binding.tilAddress.setEndIconOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void loadUserProfile() {
        API_MANAGER apiManager = API_MANAGER.getInstance();

        String email = apiManager.getUserEmail();
        String username = apiManager.getUsername();
        String name = apiManager.getName();

        if (email != null) {
            binding.tvUserEmail.setText(email);
            binding.etEmail.setText(email);
        }
        if (username != null) {
            binding.tvUserName.setText(username);
        }
        if (name != null) {
            binding.etName.setText(name);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        binding.etAddress.setText(address.getAddressLine(0));
                    } else {
                        Toast.makeText(getContext(), "Indirizzo non trovato", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e("ProfileFragment", "Errore Geocoder", e);
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
