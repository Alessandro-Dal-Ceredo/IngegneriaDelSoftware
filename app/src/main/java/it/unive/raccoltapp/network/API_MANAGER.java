package it.unive.raccoltapp.network;

// Rimosso: import android.text.TextUtils;

import it.unive.raccoltapp.model.LoginCredentials;
import it.unive.raccoltapp.model.LoginResponse;
import it.unive.raccoltapp.model.SignUpCredentials;
import it.unive.raccoltapp.model.UserInfo;

import java.util.List;
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

    // Salva il token JWT e i dati dell'utente loggato
    private String authToken = null;
    private String userId = null;
    private String userEmail = null;

    private API_MANAGER() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("apikey", API_KEY);

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

    // --- Metodi per l'autenticazione e dati utente ---

    public void setAuthToken(String token, String id, String email) {
        this.authToken = token;
        this.userId = id;
        this.userEmail = email;
    }

    public boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }

    public void logout() {
        this.authToken = null;
        this.userId = null;
        this.userEmail = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void loginUser(String email, String password, Callback<LoginResponse> callback) {
        LoginCredentials credentials = new LoginCredentials(email, password);
        Call<LoginResponse> call = apiService.login(credentials, "password");

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    setAuthToken(loginResponse.getAccessToken(), loginResponse.getUser().getId(), loginResponse.getUser().getEmail());
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
                    // Dopo la registrazione, salviamo i dati base
                    LoginResponse signUpResponse = response.body();
                    setAuthToken(signUpResponse.getAccessToken(), signUpResponse.getUser().getId(), signUpResponse.getUser().getEmail());
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

    public void getUserInfo(Callback<List<UserInfo>> callback) {
        if (userId == null) {
            // Gestisci il caso in cui l'ID utente non è disponibile
            callback.onFailure(null, new IllegalStateException("User not logged in"));
            return;
        }
        // il 'eq.' serve a Retrofit per fare il filtro corretto
        String filter = "eq." + userId;
        Call<List<UserInfo>> call = apiService.getUserInfo(filter);
        call.enqueue(callback);
    }
}
