package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import it.unive.raccoltapp.databinding.FragmentCalendarBinding;
import it.unive.raccoltapp.model.CalendarManager;
import it.unive.raccoltapp.model.RaccoltaGiorno;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarManager calendarManager;
    private Map<String, RaccoltaGiorno> calendarioMap;

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

        // Imposta il listener per il cambio di data
        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            // Formatta la data selezionata nel formato yyyy-MM-dd per la ricerca
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String selectedDate = sdf.format(calendar.getTime());

            // Aggiorna la UI con le informazioni del giorno
            aggiornaInfoGiorno(selectedDate);
        });

        // Carica i dati e aggiorna la UI con la data corrente
        caricaDatiEInizializza();
    }

    private void caricaDatiEInizializza() {
        // Carica i dati in background
        calendarManager.aggiornaDaServer(getContext(), () -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::mappaDatiEInizializzaUI);
            }
        });
        // Nel frattempo, carica i dati locali
        mappaDatiEInizializzaUI();
    }

    private void mappaDatiEInizializzaUI() {
        List<RaccoltaGiorno> calendario = calendarManager.leggiCalendarioLocale(getContext());
        if (calendario != null) {
            // Converte la lista in una mappa per un accesso rapido
            calendarioMap = calendario.stream()
                    .collect(Collectors.toMap(g -> g.data, g -> g));
            
            // Mostra le informazioni per la data corrente all'avvio
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String oggi = sdf.format(Calendar.getInstance().getTime());
            aggiornaInfoGiorno(oggi);
        }
    }

    private void aggiornaInfoGiorno(String data) {
        if (calendarioMap == null) return;

        // Aggiorna il testo della data selezionata
        binding.selectedDateText.setText("Raccolta per il " + data + ":");

        // Cerca le informazioni per la data e aggiorna il testo della raccolta
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
        binding = null; // Evita memory leak
    }
}
