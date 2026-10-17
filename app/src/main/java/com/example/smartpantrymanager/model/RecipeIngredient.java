package com.example.smartpantrymanager.model;

/**
 * One ingredient line inside a recipe, e.g. "200 g pasta".
 * Maps to a row in the recipe_ingredient table.
 */
public class RecipeIngredient {

    private long id;
    private long recipeId;          // foreign key to recipe.id
    private String name;
    private String normalizedName;
    private double quantity;
    private String unit;

    public RecipeIngredient() { }

    public RecipeIngredient(long recipeId, String name, String normalizedName,
                            double quantity, String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}