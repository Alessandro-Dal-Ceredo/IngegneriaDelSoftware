package it.unive.raccoltapp.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
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

    private final SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
        // 1. PRIMA carica subito i dati locali (se esistono), così l'utente non vede "Caricamento..." all'infinito
        mappaDatiEInizializzaUI();

        // 2. POI chiedi al server di aggiornare i dati in background
        calendarManager.aggiornaDaServer(getContext(), new Runnable() {
            @Override
            public void run() {
                // Quando il server ha finito, ricarica la UI con i dati freschi
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> mappaDatiEInizializzaUI());
                }
            }
        });
    }

    private void mappaDatiEInizializzaUI() {
        if (getContext() == null) return;
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
            // CORREZIONE: Se fallisce, scriviamo l'errore nella Card
            binding.tvWasteTypeCalendar.setText("Errore caricamento dati");
            binding.tvWasteTimeCalendar.setText("Controlla la connessione");
            configuraGraficaCard(null); // Grafica neutra
        }
    }

    private void aggiornaInfoGiorno(String dataKey) {
        // Gestione Titolo Data
        try {
            Date dateObj = sdf.parse(dataKey);
            String dataBella = sdfDisplay.format(dateObj);
            binding.selectedDateText.setText("Ritiro previsto per il " + dataBella + ":");
        } catch (ParseException e) {
            binding.selectedDateText.setText("Data: " + dataKey);
        }

        // SE LA MAPPA È NULLA O VUOTA, trattiamolo come "Nessun ritiro" invece di uscire
        if (calendarioMap == null || !calendarioMap.containsKey(dataKey)) {
            // Nessun dato trovato per questa data -> Reset Grafica
            binding.tvWasteTypeCalendar.setText("Nessun ritiro");
            binding.tvWasteTimeCalendar.setText("");
            configuraGraficaCard(null); // Resetta colore e icona a grigio
            return;
        }

        // SE ARRIVIAMO QUI, ABBIAMO I DATI
        RaccoltaGiorno giorno = calendarioMap.get(dataKey);

        if (giorno != null && giorno.tipologie != null && !giorno.tipologie.isEmpty()) {
            // Formatta il testo (Prima lettera maiuscola)
            String tipologiaPrincipale = giorno.tipologie.stream()
                    .map(parola -> {
                        if (parola.length() > 0) {
                            return parola.substring(0, 1).toUpperCase() + parola.substring(1);
                        }
                        return parola;
                    })
                    .collect(Collectors.joining(", "));

            binding.tvWasteTypeCalendar.setText(tipologiaPrincipale);
            binding.tvWasteTimeCalendar.setText("Esporre entro le 06:00");

            // Colora la card
            configuraGraficaCard(giorno.tipologie.get(0));

        } else {
            // Giorno presente nel DB ma lista vuota -> Nessun ritiro
            binding.tvWasteTypeCalendar.setText("Nessun ritiro");
            binding.tvWasteTimeCalendar.setText("");
            configuraGraficaCard(null);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void configuraGraficaCard(String tipoRifiuto) {

        if (tipoRifiuto == null || tipoRifiuto.isEmpty() || tipoRifiuto.toLowerCase().contains("nessun")) {
            // Sfondo Bianco Pulito
            binding.cardWasteResult.setCardBackgroundColor(Color.WHITE);

            // Bordo Grigio Sottile (Default)
            binding.cardWasteResult.setStrokeColor(Color.parseColor("#E0E0E0"));
            binding.cardWasteResult.setStrokeWidth(2); // Bordo sottile

            // Icona e Testo Grigio Neutro
            binding.imgWasteIconCalendar.setImageResource(R.drawable.ic_calendarhq);
            binding.imgWasteIconCalendar.setColorFilter(Color.parseColor("#757575"));
            binding.tvWasteTimeCalendar.setTextColor(Color.parseColor("#757575"));

            return; // Esci dalla funzione, abbiamo finito il reset
        }

        String tipoLower = tipoRifiuto.toLowerCase();
        int colorRes = R.color.alert_bg_light; // Default
        int iconRes = R.drawable.ic_calendarhq; // Default icon

        if (tipoLower.contains("umido") || tipoLower.contains("organico")) {
            iconRes = R.drawable.ic_organicohq;
            colorRes = Color.parseColor("#8D6E63");
        }
        else if (tipoLower.contains("plastica") || tipoLower.contains("lattine")) {
            iconRes = R.drawable.ic_plasticahq;
            colorRes = Color.parseColor("#1976D2"); // Blu
        }
        else if (tipoLower.contains("carta") || tipoLower.contains("cartone")) {
            iconRes = R.drawable.ic_cartahq;
            colorRes = Color.parseColor("#FFC107"); // Giallo
        }
        else if (tipoLower.contains("vetro")) {
            iconRes = R.drawable.ic_vetrohq; // Assicurati di avere ic_vetrohq
            colorRes = R.color.waste_glass; // Verde scuro
        }
        else if (tipoLower.contains("secco") || tipoLower.contains("indifferenziato")) {
            iconRes = R.drawable.ic_secco_indifferenziatahq; //
            colorRes = Color.GRAY;
        }

        binding.imgWasteIconCalendar.setImageResource(iconRes);
        binding.imgWasteIconCalendar.setColorFilter(colorRes);
        binding.tvWasteTimeCalendar.setTextColor(colorRes);

        // Bordo Spesso Colorato
        binding.cardWasteResult.setStrokeColor(colorRes);
        binding.cardWasteResult.setStrokeWidth(6); // Bordo più cicciotto per evidenziare

        // Sfondo Sfumato (Trasparente)
        int coloreSfondoChiaro = androidx.core.graphics.ColorUtils.setAlphaComponent(colorRes, 30);
        binding.cardWasteResult.setCardBackgroundColor(coloreSfondoChiaro);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
