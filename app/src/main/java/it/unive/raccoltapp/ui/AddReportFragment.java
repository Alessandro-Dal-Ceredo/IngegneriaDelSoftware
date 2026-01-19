package it.unive.raccoltapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.databinding.FragmentAddReportBinding;
import it.unive.raccoltapp.model.CalendarManager;
import it.unive.raccoltapp.model.Image;
import it.unive.raccoltapp.model.Priority;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.model.ReportResponse;
import it.unive.raccoltapp.model.TypeOfReport;
import it.unive.raccoltapp.network.API_MANAGER;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReportFragment extends Fragment {

    private static final String TAG = "AddReportFragment";
    private FragmentAddReportBinding binding;
    private List<Uri> imageUris = new ArrayList<>();
    private String currentPhotoPath;
    private Calendar selectedDate = Calendar.getInstance();

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "Permesso per la fotocamera negato", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        ClipData clipData = result.getData().getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            imageUris.add(imageUri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        imageUris.add(imageUri);
                    }
                    updateImagePreviews();
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    File file = new File(currentPhotoPath);
                    Uri imageUri = Uri.fromFile(file);
                    imageUris.add(imageUri);
                    updateImagePreviews();
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

        binding.backArrow.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        setupCitySpinner();
        setupPriorityMenu();
        setupReportTypeMenu();
        setupDatePicker();

        binding.btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryLauncher.launch(intent);
        });

        binding.btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        binding.btnSubmitReport.setOnClickListener(v -> {
            submitReport();
        });
    }

    private void updateImagePreviews() {
        binding.glImagePreviews.removeAllViews();
        if (imageUris.isEmpty()){
            binding.glImagePreviews.setVisibility(View.GONE);
        } else {
            binding.glImagePreviews.setVisibility(View.VISIBLE);
            for (Uri uri : imageUris) {
                ImageView imageView = new ImageView(getContext());
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 300;
                params.height = 300;
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                imageView.setImageURI(uri);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setOnClickListener(v -> {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                        ImageZoomFragment.newInstance(bitmap).show(getParentFragmentManager(), "image_zoom");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                binding.glImagePreviews.addView(imageView);
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "it.unive.raccoltapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
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

    private void setupPriorityMenu() {
        if (getContext() == null) return;
        ArrayAdapter<Priority> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, Priority.values());
        binding.actvPriority.setAdapter(adapter);
    }

    private void setupReportTypeMenu() {
        if (getContext() == null) return;
        List<String> reportTypes = Arrays.asList(getResources().getStringArray(R.array.report_types));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, reportTypes);
        binding.actvReportType.setAdapter(adapter);
    }

    private void setupDatePicker() {
        updateDateInView(); // Imposta la data odierna come default
        binding.etReportDate.setOnClickListener(v -> {
            DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateInView();
            };

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), dateSetListener,
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void updateDateInView() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.etReportDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void submitReport() {
        String title = binding.etReportTitle.getText().toString().trim();
        String description = binding.etReportDescription.getText().toString().trim();
        String city = binding.actvReportCity.getText().toString().trim();
        String street = binding.etReportStreet.getText().toString().trim();
        String priorityString = binding.actvPriority.getText().toString().trim();
        String typeString = binding.actvReportType.getText().toString().trim();
        String dateString = binding.etReportDate.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || city.isEmpty() || street.isEmpty() || priorityString.isEmpty() || typeString.isEmpty() || dateString.isEmpty()) {
            Toast.makeText(getContext(), "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show();
            return;
        }

        Priority priority = Priority.valueOf(priorityString.toUpperCase(Locale.ROOT));

        List<String> reportTypesList = Arrays.asList(getResources().getStringArray(R.array.report_types));
        int typeIndex = reportTypesList.indexOf(typeString);
        TypeOfReport type = TypeOfReport.OTHER; // Valore di default
        if (typeIndex != -1 && typeIndex < TypeOfReport.values().length) {
            type = TypeOfReport.values()[typeIndex];
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate.getTime());

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

        Report newReport = new Report(title, description, street, city, userId, priority, formattedDate, type);

        apiManager.createReport(newReport, new Callback<List<ReportResponse>>() {
            @Override
            public void onResponse(Call<List<ReportResponse>> call, Response<List<ReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    long reportId = response.body().get(0).getId();
                    Toast.makeText(getContext(), "Segnalazione inviata con successo!", Toast.LENGTH_SHORT).show();

                    if (!imageUris.isEmpty()) {
                        for (Uri uri : imageUris) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] imageInByte = baos.toByteArray();
                                String encodedImage = Base64.encodeToString(imageInByte, Base64.DEFAULT);

                                Image image = new Image(encodedImage, reportId);
                                apiManager.uploadImage(image, new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if(response.isSuccessful()){
                                            Log.d(TAG, "Immagine caricata con successo!");
                                        }
                                        else{
                                            Log.e(TAG, "Errore nel caricamento dell'immagine: " + response.code());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Log.e(TAG, "Errore di rete durante il caricamento dell'immagine", t);
                                    }
                                });
                            } catch (IOException e) {
                                Log.e(TAG, "Could not process image", e);                            }
                        }
                        NavHostFragment.findNavController(AddReportFragment.this).popBackStack();
                    } else {
                        NavHostFragment.findNavController(AddReportFragment.this).popBackStack();
                    }

                } else {
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
            public void onFailure(Call<List<ReportResponse>> call, Throwable t) {
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
