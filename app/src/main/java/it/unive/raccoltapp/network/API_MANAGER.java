package it.unive.raccoltapp.network;

// Rimosso: import android.text.TextUtils;

import it.unive.raccoltapp.model.LoginCredentials;
import it.unive.raccoltapp.model.LoginResponse;
import it.unive.raccoltapp.model.SignUpCredentials;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Classe Singleton per gestire la configurazione di Retrofit e l'accesso all'API di Supabase.
 * Gestisce anche il token di autenticazione.
 */
public class API_MANAGER {

    public static final String BASE_URL = "https://thigbtdvpcnnwollsnab.supabase.co/";
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRoaWdidGR2cGNubndvbGxzbmFiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUwNTQyNzIsImV4cCI6MjA4MDYzMDI3Mn0.ht33Tp9ZVTd-R7NwWnyBhQJ0-9TE2iuFsTapcrw8qKc";

    private static API_MANAGER instance;
    private final SupabaseApiService apiService;

    // Salva il token JWT dell'utente loggato
    private String authToken = null;

    private API_MANAGER() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("apikey", API_KEY);

                    // Sostituito TextUtils.isEmpty() con codice Java puro
                    String tokenToUse = (authToken == null || authToken.isEmpty()) ? API_KEY : authToken;
                    builder.header("Authorization", "Bearer " + tokenToUse);

                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(SupabaseApiService.class);
    }

    public static synchronized API_MANAGER getInstance() {
        if (instance == null) {
            instance = new API_MANAGER();
        }
        return instance;
    }

    public SupabaseApiService getApiService() {
        return apiService;
    }

    // --- Metodi per l'autenticazione ---

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public boolean isLoggedIn() {
        // Sostituito TextUtils.isEmpty() con codice Java puro
        return authToken != null && !authToken.isEmpty();
    }

    public void logout() {
        this.authToken = null;
    }

    public void loginUser(String email, String password, Callback<LoginResponse> callback) {
        LoginCredentials credentials = new LoginCredentials(email, password);
        Call<LoginResponse> call = apiService.login(credentials, "password");

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setAuthToken(response.body().getAccessToken());
                }
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }

    public void signUpUser(String email, String password, String name, String username, Callback<LoginResponse> callback) {
        SignUpCredentials credentials = new SignUpCredentials(email, password, name, username);
        Call<LoginResponse> call = apiService.signup(credentials);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setAuthToken(response.body().getAccessToken());
                }
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }
}