package com.example.smartpantrymanager;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.smartpantrymanager.ui.PantryFragment;
import com.example.smartpantrymanager.ui.SettingsFragment;
import com.example.smartpantrymanager.ui.SuggestedRecipesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Host activity: holds one screen (fragment) at a time and switches
 * between them using the bottom navigation bar.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Keep content clear of the status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Switch screens when a bottom navigation item is tapped
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                fragment = new PantryFragment();
            } else if (id == R.id.nav_suggested) {
                fragment = new SuggestedRecipesFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            } else {
                return false;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        });

        // Start on the pantry list the first time the activity is created
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_pantry);
        }
    }
}