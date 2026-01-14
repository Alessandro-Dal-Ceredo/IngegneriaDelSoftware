package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.unive.raccoltapp.databinding.FragmentReportDetailBinding;
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

        // Recupera i dati della segnalazione passati dal fragment precedente
        if (getArguments() != null) {
            Report report = (Report) getArguments().getSerializable("report");
            if (report != null) {
                // Popola le viste con i dati della segnalazione
                binding.detailReportTitle.setText(report.getTitle());
                binding.detailReportDescription.setText(report.getDescription());
                String location = "Luogo: " + report.getStreet() + ", " + report.getCity();
                binding.detailReportLocation.setText(location);

                // FIX: Usa il nuovo metodo per ottenere l'autore
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
