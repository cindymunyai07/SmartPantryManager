package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the SQLite database and provides CRUD methods for pantry items.
 * Recipe methods and seeding are added in a later step.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

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

    // recipe_ingredient table
    public static final String TABLE_RECIPE_ING = "recipe_ingredient";
    public static final String COL_RECIPE_ID = "recipe_id";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** Turn on foreign keys so deleting a recipe also deletes its ingredients. */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Called once, the first time the database is created. */
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
                + COL_RECIPE_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_NORMALIZED + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPE + "(" + COL_ID + ") ON DELETE CASCADE)");
    }

    /** Simple upgrade strategy: drop everything and rebuild. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------

    /** Inserts a pantry item and returns its new row id (or -1 on failure). */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, toValues(item));
    }

    // ---------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------

    /** Returns every pantry item, sorted alphabetically by name. */
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

    /** Returns one pantry item by id, or null if it does not exist. */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_PANTRY, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------

    /** Updates an existing pantry item. Returns the number of rows changed. */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, toValues(item), COL_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------

    /** Deletes a pantry item by id. Returns the number of rows removed. */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + " = ?",
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
}