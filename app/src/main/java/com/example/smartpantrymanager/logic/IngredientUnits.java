package com.example.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Common ingredients that are not used in the seeded recipes, with the kind of
 * unit they are normally measured in ("g" = weight, "ml" = volume, "piece" = count).
 * Used as a fallback by the unit validation on the Add/Edit and Add Recipe screens.
 */
public class IngredientUnits {

    private static final Map<String, String> EXPECTED = new HashMap<>();

    static {
        // Weight
        for (String name : new String[]{"salt", "black pepper", "cinnamon", "yeast",
                "baking powder", "baking soda", "cocoa", "beef", "mince", "lamb",
                "bacon", "sausage", "lentil", "bean", "chickpea", "spinach",
                "broccoli", "mushroom", "cabbage", "yoghurt", "flour", "sugar"}) {
            EXPECTED.put(name, "g");
        }
        // Volume
        for (String name : new String[]{"oil", "olive oil", "vegetable oil", "vinegar",
                "water", "juice", "cream", "stock", "wine", "coconut milk"}) {
            EXPECTED.put(name, "ml");
        }
        // Count
        for (String name : new String[]{"lemon", "apple", "orange", "avocado",
                "cucumber", "pepper", "chilli", "lime", "pear"}) {
            EXPECTED.put(name, "piece");
        }
    }

    private IngredientUnits() { }

    /** Returns "g", "ml" or "piece" for a known ingredient, or null if unknown. */
    public static String expectedBaseUnit(String normalizedName) {
        return EXPECTED.get(normalizedName);
    }
}