package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the SQLite database, seeds the recipes on first run, and provides
 * CRUD methods for pantry items plus create/read/delete methods for recipes.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 3;   // 3 adds the is_custom column to recipe

    // pantry_item table
    public static final String TABLE_PANTRY = "pantry_item";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_NORMALIZED = "normalized_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    // recipe table
    public static final String TABLE_RECIPE = "recipe";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";
    public static final String COL_IS_CUSTOM = "is_custom";

    // recipe_ingredient table
    public static final String TABLE_RECIPE_ING = "recipe_ingredient";
    public static final String COL_RECIPE_ID = "recipe_id";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Turn on foreign keys so deleting a recipe also deletes its ingredients.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Called once, the first time the database is created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_NORMALIZED + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_NAME + " TEXT NOT NULL, "
                + COL_RECIPE_STEPS + " TEXT NOT NULL, "
                + COL_IS_CUSTOM + " INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_NORMALIZED + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPE + "(" + COL_ID + ") ON DELETE CASCADE)");

        // Pre-load the recipe collection on first run
        RecipeSeeder.seed(db);
    }

    /**
     * Upgrades keep the user's data wherever possible.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Very old layout: rebuild everything (which also re-seeds the recipes)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
            onCreate(db);
            return;
        }
        if (oldVersion < 3) {
            // Just add the new column; existing pantry items and recipes are kept
            db.execSQL("ALTER TABLE " + TABLE_RECIPE + " ADD COLUMN "
                    + COL_IS_CUSTOM + " INTEGER NOT NULL DEFAULT 0");
        }
    }

    // ---------------------------------------------------------------
    // PANTRY: CREATE
    // ---------------------------------------------------------------

    /**
     * Inserts a pantry item and returns its new row id (or -1 on failure).
     */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, toValues(item));
    }

    // ---------------------------------------------------------------
    // PANTRY: READ
    // ---------------------------------------------------------------

    /**
     * Returns every pantry item, sorted alphabetically by name.
     */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_PANTRY, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                items.add(fromCursor(c));
            }
        }
        return items;
    }

    /**
     * Returns one pantry item by id, or null if it does not exist.
     */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_PANTRY, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    // ---------------------------------------------------------------
    // PANTRY: UPDATE
    // ---------------------------------------------------------------

    /**
     * Updates an existing pantry item. Returns the number of rows changed.
     */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, toValues(item), COL_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    // ---------------------------------------------------------------
    // PANTRY: DELETE
    // ---------------------------------------------------------------

    /**
     * Deletes a pantry item by id. Returns the number of rows removed.
     */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // ---------------------------------------------------------------
    // RECIPES: CREATE
    // ---------------------------------------------------------------

    /**
     * Saves a user-created recipe together with all of its ingredients.
     * Uses a transaction so either everything is saved or nothing is.
     * Returns the new recipe id, or -1 on failure.
     */
    public long addRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put(COL_RECIPE_NAME, recipe.getName());
            recipeValues.put(COL_RECIPE_STEPS, recipe.getSteps());
            recipeValues.put(COL_IS_CUSTOM, 1);
            long recipeId = db.insert(TABLE_RECIPE, null, recipeValues);
            if (recipeId == -1) {
                return -1;   // transaction is not marked successful, so it is rolled back
            }

            for (RecipeIngredient ing : recipe.getIngredients()) {
                ContentValues values = new ContentValues();
                values.put(COL_RECIPE_ID, recipeId);
                values.put(COL_NAME, ing.getName());
                values.put(COL_NORMALIZED, ing.getNormalizedName());
                values.put(COL_QUANTITY, ing.getQuantity());
                values.put(COL_UNIT, ing.getUnit());
                db.insert(TABLE_RECIPE_ING, null, values);
            }
            db.setTransactionSuccessful();
            return recipeId;
        } finally {
            db.endTransaction();
        }
    }

    // ---------------------------------------------------------------
    // RECIPES: READ
    // ---------------------------------------------------------------

    /**
     * Returns every recipe together with its ingredient list.
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPE, null, null, null,
                null, null, COL_RECIPE_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                recipes.add(recipeFromCursor(db, c));
            }
        }
        return recipes;
    }

    /**
     * Returns one recipe (with its ingredients) by id, or null if not found.
     */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPE, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? recipeFromCursor(db, c) : null;
        }
    }

    /**
     * Returns the base unit ("g", "ml" or "piece") that recipes use for this
     * ingredient, or null if no recipe uses it.
     */
    public String getRecipeUnitForIngredient(String normalizedName) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_RECIPE_ING, new String[]{COL_UNIT},
                COL_NORMALIZED + " = ?", new String[]{normalizedName},
                null, null, null, "1")) {
            return c.moveToFirst() ? c.getString(0) : null;
        }
    }

    // ---------------------------------------------------------------
    // RECIPES: DELETE
    // ---------------------------------------------------------------

    /**
     * Deletes a recipe the user created. Built-in recipes cannot be deleted.
     * The recipe's ingredient rows are removed automatically (ON DELETE CASCADE).
     */
    public int deleteRecipe(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_RECIPE, COL_ID + " = ? AND " + COL_IS_CUSTOM + " = 1",
                new String[]{String.valueOf(id)});
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_NORMALIZED, item.getNormalizedName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate()); // null is allowed
        return values;
    }

    private PantryItem fromCursor(Cursor c) {
        PantryItem item = new PantryItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        item.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        item.setNormalizedName(c.getString(c.getColumnIndexOrThrow(COL_NORMALIZED)));
        item.setQuantity(c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)));
        item.setUnit(c.getString(c.getColumnIndexOrThrow(COL_UNIT)));
        int expiryIndex = c.getColumnIndexOrThrow(COL_EXPIRY);
        item.setExpiryDate(c.isNull(expiryIndex) ? null : c.getString(expiryIndex));
        return item;
    }

    /**
     * Builds a Recipe from the current cursor row and loads its ingredients.
     */
    private Recipe recipeFromCursor(SQLiteDatabase db, Cursor c) {
        Recipe recipe = new Recipe();
        recipe.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        recipe.setName(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)));
        recipe.setSteps(c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)));
        recipe.setCustom(c.getInt(c.getColumnIndexOrThrow(COL_IS_CUSTOM)) == 1);
        recipe.setIngredients(getIngredientsForRecipe(db, recipe.getId()));
        return recipe;
    }

    /**
     * Reads all ingredient lines that belong to one recipe.
     */
    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor c = db.query(TABLE_RECIPE_ING, null, COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null)) {
            while (c.moveToNext()) {
                RecipeIngredient ing = new RecipeIngredient();
                ing.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
                ing.setRecipeId(recipeId);
                ing.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
                ing.setNormalizedName(c.getString(c.getColumnIndexOrThrow(COL_NORMALIZED)));
                ing.setQuantity(c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)));
                ing.setUnit(c.getString(c.getColumnIndexOrThrow(COL_UNIT)));
                ingredients.add(ing);
            }
        }
        return ingredients;
    }
}