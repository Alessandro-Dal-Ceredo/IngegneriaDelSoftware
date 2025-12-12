package it.unive.raccoltapp.ui;

import it.unive.raccoltapp.databinding.FragmentSignupBinding;

import it.unive.raccoltapp.R;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import it.unive.raccoltapp.model.LoginResponse;
import it.unive.raccoltapp.network.API_MANAGER;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {

    private FragmentSignupBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSignup.setOnClickListener(v -> {
            String name = binding.editTextName.getText().toString();
            String username = binding.editTextUsernameSignup.getText().toString();
            String email = binding.editTextEmailSignup.getText().toString();
            String password = binding.editTextPasswordSignup.getText().toString();

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show();
                return;
            }

            API_MANAGER.getInstance().signUpUser(email, password, name, username, new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Registrazione riuscita! Effettua il login.", Toast.LENGTH_LONG).show();
                        // Riportiamo l'utente alla pagina di login
                        NavHostFragment.findNavController(SignUpFragment.this)
                                .navigate(R.id.action_SignUpFragment_to_LoginFragment);
                    } else {
                        Toast.makeText(getContext(), "Errore durante la registrazione", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Errore di rete", Toast.LENGTH_SHORT).show();
                    Log.e("SignUpFragment", "Errore di rete", t);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}