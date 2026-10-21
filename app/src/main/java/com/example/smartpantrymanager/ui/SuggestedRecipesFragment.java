package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.Recipe;

import java.util.List;

/**
 * Suggested Recipes screen: runs the strict-matching rule against the
 * current pantry and lists ONLY the recipes the user can make right now.
 * Also lets the user open the Add Recipe screen.
 */
public class SuggestedRecipesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private TextView textNoMatches;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggested, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        textNoMatches = view.findViewById(R.id.textNoMatches);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerSuggested);
        adapter = new RecipeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Open the Add Recipe screen with an Intent
        view.findViewById(R.id.fabAddRecipe).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddRecipeActivity.class)));

        return view;
    }

    /**
     * Re-run the matching every time the screen is shown, so it reflects pantry and recipe changes.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<Recipe> matches = RecipeMatcher.findStrictMatches(
                dbHelper.getAllRecipes(), dbHelper.getAllPantryItems());
        adapter.setRecipes(matches);
        textNoMatches.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Tapping a recipe opens the detail screen and passes the recipe id in the Intent.
     */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbHelper.close();
    }
}