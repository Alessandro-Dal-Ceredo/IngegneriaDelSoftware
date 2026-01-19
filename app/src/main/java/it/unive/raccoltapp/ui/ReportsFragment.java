package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.databinding.FragmentReportsBinding;
import it.unive.raccoltapp.model.CalendarManager;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.network.API_MANAGER;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportAdapter adapter;
    private List<Report> reportList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilterSpinner();
        loadReportsFromApi();

        binding.fabAddReport.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_reportsFragment_to_addReportFragment);
        });
    }

    private void setupRecyclerView() {
        binding.recyclerViewReports.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportAdapter(reportList);
        binding.recyclerViewReports.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        CalendarManager.getInstance().fetchComuniFromSupabase(new CalendarManager.OnComuniReadyCallback() {
            @Override
            public void onComuniReady(List<String> comuni) {
                if (binding == null) return;
                List<String> cities = new ArrayList<>();
                cities.add("Tutti i comuni"); // Opzione di default
                cities.addAll(comuni);

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, cities);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCityFilter.setAdapter(spinnerAdapter);

                // Se l'utente è loggato, preseleziona il suo comune
                API_MANAGER apiManager = API_MANAGER.getInstance();
                if (apiManager.isLoggedIn()) {
                    String userCity = apiManager.getCity();
                    if (userCity != null) {
                        int position = spinnerAdapter.getPosition(userCity);
                        if (position >= 0) {
                            binding.spinnerCityFilter.setSelection(position);
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Errore nel caricamento dei comuni", Toast.LENGTH_SHORT).show();
            }
        });

        binding.spinnerCityFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = parent.getItemAtPosition(position).toString();
                adapter.filter(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }


    private void loadReportsFromApi() {
        API_MANAGER.getInstance().getReports(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                if (binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(getContext(), "Errore nel caricamento delle segnalazioni", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Errore di rete: " + t.getMessage(), Toast.LENGTH_SHORT).show();      }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
