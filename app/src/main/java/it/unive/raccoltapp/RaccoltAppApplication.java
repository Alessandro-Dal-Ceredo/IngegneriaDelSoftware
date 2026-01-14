package it.unive.raccoltapp;

import android.app.Application;
import it.unive.raccoltapp.network.API_MANAGER;

public class RaccoltAppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        API_MANAGER.initialize(this);
    }
}
