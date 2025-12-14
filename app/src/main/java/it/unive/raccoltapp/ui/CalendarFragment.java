package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
        calendarManager = new CalendarManager();
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

        Spinner spinner = binding.spinnerComuni;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.comuni, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String comuneSelezionato = parent.getItemAtPosition(position).toString().toLowerCase(Locale.ROOT);
                calendarManager.setComune(comuneSelezionato);
                caricaDatiEInizializza();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            dataSelezionata = sdf.format(calendar.getTime());
            aggiornaInfoGiorno(dataSelezionata);
        });

        caricaDatiEInizializza();
    }

    private void caricaDatiEInizializza() {
        calendarManager.aggiornaDaServer(getContext(), () -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::mappaDatiEInizializzaUI);
            }
        });
        mappaDatiEInizializzaUI();
    }

    private void mappaDatiEInizializzaUI() {
        List<RaccoltaGiorno> calendario = calendarManager.leggiCalendarioLocale(getContext());
        if (calendario != null) {
            calendarioMap = calendario.stream()
                    .collect(Collectors.toMap(g -> g.data, g -> g));

            aggiornaInfoGiorno(dataSelezionata);

            try {
                Date date = sdf.parse(dataSelezionata);
                if (date != null && binding.calendarView.getDate() != date.getTime()) {
                    binding.calendarView.setDate(date.getTime(), true, true);
                }
            } catch (ParseException e) {
                // ignore
            }
        }
    }

    private void aggiornaInfoGiorno(String data) {
        if (calendarioMap == null) return;

        binding.selectedDateText.setText("Raccolta per il " + data + ":");

        RaccoltaGiorno giorno = calendarioMap.get(data);
        String info;
        if (giorno != null && giorno.tipologie != null && !giorno.tipologie.isEmpty()) {
            info = String.join(", ", giorno.tipologie);
        } else {
            info = "Nessuna raccolta prevista.";
        }
        binding.collectionInfoText.setText(info);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
