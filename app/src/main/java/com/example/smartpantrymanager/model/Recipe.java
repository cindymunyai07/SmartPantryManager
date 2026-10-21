package com.example.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe with its preparation steps and required ingredients.
 * Maps to a row in the recipe table; the ingredients come from recipe_ingredient.
 */
public class Recipe {

    private long id;
    private String name;
    private String steps;                       // simple preparation method
    private boolean custom;                     // true if the user created this recipe
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() { }

    public Recipe(String name, String steps) {
        this.name = name;
        this.steps = steps;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public boolean isCustom() { return custom; }
    public void setCustom(boolean custom) { this.custom = custom; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}