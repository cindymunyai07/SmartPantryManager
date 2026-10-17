package com.example.smartpantrymanager.logic;

import java.util.Locale;

/**
 * Converts quantities to a base unit so that "1 kg" in the pantry
 * satisfies "500 g" in a recipe. Base units: g (weight), ml (volume), piece (count).
 */
public class UnitConverter {

    /** Units shown in the unit dropdown on the Add/Edit screen. */
    public static final String[] UNITS =
            {"g", "kg", "ml", "l", "cup", "tbsp", "tsp", "piece"};

    private UnitConverter() { }

    /** Converts a quantity in any supported unit into its base unit. */
    public static double toBaseQuantity(double quantity, String unit) {
        switch (clean(unit)) {
            case "kg":   return quantity * 1000;   // kilograms -> grams
            case "l":    return quantity * 1000;   // litres -> millilitres
            case "cup":  return quantity * 250;    // 1 cup = 250 ml
            case "tbsp": return quantity * 15;     // 1 tablespoon = 15 ml
            case "tsp":  return quantity * 5;      // 1 teaspoon = 5 ml
            default:     return quantity;          // g, ml, piece stay as they are
        }
    }

    /** Returns the base unit ("g", "ml" or "piece") for any supported unit. */
    public static String toBaseUnit(String unit) {
        switch (clean(unit)) {
            case "g":
            case "kg":
                return "g";
            case "ml":
            case "l":
            case "cup":
            case "tbsp":
            case "tsp":
                return "ml";
            default:
                return "piece";
        }
    }

    private static String clean(String unit) {
        return unit == null ? "" : unit.toLowerCase(Locale.ROOT).trim();
    }
}