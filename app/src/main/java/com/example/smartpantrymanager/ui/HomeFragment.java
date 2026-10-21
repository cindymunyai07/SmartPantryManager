package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.data.SettingsManager;
import com.example.smartpantrymanager.logic.ExpiryHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Home screen: a welcome header with live statistics from the database
 * and shortcuts to the Recipes and Pantry tabs.
 */
public class HomeFragment extends Fragment {

    private TextView textStatPantry, textStatCook, textStatExpiring, textCookSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textStatPantry = view.findViewById(R.id.textStatPantry);
        textStatCook = view.findViewById(R.id.textStatCook);
        textStatExpiring = view.findViewById(R.id.textStatExpiring);
        textCookSummary = view.findViewById(R.id.textCookSummary);

        view.findViewById(R.id.buttonSeeRecipes).setOnClickListener(v -> goToTab(R.id.nav_suggested));
        view.findViewById(R.id.buttonGoPantry).setOnClickListener(v -> goToTab(R.id.nav_pantry));

        return view;
    }

    /** Refresh the numbers every time the Home tab is shown. */
    @Override
    public void onResume() {
        super.onResume();
        refreshStats();
    }

    /** Reads the database and updates the three stat cards and the summary text. */
    private void refreshStats() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        List<PantryItem> pantry;
        List<Recipe> recipes;
        try {
            pantry = dbHelper.getAllPantryItems();
            recipes = dbHelper.getAllRecipes();
        } finally {
            dbHelper.close();
        }

        // Uses the same strict-matching rule as the Recipes tab
        int canCook = RecipeMatcher.findStrictMatches(recipes, pantry).size();

        // Count items that are expired or expire within the number of days set in Settings
        int days = new SettingsManager(requireContext()).getExpiryDays();
        int expiring = 0;
        for (PantryItem item : pantry) {
            String expiry = item.getExpiryDate();
            if (expiry != null && !expiry.isEmpty() && ExpiryHelper.daysUntil(expiry) <= days) {
                expiring++;
            }
        }

        textStatPantry.setText(String.valueOf(pantry.size()));
        textStatCook.setText(String.valueOf(canCook));
        textStatExpiring.setText(String.valueOf(expiring));

        if (canCook == 0) {
            textCookSummary.setText("No recipes match your pantry yet. Add more ingredients to get started.");
        } else {
            textCookSummary.setText("You can cook " + canCook
                    + (canCook == 1 ? " recipe" : " recipes") + " right now. No shopping needed!");
        }
    }

    /** Switches the bottom navigation bar (and the screen) to another tab. */
    private void goToTab(int menuItemId) {
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
        nav.setSelectedItemId(menuItemId);
    }
}