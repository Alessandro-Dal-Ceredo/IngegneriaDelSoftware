package it.unive.raccoltapp.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unive.raccoltapp.R;
import it.unive.raccoltapp.network.API_MANAGER;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

            // Collega la Bottom Nav, ma gestisce il clic sul profilo manualmente
            bottomNav.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_profile) {
                    if (API_MANAGER.getInstance().isLoggedIn()) {
                        // Utente loggato: vai al profilo
                        navController.navigate(R.id.nav_profile);
                    } else {
                        // Utente non loggato: vai al login
                        navController.navigate(R.id.LoginFragment);
                    }
                    return true; // Evento gestito
                } else {
                    // Per tutti gli altri item, usa il comportamento di default
                    return NavigationUI.onNavDestinationSelected(item, navController);
                }
            });

            // Nasconde/mostra la bottom nav e gestisce l'elemento selezionato
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Mostra la nav bar su tutte le schermate tranne che su SignUp
                if (destination.getId() == R.id.SignUpFragment) {
                    bottomNav.setVisibility(View.GONE);
                } else {
                    bottomNav.setVisibility(View.VISIBLE);
                }

                // Gestisce quale icona evidenziare
                if (destination.getId() == R.id.LoginFragment) {
                    // Se siamo sulla schermata di login, evidenzia l'icona del profilo
                    bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
                } else {
                    // Altrimenti, lascia che il sistema evidenzi l'icona corretta
                    MenuItem menuItem = bottomNav.getMenu().findItem(destination.getId());
                    if (menuItem != null) {
                        menuItem.setChecked(true);
                    }
                }
            });
        }
    }

    /**
     * Mostra un popup (AlertDialog) per inviare una segnalazione.
     * Questo metodo può essere chiamato da qualsiasi fragment ospitato da questa activity.
     */
    public void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invia Segnalazione");
        builder.setMessage("Descrivi il problema o il suggerimento che vuoi inviare.");

        // Per ora, un semplice dialog con un pulsante OK
        // In futuro, qui puoi inserire un layout personalizzato con EditText, etc.
        builder.setPositiveButton("Invia", (dialog, which) -> {
            // Logica per inviare la segnalazione
            dialog.dismiss();
        });
        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }
}
