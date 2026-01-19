package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.databinding.FragmentProfileBinding;
import it.unive.raccoltapp.network.API_MANAGER;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();

        binding.btnLogout.setOnClickListener(v -> {
            API_MANAGER.getInstance().logout();
            Toast.makeText(getContext(), "Logout effettuato", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.action_global_to_LoginFragment);
        });

        binding.btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserProfile() {
        API_MANAGER apiManager = API_MANAGER.getInstance();

        String email = apiManager.getUserEmail();
        String name = apiManager.getName();
        String city = apiManager.getCity();

        if (email != null) {
            binding.tvUserEmail.setText(email);
            binding.etEmail.setText(email);
        }
        if (name != null) {
            binding.tvUserName.setText(name);
            binding.etName.setText(name);
        }
        if (city != null) {
            binding.cityDropdownProfile.setText(city, false);
        }
    }

    private void saveProfileChanges() {
        String newCity = binding.cityDropdownProfile.getText().toString();
        API_MANAGER apiManager = API_MANAGER.getInstance();

        if (newCity.isEmpty()) {
            Toast.makeText(getContext(), "Il comune non può essere vuoto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newCity.equals(apiManager.getCity())) {
            Toast.makeText(getContext(), "Nessuna modifica da salvare", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSaveChanges.setEnabled(false);
        binding.btnSaveChanges.setText("Salvataggio...");

        apiManager.updateUserCity(newCity, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                binding.btnSaveChanges.setEnabled(true);
                binding.btnSaveChanges.setText(getString(R.string.profile_save));

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Comune aggiornato con successo", Toast.LENGTH_SHORT).show();
                    apiManager.setCity(newCity);
                } else {
                     Toast.makeText(getContext(), "Errore durante l'aggiornamento del comune", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                binding.btnSaveChanges.setEnabled(true);
                binding.btnSaveChanges.setText(getString(R.string.profile_save));
                Toast.makeText(getContext(), "Errore di rete. Riprova.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
