package it.unive.raccoltapp.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

        if (getArguments() != null) {
            Report report = (Report) getArguments().getSerializable("report");
            if (report != null) {
                binding.detailReportTitle.setText(report.getTitle());
                binding.detailReportDescription.setText(report.getDescription());
                binding.detailReportCity.setText("Città: " + report.getCity());
                binding.detailReportStreet.setText("Via: " + report.getStreet());
                if (report.getPriority() != null) {
                    binding.detailReportPriority.setText("Priorità: " + report.getPriority().toString());
                } else {
                    binding.detailReportPriority.setText("Priorità: N/D");
                }

                List<ImageResponse> images = report.getImages();
                if (images != null && !images.isEmpty()) {
                    byte[] decodedString = Base64.decode(images.get(0).getImage(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    binding.detailReportImage.setImageBitmap(decodedByte);
                    binding.detailReportImage.setVisibility(View.VISIBLE);
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
