package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.databinding.FragmentCalendarBinding;
import it.unive.raccoltapp.model.CalendarManager;
import it.unive.raccoltapp.model.RaccoltaGiorno;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarManager calendarManager;
    private Map<String, RaccoltaGiorno> calendarioMap;
    private String dataSelezionata;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendarManager = CalendarManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (dataSelezionata == null) {
            dataSelezionata = sdf.format(new Date(binding.calendarView.getDate()));
        }

        setupComuniSpinner();

        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            dataSelezionata = sdf.format(calendar.getTime());
            aggiornaInfoGiorno(dataSelezionata);
        });
    }

    private void setupComuniSpinner() {
        Spinner spinner = binding.spinnerComuni;

        CalendarManager.getInstance().fetchComuniFromSupabase(new CalendarManager.OnComuniReadyCallback() {
            @Override
            public void onComuniReady(List<String> comuni) {
                if (getContext() == null) return;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, comuni);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Errore nel caricamento dei comuni", Toast.LENGTH_SHORT).show();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String comuneSelezionato = parent.getItemAtPosition(position).toString();
                calendarManager.setComune(comuneSelezionato);
                caricaDatiEInizializza();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void caricaDatiEInizializza() {
        // Mostra uno stato di caricamento e pulisce i dati vecchi
        binding.collectionInfoText.setText("Caricamento...");
        if (calendarioMap != null) {
            calendarioMap.clear();
        }

        // Scarica i dati e aggiorna la UI SOLO quando il download è completo
        calendarManager.aggiornaDaServer(getContext(), this::mappaDatiEInizializzaUI);
    }

    private void mappaDatiEInizializzaUI() {
        if (getContext() == null) return; // Evita crash se il fragment viene distrutto
        List<RaccoltaGiorno> calendario = calendarManager.leggiCalendarioLocale(getContext());
        if (calendario != null) {
            calendarioMap = calendario.stream()
                    .collect(Collectors.toMap(g -> g.data, g -> g, (oldValue, newValue) -> newValue));

            aggiornaInfoGiorno(dataSelezionata);

            try {
                Date date = sdf.parse(dataSelezionata);
                if (date != null && binding.calendarView.getDate() != date.getTime()) {
                    binding.calendarView.setDate(date.getTime(), true, true);
                }
            } catch (ParseException e) {
                // ignore
            }
        } else {
            // Se il calendario non può essere letto, mostra un errore
            binding.collectionInfoText.setText("Errore nel caricamento del calendario.");
        }
    }

    private void aggiornaInfoGiorno(String data) {
        if (calendarioMap == null) {
            binding.collectionInfoText.setText("Caricamento...");
            return;
        }

        binding.selectedDateText.setText("Raccolta per il " + data + ":");

        RaccoltaGiorno giorno = calendarioMap.get(data);
        String info = (giorno != null && giorno.tipologie != null && !giorno.tipologie.isEmpty()) ?
                String.join(", ", giorno.tipologie) :
                "Nessuna raccolta prevista.";
        binding.collectionInfoText.setText(info);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
