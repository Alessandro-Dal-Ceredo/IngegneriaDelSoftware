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

        // Imposta l'azione di logout
        binding.buttonLogout.setOnClickListener(v -> {
            // Esegue il logout
            API_MANAGER.getInstance().logout();
            Toast.makeText(getContext(), "Logout effettuato", Toast.LENGTH_SHORT).show();

            // Usa l'azione globale per tornare al login e pulire lo stack
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_global_to_LoginFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
