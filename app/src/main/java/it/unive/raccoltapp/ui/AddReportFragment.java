package it.unive.raccoltapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.IOException;
import java.util.List;

import it.unive.raccoltapp.databinding.FragmentAddReportBinding;
import it.unive.raccoltapp.model.CalendarManager;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.network.API_MANAGER;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReportFragment extends Fragment {

    private static final String TAG = "AddReportFragment";
    private FragmentAddReportBinding binding;
    private Uri imageUri; // Per memorizzare l'URI dell'immagine selezionata

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivImagePreview.setImageURI(imageUri);
                    binding.ivImagePreview.setVisibility(View.VISIBLE);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCitySpinner();

        binding.btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnSubmitReport.setOnClickListener(v -> {
            submitReport();
        });
    }

    private void setupCitySpinner() {
        CalendarManager.getInstance().fetchComuniFromSupabase(new CalendarManager.OnComuniReadyCallback() {
            @Override
            public void onComuniReady(List<String> comuni) {
                if (getContext() == null) return;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, comuni);
                binding.actvReportCity.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Errore nel caricamento dei comuni", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReport() {
        String title = binding.etReportTitle.getText().toString().trim();
        String description = binding.etReportDescription.getText().toString().trim();
        String city = binding.actvReportCity.getText().toString().trim();
        String street = ""; // Puoi aggiungere un campo per la via se necessario

        if (title.isEmpty() || description.isEmpty() || city.isEmpty()) {
            Toast.makeText(getContext(), "Titolo, descrizione e comune sono obbligatori", Toast.LENGTH_SHORT).show();
            return;
        }

        API_MANAGER apiManager = API_MANAGER.getInstance();
        if (!apiManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Devi effettuare il login per inviare una segnalazione", Toast.LENGTH_SHORT).show();
            return;
        }

        Long userId = apiManager.getUserInfoId();
        if (userId == null) {
            Toast.makeText(getContext(), "Errore: ID utente non trovato. Riprova a fare il login.", Toast.LENGTH_LONG).show();
            return;
        }

        Report newReport = new Report(title, description, street, city, userId);

        apiManager.createReport(newReport, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Segnalazione inviata con successo!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(AddReportFragment.this).popBackStack();
                } else {
                    // FIX: Logga l'errore esatto dal server
                    String errorBody = "Corpo dell'errore non disponibile.";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Errore durante la lettura dell'errorBody", e);
                    }
                    String errorMessage = "Errore dall'invio: HTTP " + response.code() + " - " + errorBody;
                    Log.e(TAG, errorMessage);
                    Toast.makeText(getContext(), "Invio fallito. Controlla il Logcat per i dettagli.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Fallimento della chiamata createReport", t);
                Toast.makeText(getContext(), "Errore di rete: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
