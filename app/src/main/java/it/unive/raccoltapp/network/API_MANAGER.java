package it.unive.raccoltapp.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import it.unive.raccoltapp.model.Image;
import it.unive.raccoltapp.model.LoginCredentials;
import it.unive.raccoltapp.model.LoginResponse;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.model.ReportResponse;
import it.unive.raccoltapp.model.SignUpCredentials;
import it.unive.raccoltapp.model.UserInfo;
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

    private static final String PREFS_NAME = "RaccoltAppPrefs";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_INFO_ID = "userInfoId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME = "name";
    private static final String KEY_CITY = "city";

    private static API_MANAGER instance;
    private final SupabaseApiService apiService;
    private final SharedPreferences sharedPreferences;

    private String authToken = null;
    private String userId = null;
    private String userEmail = null;
    private Long userInfoId = null;
    private String username = null;
    private String name = null;
    private String city = null;

    private API_MANAGER(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadSession();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
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

    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new API_MANAGER(context);
        }
    }

    public static synchronized API_MANAGER getInstance() {
        if (instance == null) {
            throw new IllegalStateException("API_MANAGER must be initialized in the Application class");
        }
        return instance;
    }

    public boolean isLoggedIn() { return authToken != null && !authToken.isEmpty(); }
    public String getUserEmail() { return userEmail; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getCity() { return city; } 
    public void setCity(String newCity) { 
        String oldCity = this.city;
        if (oldCity != null && !oldCity.isEmpty() && !oldCity.equals(newCity)) {
            String oldTopic = "city_" + oldCity.replaceAll("\\s+", "_");
            FirebaseMessaging.getInstance().unsubscribeFromTopic(oldTopic).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Disiscrizione dal topic " + oldTopic + " RIUSCITA.");
                } else {
                    Log.e(TAG, "Disiscrizione dal topic " + oldTopic + " FALLITA.", task.getException());
                }
            });
        }
        this.city = newCity;
        if (newCity != null && !newCity.isEmpty()) {
            String newTopic = "city_" + newCity.replaceAll("\\s+", "_");
            FirebaseMessaging.getInstance().subscribeToTopic(newTopic).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Iscrizione al topic " + newTopic + " RIUSCITA.");
                } else {
                    Log.e(TAG, "Iscrizione al topic " + newTopic + " FALLITA.", task.getException());
                }
            });
        }
        saveSession(); 
    } 
    public Long getUserInfoId() { return userInfoId; }

    public void logout() {
        if (this.city != null && !this.city.isEmpty()) {
            String topic = "city_" + this.city.replaceAll("\\s+", "_");
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Disiscrizione (logout) dal topic " + topic + " RIUSCITA.");
                } else {
                    Log.e(TAG, "Disiscrizione (logout) dal topic " + topic + " FALLITA.", task.getException());
                }
            });
        }
        this.authToken = null; this.userId = null; this.userEmail = null;
        this.userInfoId = null; this.username = null; this.name = null; this.city = null;
        clearSession();
    }

    private void saveSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putLong(KEY_USER_INFO_ID, userInfoId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_CITY, city);
        editor.apply();
    }

    private void loadSession() {
        authToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null);
        userId = sharedPreferences.getString(KEY_USER_ID, null);
        userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
        userInfoId = sharedPreferences.getLong(KEY_USER_INFO_ID, 0);
        username = sharedPreferences.getString(KEY_USERNAME, null);
        name = sharedPreferences.getString(KEY_NAME, null);
        city = sharedPreferences.getString(KEY_CITY, null);
    }

    private void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
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

    public void signUpUser(String email, String password, String name, String username, String city, Callback<LoginResponse> callback) {
        apiService.signup(new SignUpCredentials(email, password, name, username, city)).enqueue(callback);
    }

    public void updateUserCity(String newCity, Callback<Void> callback) {
        if (userInfoId == 0) {
            callback.onFailure(null, new IllegalStateException("ID utente non disponibile"));
            return;
        }
        Map<String, String> cityUpdate = new HashMap<>();
        cityUpdate.put("city", newCity);
        apiService.updateUserCity("eq." + userInfoId, cityUpdate).enqueue(callback);
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
                        setCity(userInfo.getCity()); // Usa il nuovo metodo setCity per iscrivere al topic
                        
                        saveSession();
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
    public void getReportImages(String reportId, Callback<List<Report>> callback) { apiService.getReportImages(reportId).enqueue(callback); }
    public void createReport(Report report, Callback<List<ReportResponse>> callback) { apiService.createReport(report).enqueue(callback); }
    public void uploadImage(Image image, Callback<Void> callback) { apiService.uploadImage(image).enqueue(callback); }
}
