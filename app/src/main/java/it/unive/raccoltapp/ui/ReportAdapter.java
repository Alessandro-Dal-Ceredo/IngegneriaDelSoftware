package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.model.Report;

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
        holder.title.setText(report.getTitle());
        holder.city.setText(report.getCity());
        holder.street.setText(report.getStreet());

        if(report.getDate() != null)
            holder.date.setText(report.getDate());

        if(report.getType() != null)
            holder.type.setText(report.getType().toString().replace("_", " "));

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

    // Metodo per filtrare la lista
    /*
    public void filter(String city) {
        reportList.clear();
        if (city.isEmpty() || city.equals("Tutti i comuni")) {
            reportList.addAll(reportListFull);
        } else {
            for (Report report : reportListFull) {
                if (report.getCity() != null && report.getCity().equalsIgnoreCase(city)) {
                    reportList.add(report);
                }
            }
        }
        notifyDataSetChanged();
    }*/
    // Modifica la firma per accettare DUE parametri
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
            // Assumiamo che report.getType() restituisca un Enum
            boolean matchType = false;
            if (type.equals("Tipo")) { // O qual è la tua stringa di default
                matchType = true;
            } else if (report.getType() != null) {
                // 1. Prendo il tipo grezzo dal report (es. "HEAVY_WASTE")
                String rawType = report.getType().toString();

                // 2. Lo "pulisco" togliendo gli underscore, proprio come fai nel dettaglio
                String cleanType = rawType.replace("_", " ");

                // 3. Ora confronto la versione pulita con quella dello spinner
                matchType = cleanType.equalsIgnoreCase(type);
            }

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
        TextView title, city, street, priority, date, type;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_report_title);
            city = itemView.findViewById(R.id.tv_report_city);
            street = itemView.findViewById(R.id.tv_report_street);
            priority = itemView.findViewById(R.id.tv_report_priority);
            date = itemView.findViewById(R.id.tv_report_date);
            type = itemView.findViewById(R.id.tv_report_type);
        }
    }
}
