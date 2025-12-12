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

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Definizione dell'azione di navigazione verso il Login
        View.OnClickListener logoutAction = v -> {
            Toast.makeText(getContext(), "Logout in corso...", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_ProfileFragment_to_LoginFragment);
        };

        // --- APPLICO L'AZIONA A TUTTI I BOTTONI ---

        // 1. Applica al bottone Material rosso (se presente nell'XML come btn_logout)
        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(logoutAction);
        }

        // 2. Applica al bottone standard (se presente nell'XML come button_logout)
        // PRIMA QUI C'ERA "finishAffinity()" CHE CHIUDEVA L'APP. ORA NAVIGA AL LOGIN.
        if (binding.buttonLogout != null) {
            binding.buttonLogout.setOnClickListener(logoutAction);
        }

        // Tasto Salva (opzionale)
        if (binding.btnSave != null) {
            binding.btnSave.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Dati salvati!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
