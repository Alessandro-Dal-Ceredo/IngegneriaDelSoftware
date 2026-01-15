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

            bottomNav.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_profile) {
                    if (API_MANAGER.getInstance().isLoggedIn()) {
                        navController.navigate(R.id.nav_profile);
                    } else {
                        navController.navigate(R.id.LoginFragment);
                    }
                    return true;
                } else {
                    return NavigationUI.onNavDestinationSelected(item, navController);
                }
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                bottomNav.setVisibility(View.VISIBLE);

                if (destination.getId() == R.id.LoginFragment) {
                    bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
                } else {
                    MenuItem menuItem = bottomNav.getMenu().findItem(destination.getId());
                    if (menuItem != null) {
                        menuItem.setChecked(true);
                    }
                }
            });
        }
    }

    public void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invia Segnalazione");
        builder.setMessage("Descrivi il problema o il suggerimento che vuoi inviare.");
        builder.setPositiveButton("Invia", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }
}
