package it.unive.raccoltapp.network;

import it.unive.raccoltapp.model.LoginCredentials;
import it.unive.raccoltapp.model.LoginResponse;
import it.unive.raccoltapp.model.Post;
import it.unive.raccoltapp.model.SignUpCredentials;
import it.unive.raccoltapp.model.User;
import it.unive.raccoltapp.model.UserInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Interfaccia per Retrofit che definisce gli endpoint dell'API di Supabase.
 */
public interface SupabaseApiService {

    // --- Endpoint di Autenticazione ---

    /**
     * Esegue il login di un utente tramite email e password.
     */
    @POST("auth/v1/token")
    Call<LoginResponse> login(@Body LoginCredentials credentials, @Query("grant_type") String grantType);

    /**
     * Registra un nuovo utente.
     */
    @POST("auth/v1/signup")
    Call<LoginResponse> signup(@Body SignUpCredentials credentials);

    // --- Endpoint per i dati dell'utente ---

    /**
     * Recupera le informazioni del profilo dell'utente corrente da users_info.
     * Il filtro 'select=name,username' specifica quali colonne prendere.
     * Il filtro 'id_user=eq.{user_id}' verra' aggiunto in API_MANAGER.
     */
    @GET("rest/v1/users_info?select=name,username")
    Call<List<UserInfo>> getUserInfo(@Query("id_user") String userId);


    // --- Endpoint per i Post ---

    @GET("rest/v1/post?order=id.asc")
    Call<List<Post>> getPosts();

    @POST("rest/v1/post")
    @Headers("Prefer: return=representation")
    Call<List<Post>> createPost(@Body Post post);


    // --- Endpoint per gli User ---

    @GET("rest/v1/users?order=id.asc")
    Call<List<User>> getUsers();

    @POST("rest/v1/users")
    @Headers("Prefer: return=representation")
    Call<List<User>> createUser(@Body User user);

}
