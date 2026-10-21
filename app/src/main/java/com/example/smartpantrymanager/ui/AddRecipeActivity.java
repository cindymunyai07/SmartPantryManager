package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.logic.IngredientUnits;
import com.example.smartpantrymanager.logic.NameNormalizer;
import com.example.smartpantrymanager.logic.UnitConverter;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Add Recipe screen: lets the user create their own recipe.
 * Ingredient names and units go through the same NameNormalizer and
 * UnitConverter as pantry items, so strict matching works for custom recipes too.
 */
public class AddRecipeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextInputLayout layoutName, layoutSteps;
    private TextInputEditText editName, editSteps;
    private LinearLayout containerIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_recipe);

        // Keep content clear of the status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addRecipeRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        layoutName = findViewById(R.id.layoutRecipeName);
        layoutSteps = findViewById(R.id.layoutRecipeSteps);
        editName = findViewById(R.id.editRecipeName);
        editSteps = findViewById(R.id.editRecipeSteps);
        containerIngredients = findViewById(R.id.containerIngredients);

        findViewById(R.id.buttonAddIngredientRow).setOnClickListener(v -> addIngredientRow());
        findViewById(R.id.buttonSaveRecipe).setOnClickListener(v -> saveRecipe());

        addIngredientRow();   // start with one empty ingredient row
    }

    /** Adds one empty ingredient row (name, quantity, unit) to the form. */
    private void addIngredientRow() {
        View row = getLayoutInflater().inflate(R.layout.item_ingredient_input,
                containerIngredients, false);

        Spinner spinner = row.findViewById(R.id.spinnerIngUnit);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UnitConverter.UNITS));

        row.findViewById(R.id.buttonRemoveRow).setOnClickListener(v -> {
            if (containerIngredients.getChildCount() > 1) {
                containerIngredients.removeView(row);
            } else {
                Toast.makeText(this, "A recipe needs at least one ingredient",
                        Toast.LENGTH_SHORT).show();
            }
        });

        containerIngredients.addView(row);
    }

    /** Validates the whole form and, if it is correct, saves the recipe. */
    private void saveRecipe() {
        String name = getText(editName);
        String steps = getText(editSteps);
        boolean valid = true;

        // Validate the recipe name
        if (name.isEmpty()) {
            layoutName.setError("Please enter a recipe name");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            layoutName.setError("The name must contain letters");
            valid = false;
        } else if (name.length() > 60) {
            layoutName.setError("The name is too long (maximum 60 characters)");
            valid = false;
        } else {
            layoutName.setError(null);
        }

        // Validate the steps
        if (steps.isEmpty()) {
            layoutSteps.setError("Please enter the preparation steps");
            valid = false;
        } else {
            layoutSteps.setError(null);
        }

        // Validate every ingredient row
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < containerIngredients.getChildCount(); i++) {
            View row = containerIngredients.getChildAt(i);
            EditText editIngName = row.findViewById(R.id.editIngName);
            EditText editIngQuantity = row.findViewById(R.id.editIngQuantity);
            Spinner spinnerUnit = row.findViewById(R.id.spinnerIngUnit);

            String ingName = editIngName.getText().toString().trim();
            String quantityText = editIngQuantity.getText().toString().trim();
            String unit = spinnerUnit.getSelectedItem().toString();
            boolean rowValid = true;

            if (ingName.isEmpty() || !ingName.matches(".*[A-Za-z].*")) {
                editIngName.setError("Enter an ingredient name");
                rowValid = false;
            }

            double quantity = 0;
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    editIngQuantity.setError("Must be above 0");
                    rowValid = false;
                }
            } catch (NumberFormatException e) {
                editIngQuantity.setError("Enter a number");
                rowValid = false;
            }

            if (rowValid) {
                String normalized = NameNormalizer.normalize(ingName);
                if (!seen.add(normalized)) {
                    editIngName.setError("Already added");
                    rowValid = false;
                } else {
                    // Same unit check as the pantry form
                    String expected = dbHelper.getRecipeUnitForIngredient(normalized);
                    if (expected == null) {
                        expected = IngredientUnits.expectedBaseUnit(normalized);
                    }
                    if (expected != null && !expected.equals(UnitConverter.toBaseUnit(unit))) {
                        editIngName.setError(ingName + " is measured in " + describeUnit(expected));
                        rowValid = false;
                    } else {
                        ingredients.add(new RecipeIngredient(0, ingName, normalized,
                                UnitConverter.toBaseQuantity(quantity, unit),
                                UnitConverter.toBaseUnit(unit)));
                    }
                }
            }
            if (!rowValid) {
                valid = false;
            }
        }

        if (!valid) {
            return;   // stay on the screen so the user can fix the errors
        }

        Recipe recipe = new Recipe(name, steps);
        recipe.setIngredients(ingredients);
        long id = dbHelper.addRecipe(recipe);   // CREATE

        if (id == -1) {
            Toast.makeText(this, "Could not save the recipe", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Recipe saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /** Describes a base unit in friendly words for the error message. */
    private String describeUnit(String baseUnit) {
        switch (baseUnit) {
            case "g":  return "weight (g or kg)";
            case "ml": return "volume (ml, l, cup, tbsp or tsp)";
            default:   return "pieces (piece)";
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}