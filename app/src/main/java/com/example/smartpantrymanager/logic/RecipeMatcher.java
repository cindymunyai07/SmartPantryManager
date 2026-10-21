package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict-matching rule: a recipe is suggested ONLY if every single ingredient
 * it needs is in the pantry, in at least the required quantity.
 * One missing or insufficient ingredient excludes the whole recipe.
 */
public class RecipeMatcher {

    private static final double EPSILON = 1e-9;   // avoids tiny decimal rounding problems

    private RecipeMatcher() {
    }

    /**
     * Returns only the recipes the user can make right now with their current pantry.
     */
    public static List<Recipe> findStrictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> stock = buildStock(pantry);

        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, stock)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /**
     * Totals the pantry by "normalized name + base unit", e.g. "egg|piece" -> 6.
     */
    private static Map<String, Double> buildStock(List<PantryItem> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = key(item.getNormalizedName(), item.getUnit());
            double current = stock.containsKey(key) ? stock.get(key) : 0;
            stock.put(key, current + item.getQuantity());   // duplicates are added together
        }
        return stock;
    }

    /**
     * True only if EVERY ingredient is present in at least the required quantity.
     */
    private static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        if (recipe.getIngredients().isEmpty()) {
            return false;   // a recipe with no ingredients is never suggested
        }
        for (RecipeIngredient needed : recipe.getIngredients()) {
            Double have = stock.get(key(needed.getNormalizedName(), needed.getUnit()));
            if (have == null || have + EPSILON < needed.getQuantity()) {
                return false;   // missing or not enough: the whole recipe is excluded
            }
        }
        return true;
    }

    private static String key(String normalizedName, String unit) {
        return normalizedName + "|" + unit;
    }
}