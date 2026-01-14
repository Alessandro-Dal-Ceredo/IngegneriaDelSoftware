package it.unive.raccoltapp.network;

import it.unive.raccoltapp.model.Image;
import it.unive.raccoltapp.model.LoginCredentials;
import it.unive.raccoltapp.model.LoginResponse;
import it.unive.raccoltapp.model.Report;
import it.unive.raccoltapp.model.ReportResponse;
import it.unive.raccoltapp.model.SignUpCredentials;
import it.unive.raccoltapp.model.UserInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApiService {

    // --- Autenticazione ---
    @POST("auth/v1/token")
    Call<LoginResponse> login(@Body LoginCredentials credentials, @Query("grant_type") String grantType);

    @POST("auth/v1/signup")
    Call<LoginResponse> signup(@Body SignUpCredentials credentials);

    // --- Dati Utente ---
    @GET("rest/v1/users_info?select=id,name,username") // FIX: Aggiunto 'id'
    Call<List<UserInfo>> getUserInfo(@Query("id_user") String userId);


    // --- Segnalazioni (Reports) ---
    @GET("rest/v1/reports?select=*,users_info(username),images(id,image)&order=id.desc")
    Call<List<Report>> getReports();

    @POST("rest/v1/reports")
    @Headers("Prefer: return=representation")
    Call<List<ReportResponse>> createReport(@Body Report report);

    @POST("rest/v1/images")
    @Headers("Prefer: return=minimal")
    Call<Void> uploadImage(@Body Image image);

}
