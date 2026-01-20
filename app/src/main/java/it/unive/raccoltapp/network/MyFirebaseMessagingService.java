package it.unive.raccoltapp.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import it.unive.raccoltapp.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Chiamato quando un messaggio viene ricevuto mentre l'app è in primo piano.
     * @param remoteMessage Oggetto che rappresenta il messaggio ricevuto da FCM.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Log per confermare la ricezione di un messaggio
        Log.d(TAG, "-- NUOVO MESSAGGIO RICEVUTO --");
        Log.d(TAG, "ID Messaggio: " + remoteMessage.getMessageId());
        Log.d(TAG, "Da: " + remoteMessage.getFrom());

        // Controlla se il messaggio contiene una notifica e la mostra.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Payload Notifica: " + title + " | " + body);
            sendNotification(title, body);
        } else {
            Log.d(TAG, "Questo messaggio non contiene un payload di notifica.");
        }
    }

    /**
     * Chiamato se l'istanza di FCM genera un nuovo token. 
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e(TAG, "NUOVO TOKEN GENERATO: " + token);
        // Con l'approccio basato sui topic, non abbiamo bisogno di salvare il token manualmente.
    }


    /**
     * Crea e mostra una notifica semplice.
     * @param messageTitle Titolo della notifica.
     * @param messageBody Corpo della notifica.
     */
    private void sendNotification(String messageTitle, String messageBody) {
        Log.d(TAG, "Tentativo di creare e mostrare la notifica.");
        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Necessario da Android 8.0 (API 26) in poi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Notifiche Generali", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Canale di notifica creato/aggiornato.");
        }

        notificationManager.notify(0, notificationBuilder.build());
        Log.d(TAG, "Notifica inviata al sistema.");
    }
}
