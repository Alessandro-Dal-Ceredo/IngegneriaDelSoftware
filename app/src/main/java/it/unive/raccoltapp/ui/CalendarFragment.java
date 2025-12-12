package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.unive.raccoltapp.R;

public class CalendarFragment extends Fragment {

    public CalendarFragment() {
        // Costruttore vuoto richiesto
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Collegato al layout fragment_calendar.xml
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }
}
