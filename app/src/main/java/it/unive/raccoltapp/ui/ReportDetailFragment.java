package it.unive.raccoltapp.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

import it.unive.raccoltapp.databinding.FragmentReportDetailBinding;
import it.unive.raccoltapp.model.ImageResponse;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.model.UserInfo;

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
                binding.detailReportTitle.setText(report.getTitle());
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
                } else {
                    binding.detailReportPriority.setText("Priorità: N/D");
                }

                List<ImageResponse> images = report.getImages();
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
