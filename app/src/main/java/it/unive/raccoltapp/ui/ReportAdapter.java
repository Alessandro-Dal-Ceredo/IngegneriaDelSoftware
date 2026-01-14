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
    }

    // Metodo per aggiornare i dati dall'esterno
    public void updateData(List<Report> newReports) {
        this.reportList.clear();
        this.reportList.addAll(newReports);
        this.reportListFull = new ArrayList<>(newReports);
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView city;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_report_title);
            city = itemView.findViewById(R.id.tv_report_city);
        }
    }
}
