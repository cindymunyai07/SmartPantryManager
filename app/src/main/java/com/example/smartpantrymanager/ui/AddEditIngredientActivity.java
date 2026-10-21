package com.example.smartpantrymanager.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Add/Edit Ingredient screen.
 * Opened through an Intent: with no extra it creates a new item,
 * with EXTRA_ITEM_ID it edits the existing item with that id.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Key used to pass the id of the item being edited through the Intent. */
    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private DatabaseHelper dbHelper;
    private long itemId = -1;               // -1 means "adding a new item"

    private TextInputLayout layoutName, layoutQuantity;
    private TextInputEditText editName, editQuantity, editExpiry;
    private Spinner spinnerUnit;
    private TextView textUnitError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);

        // Keep content clear of the status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addEditRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        textUnitError = findViewById(R.id.textUnitError);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonClearDate = findViewById(R.id.buttonClearDate);

        // Fill the unit dropdown
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UnitConverter.UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        // If an id was passed in the Intent, we are editing an existing item
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId >= 0) {
            ((TextView) findViewById(R.id.textScreenTitle)).setText("Edit ingredient");
            loadExistingItem();
        }

        editExpiry.setOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> editExpiry.setText(""));
        buttonSave.setOnClickListener(v -> saveItem());
    }

    /** Reads the item from the database and fills the form fields. */
    private void loadExistingItem() {
        PantryItem item = dbHelper.getPantryItem(itemId);
        if (item == null) {
            Toast.makeText(this, "Ingredient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        editName.setText(item.getName());
        editQuantity.setText(formatQuantity(item.getQuantity()));
        int position = Arrays.asList(UnitConverter.UNITS).indexOf(item.getUnit());
        spinnerUnit.setSelection(Math.max(position, 0));
        if (item.getExpiryDate() != null) {
            editExpiry.setText(item.getExpiryDate());
        }
    }

    /** Opens a calendar so the user picks a valid date instead of typing it. */
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                editExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /** Validates the form and, if everything is correct, saves it to the database. */
    private void saveItem() {
        String name = getText(editName);
        String quantityText = getText(editQuantity);
        boolean valid = true;

        // Validate the name
        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            layoutName.setError("The name must contain letters");
            valid = false;
        } else if (name.length() > 40) {
            layoutName.setError("The name is too long (maximum 40 characters)");
            valid = false;
        } else {
            layoutName.setError(null);
        }

        // Validate the quantity
        double quantity = 0;
        if (quantityText.isEmpty()) {
            layoutQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    layoutQuantity.setError("The quantity must be greater than 0");
                    valid = false;
                } else {
                    layoutQuantity.setError(null);
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        // Validate the unit against what the recipes use for this ingredient
        String chosenUnit = spinnerUnit.getSelectedItem().toString();
        String normalizedName = NameNormalizer.normalize(name);
        String expectedBase = name.isEmpty() ? null
                : dbHelper.getRecipeUnitForIngredient(normalizedName);
        if (expectedBase == null && !name.isEmpty()) {
            expectedBase = IngredientUnits.expectedBaseUnit(normalizedName);   // fallback list
        }
        if (expectedBase != null
                && !expectedBase.equals(UnitConverter.toBaseUnit(chosenUnit))) {
            textUnitError.setText(name + " is measured in "
                    + describeUnit(expectedBase) + ". Please choose a matching unit.");
            textUnitError.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            textUnitError.setVisibility(View.GONE);
        }

        if (!valid) {
            return; // stay on the screen so the user can fix the errors
        }

        // Convert to the base unit and clean the name so matching works later
        double baseQuantity = UnitConverter.toBaseQuantity(quantity, chosenUnit);
        String baseUnit = UnitConverter.toBaseUnit(chosenUnit);
        String expiry = getText(editExpiry);

        PantryItem item = new PantryItem(name, NameNormalizer.normalize(name),
                baseQuantity, baseUnit, expiry.isEmpty() ? null : expiry);

        if (itemId >= 0) {
            item.setId(itemId);
            dbHelper.updatePantryItem(item);      // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addPantryItem(item);         // CREATE
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        }
        finish(); // go back to the pantry list
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

    /** Shows 2 instead of 2.0 when the quantity is a whole number. */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}