package com.example.smartpantrymanager.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Cleans ingredient names so that trivial differences do not break matching.
 * "Tomatoes " -> "tomato", "EGGS" -> "egg", "Cherry Tomatoes" -> "cherry tomato".
 * Used for BOTH pantry items and recipe ingredients, so they always compare equally.
 */
public class NameNormalizer {

    // Words that end in "s" but are not plurals
    private static final Set<String> KEEP_AS_IS = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "lemongrass", "watercress"));

    private NameNormalizer() { }

    /** Returns the normalized form of an ingredient name. */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }

        // Lowercase, trim, and collapse repeated spaces
        String cleaned = name.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return "";
        }

        // Only the last word is made singular ("cherry tomatoes" -> "cherry tomato")
        int lastSpace = cleaned.lastIndexOf(' ');
        String prefix = lastSpace >= 0 ? cleaned.substring(0, lastSpace + 1) : "";
        String lastWord = cleaned.substring(lastSpace + 1);

        return prefix + singular(lastWord);
    }

    /** Applies simple English plural rules to one word. */
    private static String singular(String word) {
        if (word.length() <= 3 || KEEP_AS_IS.contains(word)) {
            return word;
        }
        if (word.endsWith("ies")) {                       // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes")                          // tomatoes -> tomato
                || word.endsWith("ches")                  // peaches -> peach
                || word.endsWith("shes")                  // radishes -> radish
                || word.endsWith("xes")) {                // boxes -> box
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss") && !word.endsWith("us")) {
            return word.substring(0, word.length() - 1);  // eggs -> egg
        }
        return word;
    }
}