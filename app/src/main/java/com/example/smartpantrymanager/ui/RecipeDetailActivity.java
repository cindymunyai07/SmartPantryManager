package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

/**
 * Recipe Detail screen: shows the full ingredient list and method of one recipe.
 * The recipe id arrives through an Intent extra. Recipes the user created
 * also get a delete button; the built-in recipes do not.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key used to pass the recipe id through the Intent. */
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        // Keep content clear of the status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recipeRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        // Read the recipe id that was passed in the Intent
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Recipe recipe = dbHelper.getRecipe(recipeId);
        dbHelper.close();

        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.textRecipeTitle)).setText(recipe.getName());
        ((TextView) findViewById(R.id.textSteps)).setText(recipe.getSteps());

        // Build the ingredient list, one line per ingredient
        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            ingredients.append("\u2022 ")
                    .append(formatQuantity(ing.getQuantity()))
                    .append(" ")
                    .append(ing.getUnit())
                    .append(" ")
                    .append(ing.getName())
                    .append("\n");
        }
        ((TextView) findViewById(R.id.textIngredients)).setText(ingredients.toString().trim());

        // Only recipes the user created can be deleted
        Button buttonDelete = findViewById(R.id.buttonDeleteRecipe);
        if (recipe.isCustom()) {
            buttonDelete.setVisibility(View.VISIBLE);
            buttonDelete.setOnClickListener(v -> confirmDelete(recipe));
        }
    }

    /** Asks for confirmation, then deletes the recipe and closes the screen. */
    private void confirmDelete(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("Delete recipe")
                .setMessage("Delete \"" + recipe.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseHelper dbHelper = new DatabaseHelper(this);
                    dbHelper.deleteRecipe(recipe.getId());
                    dbHelper.close();
                    Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Shows 2 instead of 2.0 when the quantity is a whole number. */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}