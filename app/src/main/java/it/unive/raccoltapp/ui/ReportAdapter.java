package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.model.TypeOfReport;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;
    private List<Report> reportListFull;


    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
        this.reportListFull = new ArrayList<>(reportList);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.city.setText(report.getCity());
        holder.street.setText(report.getStreet());
        int colorCode = android.graphics.Color.GRAY; // Colore di default

        if (report.getPriority() != null) {
            // Usa gli stessi colori che hai scelto per il dettaglio
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
            }
        }

        // Applica il colore al bordo della card di QUESTA riga specifica
        if (holder.cardView != null) {
            holder.cardView.setStrokeColor(colorCode);
            holder.priority.setTextColor(colorCode);
            holder.priority.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (report.getType() != null && holder.iconView != null) {
            int iconResId;

            // Assumiamo che il tuo Enum si chiami TypeOfReport
            switch (report.getType()) {
                case METEO_ALERT:
                    iconResId = R.drawable.ic_meteo_alert;
                    break;// Usa i TUOI nomi file
                case ABANDONED_WASTE:
                    iconResId = R.drawable.ic_abandoned_waste; // Usa i TUOI nomi file
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
                default: // Caso OTHER o tipi non gestiti
                    iconResId = R.drawable.ic_waste_default;
                    break;
            }
            // Imposta l'immagine
            holder.iconView.setImageResource(iconResId);
        }

        if(report.getDate() != null)
            holder.date.setText(report.getDate());

        if(report.getType() != null)
            holder.type.setText(report.getType().toString());

        if (report.getPriority() != null) {
            holder.priority.setText(report.getPriority().toString());
        } else {
            holder.priority.setText("N/D");
        }

        // Naviga alla pagina di dettaglio al click
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("report", report);
            Navigation.findNavController(v).navigate(R.id.action_reportsFragment_to_reportDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void filter(String city, String priority, String type) {
        android.util.Log.d("FILTRO_DEBUG", "Cercando: " + city + " | " + priority + " | " + type);

        reportList.clear();

        if (reportListFull == null || reportListFull.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        for (Report report : reportListFull) {
            // CHECK 1: Città (Default: "Comuni")
            boolean matchCity = city.equals("Comuni") ||
                    (report.getCity() != null && report.getCity().equalsIgnoreCase(city));

            // CHECK 2: Priorità (Default: "Priorità")
            boolean matchPriority = priority.equals("Priorità") ||
                    (report.getPriority() != null && report.getPriority().toString().equalsIgnoreCase(priority));

            // CHECK 3: Tipo (Default: "Tipo")
            boolean matchType = type.equals("Tipo") ||
                    (report.getType() != null && report.getType().toString().equalsIgnoreCase(type));


            // LOGICA AND: Passa solo se TUTTI e 3 sono veri
            if (matchCity && matchPriority && matchType) {
                reportList.add(report);
            }
        }
        notifyDataSetChanged();
    }

    // Metodo per aggiornare i dati dall'esterno
    public void updateData(List<Report> newReports) {
        this.reportList.clear();
        this.reportList.addAll(newReports);
        this.reportListFull = new ArrayList<>(newReports);
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView city, street, priority, date, type;
        ImageView iconView;
        com.google.android.material.card.MaterialCardView cardView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            city = itemView.findViewById(R.id.tv_report_city);
            street = itemView.findViewById(R.id.tv_report_street);
            priority = itemView.findViewById(R.id.tv_report_priority);
            date = itemView.findViewById(R.id.tv_report_date);
            type = itemView.findViewById(R.id.tv_report_type);
            cardView = itemView.findViewById(R.id.card_item_container);
            iconView = itemView.findViewById(R.id.iv_report_icon);
        }
    }
}
