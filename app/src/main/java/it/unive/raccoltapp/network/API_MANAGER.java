package it.unive.raccoltapp.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import it.unive.raccoltapp.model.LoginCredentials;
import it.unive.raccoltapp.model.LoginResponse;
import it.unive.raccoltapp.model.Report;
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

public class API_MANAGER {

    private static final String TAG = "API_MANAGER";
    public static final String BASE_URL = "https://thigbtdvpcnnwollsnab.supabase.co/";
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRoaWdidGR2cGNubndvbGxzbmFiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUwNTQyNzIsImV4cCI6MjA4MDYzMDI3Mn0.ht33Tp9ZVTd-R7NwWnyBhQJ0-9TE2iuFsTapcrw8qKc";

    private static API_MANAGER instance;
    private final SupabaseApiService apiService;

    // Dati utente in memoria
    private String authToken = null;
    private String userId = null;
    private String userEmail = null;
    private Long userInfoId = null;
    private String username = null;
    private String name = null;

    private API_MANAGER() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder().header("apikey", API_KEY);
                    String tokenToUse = (authToken == null || authToken.isEmpty()) ? API_KEY : authToken;
                    builder.header("Authorization", "Bearer " + tokenToUse);
                    return chain.proceed(builder.build());
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

    // --- Getters & User Session ---
    public boolean isLoggedIn() { return authToken != null && !authToken.isEmpty(); }
    public String getUserEmail() { return userEmail; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public Long getUserInfoId() { return userInfoId; }

    public void logout() {
        this.authToken = null; this.userId = null; this.userEmail = null;
        this.userInfoId = null; this.username = null; this.name = null;
    }

    private void handleSuccessfulAuth(LoginResponse authResponse, Callback<LoginResponse> originalCallback, Call<LoginResponse> originalCall) {
        authToken = authResponse.getAccessToken();
        if (authResponse.getUser() != null) {
            userId = authResponse.getUser().getId();
            userEmail = authResponse.getUser().getEmail();
            fetchAndSetUserInfo(originalCallback, originalCall, authResponse);
        } else {
            originalCallback.onResponse(originalCall, Response.success(authResponse));
        }
    }

    public void loginUser(String email, String password, Callback<LoginResponse> callback) {
        apiService.login(new LoginCredentials(email, password), "password").enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulAuth(response.body(), callback, call);
                } else {
                    callback.onResponse(call, response);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) { callback.onFailure(call, t); }
        });
    }

    public void signUpUser(String email, String password, String name, String username, Callback<LoginResponse> callback) {
        apiService.signup(new SignUpCredentials(email, password, name, username)).enqueue(callback);
    }

    private void fetchAndSetUserInfo(Callback<LoginResponse> finalCallback, Call<LoginResponse> originalCall, LoginResponse originalAuthResponse) {
        if (userId == null) {
            finalCallback.onFailure(originalCall, new IllegalStateException("User ID (UUID) è nullo"));
            return;
        }
        final int MAX_RETRIES = 3;
        final int DELAY_MS = 1000;
        final int[] retryCount = {0};

        final Runnable[] fetchTaskHolder = new Runnable[1];
        Runnable fetchTask = () -> {
            String filter = "eq." + userId;
            apiService.getUserInfo(filter).enqueue(new Callback<List<UserInfo>>() {
                @Override
                public void onResponse(Call<List<UserInfo>> call, Response<List<UserInfo>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        UserInfo userInfo = response.body().get(0);
                        userInfoId = userInfo.getId();
                        username = userInfo.getUsername();
                        name = userInfo.getName();
                        Log.d(TAG, "Login Fase 2 OK. UserInfoID (numerico): " + userInfoId);
                        finalCallback.onResponse(originalCall, Response.success(originalAuthResponse));
                    } else {
                        if (retryCount[0] < MAX_RETRIES) {
                            retryCount[0]++;
                            Log.w(TAG, "Dati utente non ancora pronti. Tentativo " + retryCount[0] + ".");
                            new Handler(Looper.getMainLooper()).postDelayed(fetchTaskHolder[0], DELAY_MS);
                        } else {
                            String errorMsg = "Impossibile trovare i dati utente associati dopo " + MAX_RETRIES + " tentativi.";
                            Log.e(TAG, errorMsg);
                            finalCallback.onFailure(originalCall, new IOException(errorMsg));
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<UserInfo>> call, Throwable t) {
                    finalCallback.onFailure(originalCall, t);
                }
            });
        };
        fetchTaskHolder[0] = fetchTask;
        new Handler(Looper.getMainLooper()).post(fetchTask);
    }

    public void getReports(Callback<List<Report>> callback) { apiService.getReports().enqueue(callback); }
    public void createReport(Report report, Callback<Void> callback) { apiService.createReport(report).enqueue(callback); }
}
