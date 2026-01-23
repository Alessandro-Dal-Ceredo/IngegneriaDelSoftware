package it.unive.raccoltapp.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.databinding.FragmentReportDetailBinding;
import it.unive.raccoltapp.model.ImageResponse;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.model.UserInfo;
import it.unive.raccoltapp.network.API_MANAGER;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailFragment extends Fragment {

    private FragmentReportDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backArrow.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        if (getArguments() != null) {
            Report report = (Report) getArguments().getSerializable("report");
            if (report != null) {
                binding.detailReportDescription.setText(report.getDescription());
                binding.detailReportCity.setText("Città: " + report.getCity());
                binding.detailReportStreet.setText("Via: " + report.getStreet());

                if (report.getType() != null) {
                    binding.detailReportType.setText("Tipo: " + report.getType().toString().replace("_", " "));
                } else {
                    binding.detailReportType.setText("Tipo: N/D");
                }

                if (report.getDate() != null) {
                    binding.detailReportDate.setText("Data: " + report.getDate());
                } else {
                    binding.detailReportDate.setText("Data: N/D");
                }

                if (report.getPriority() != null) {
                    binding.detailReportPriority.setText("Priorità: " + report.getPriority().toString());
                    // --- INIZIO LOGICA COLORE BORDO ---
                    int colorCode;

                    switch (report.getPriority()) {
                        case EXTREME:
                            colorCode = android.graphics.Color.RED;
                            break;
                        case HIGH:
                            colorCode = android.graphics.Color.parseColor("#FFA500"); // Arancione
                            break;
                        case MEDIUM:
                            colorCode = android.graphics.Color.parseColor("#FFEE53"); // Giallo
                            break;
                        case LOW:
                            colorCode = android.graphics.Color.parseColor("#4CAF50"); // Verde
                            break;
                        default:
                            colorCode = android.graphics.Color.GRAY; // Colore per priorità sconosciute
                            break;
                    }
                    if (report.getType() != null) {
                        int iconResId;

                        switch (report.getType()) {
                            case METEO_ALERT:
                                iconResId = R.drawable.ic_meteo_alert;
                                break;
                            case ABANDONED_WASTE:
                                iconResId = R.drawable.ic_abandoned_waste;
                                break;
                            case OVERFLOWING_BIN:
                                iconResId = R.drawable.ic_overflowing_bin;
                                break;
                            case BULKY_WASTE:
                                iconResId = R.drawable.ic_bulky_waste;
                                break;
                            case ILLEGAL_DUMPING:
                                iconResId = R.drawable.ic_illegal_dumping;
                                break;
                            case DAMAGED_BIN:
                                iconResId = R.drawable.ic_damaged_bin;
                                break;
                            case UNSORTED_WASTE:
                                iconResId = R.drawable.ic_unsorted_waste;
                                break;
                            case MISSED_COLLECTION:
                                iconResId = R.drawable.ic_missed_collection;
                                break;
                            default:
                                iconResId = R.drawable.ic_waste_default;
                                break;
                        }

                        binding.detailReportIcon.setImageResource(iconResId);
                    }

                    binding.cardDetailContainer.setStrokeColor(colorCode);
                    binding.detailReportPriority.setTextColor(colorCode);
                    binding.detailReportPriority.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    binding.detailReportPriority.setText("Priorità: N/D");
                }

                // --- LOGICA CARICAMENTO IMMAGINI ---
                API_MANAGER.getInstance().getReportImages("eq." + report.getId(), new Callback<List<Report>>() {
                    @Override
                    public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                        if (isAdded() && binding != null && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<ImageResponse> images = response.body().get(0).getImages();

                            if (images != null && !images.isEmpty()) {
                                binding.glReportImages.removeAllViews();
                                binding.glReportImages.setVisibility(View.VISIBLE);
                                for (ImageResponse image : images) {
                                    byte[] decodedString = Base64.decode(image.getImage(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    ImageView imageView = new ImageView(getContext());
                                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                                    params.width = 300;
                                    params.height = 300;
                                    params.setMargins(8, 8, 8, 8);
                                    imageView.setLayoutParams(params);
                                    imageView.setImageBitmap(decodedByte);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    imageView.setOnClickListener(v -> {
                                        ImageZoomFragment.newInstance(decodedByte).show(getParentFragmentManager(), "image_zoom");
                                    });
                                    binding.glReportImages.addView(imageView);
                                }
                            } else {
                                binding.glReportImages.setVisibility(View.GONE);
                            }
                        } else {
                            if (isAdded() && binding != null) {
                                binding.glReportImages.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Report>> call, Throwable t) {
                        if (isAdded() && getContext() != null) {
                            Log.e("ReportDetailFragment", "Failed to load images", t);
                            Toast.makeText(getContext(), "Errore nel caricamento delle immagini", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                String authorText = "Autore: Sconosciuto";
                UserInfo authorInfo = report.getAuthorInfo();
                if (authorInfo != null && authorInfo.getUsername() != null) {
                    authorText = "Autore: " + authorInfo.getUsername();
                }
                binding.detailReportAuthor.setText(authorText);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
