package it.unive.raccoltapp.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unive.raccoltapp.network.API_MANAGER;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalendarManager {

    private static final String TAG = "CalendarManager"; // Tag per il logging
    private static volatile CalendarManager instance;
    private final List<String> comuniList = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();

    private CalendarManager() {}

    public static CalendarManager getInstance() {
        if (instance == null) {
            synchronized (CalendarManager.class) {
                if (instance == null) {
                    instance = new CalendarManager();
                }
            }
        }
        return instance;
    }

    public interface OnComuniReadyCallback {
        void onComuniReady(List<String> comuni);
        void onError(Exception e);
    }

    public void fetchComuniFromSupabase(OnComuniReadyCallback callback) {
        if (!comuniList.isEmpty()) {
            callback.onComuniReady(comuniList);
            return;
        }

        executor.execute(() -> {
            String url = API_MANAGER.BASE_URL + "rest/v1/rpc/get_comuni_list";
            RequestBody body = RequestBody.create(MediaType.get("application/json"), "{}");

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", API_MANAGER.API_KEY)
                    .addHeader("Authorization", "Bearer " + API_MANAGER.API_KEY)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String jsonString = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Risposta da Supabase (Comuni): HTTP " + response.code() + ", Body: " + jsonString);

                if (!response.isSuccessful()) {
                    throw new IOException("Errore server nel recupero comuni: " + response.code() + " - " + jsonString);
                }

                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> downloadedComuni = new Gson().fromJson(jsonString, listType);

                if (downloadedComuni == null) {
                    throw new IOException("La lista dei comuni ricevuta è nulla o malformata.");
                }

                comuniList.clear();
                comuniList.addAll(downloadedComuni);

                new Handler(Looper.getMainLooper()).post(() -> callback.onComuniReady(comuniList));

            } catch (IOException | JsonSyntaxException e) {
                Log.e(TAG, "Errore durante il fetch dei comuni", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    private static final String NOME_FILE_LOCALE_PREFIX = "calendario_raccolta_locale_";
    private String comuneSelezionato = "treviso"; // default

    public void setComune(String comune) {
        this.comuneSelezionato = comune;
    }

    private String getUrlServer() {
        return API_MANAGER.BASE_URL + "storage/v1/object/public/calendari_rifiuti/" + comuneSelezionato.toLowerCase() + ".json";
    }

    private String getNomeFileLocale() {
        return NOME_FILE_LOCALE_PREFIX + comuneSelezionato.toLowerCase() + ".json";
    }

    public List<RaccoltaGiorno> leggiCalendarioLocale(Context context) {
        InputStream inputStream = null;
        try {
            File file = new File(context.getFilesDir(), getNomeFileLocale());
            if (file.exists() && file.length() > 0) {
                Log.d(TAG, "Leggo calendario da file locale: " + file.getAbsolutePath());
                inputStream = new FileInputStream(file);
            } else {
                Log.d(TAG, "File locale non trovato, uso asset di backup.");
                inputStream = context.getAssets().open("calendario_rifiuti_2026.json");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<RaccoltaGiorno>>() {}.getType();
            return gson.fromJson(jsonString, listType);

        } catch (IOException | JsonSyntaxException e) {
            Log.e(TAG, "Errore durante la lettura del calendario", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Errore nella chiusura dello stream", e);
                }
            }
        }
        return null;
    }

    public void aggiornaDaServer(Context context, Runnable onSuccess) {
        executor.execute(() -> {
            String url = getUrlServer();
            Log.d(TAG, "Provo a scaricare il calendario da: " + url);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", API_MANAGER.API_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Errore server scaricamento calendario: " + response.code());
                    return;
                }

                File fileOutput = new File(context.getFilesDir(), getNomeFileLocale());
                try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
                    fos.write(response.body().bytes());
                }

                Log.d(TAG, "Calendario aggiornato con successo: " + fileOutput.getAbsolutePath());

                // FIX: Esegui il callback sul thread principale (UI Thread)
                if (onSuccess != null) {
                    new Handler(Looper.getMainLooper()).post(onSuccess);
                }

            } catch (IOException e) {
                Log.e(TAG, "Errore durante il download del calendario", e);
            }
        });
    }
}
