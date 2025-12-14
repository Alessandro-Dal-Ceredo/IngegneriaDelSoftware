package it.unive.raccoltapp.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unive.raccoltapp.network.API_MANAGER;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CalendarManager {

    private static final String NOME_FILE_LOCALE = "calendario_raccolta_locale.json";
    private static final String URL_SERVER = API_MANAGER.BASE_URL + "storage/v1/object/public/calendari_rifiuti/treviso.json";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();

    // Legge il calendario in modo robusto
    public List<RaccoltaGiorno> leggiCalendarioLocale(Context context) {
        InputStream inputStream = null;
        try {
            // Priorità 1: Cerca il file scaricato dal server
            File file = new File(context.getFilesDir(), NOME_FILE_LOCALE);
            if (file.exists() && file.length() > 0) {
                inputStream = new FileInputStream(file);
            } else {
                // Priorità 2: Se non esiste, usa il file di backup negli assets
                inputStream = context.getAssets().open("calendario_rifiuti_2026.json");
            }

            // Metodo di lettura robusto
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();

            // Parsing del JSON con gestione degli errori
            Gson gson = new Gson();
            Type listType = new TypeToken<List<RaccoltaGiorno>>() {}.getType();
            return gson.fromJson(jsonString, listType);

        } catch (IOException e) {
            Log.e("CalendarManager", "Errore IO durante la lettura del calendario", e);
        } catch (JsonSyntaxException e) {
            Log.e("CalendarManager", "Errore di sintassi nel file JSON del calendario", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("CalendarManager", "Errore nella chiusura dello stream", e);
                }
            }
        }
        return null; // Ritorna null se qualcosa è andato storto
    }

    // Scarica l'aggiornamento da Supabase in background
    public void aggiornaDaServer(Context context, Runnable onSuccess) {
        executor.execute(() -> {
            Request request = new Request.Builder()
                    .url(URL_SERVER)
                    .addHeader("apikey", API_MANAGER.API_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("CalendarManager", "Errore server: " + response.code());
                    return;
                }

                File fileOutput = new File(context.getFilesDir(), NOME_FILE_LOCALE);
                try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
                    fos.write(response.body().bytes());
                }

                Log.d("CalendarManager", "Calendario aggiornato con successo.");

                if (onSuccess != null) {
                    onSuccess.run();
                }

            } catch (IOException e) {
                Log.e("CalendarManager", "Errore durante il download del calendario: " + e.getMessage());
            }
        });
    }
}
