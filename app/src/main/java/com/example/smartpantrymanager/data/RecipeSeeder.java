package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.smartpantrymanager.logic.NameNormalizer;
import com.example.smartpantrymanager.logic.UnitConverter;

/**
 * Pre-loads the recipe collection the first time the database is created.
 * Each ingredient is written as "name,quantity,unit". Names are normalized and
 * quantities converted to base units, exactly like pantry items, so they compare fairly.
 */
public class RecipeSeeder {

    private RecipeSeeder() { }

    public static void seed(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Beat the eggs in a bowl.\n2. Melt the butter in a pan and cook the eggs, stirring gently.\n3. Toast the bread and serve the eggs on top.",
                "egg,3,piece", "bread,2,piece", "butter,10,g");

        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt the butter in a pan and pour in the eggs.\n3. Add the cheese, fold the omelette and serve.",
                "egg,3,piece", "cheese,50,g", "butter,10,g");

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft.\n2. Fry the chopped onion and garlic, then add the chopped tomatoes and simmer.\n3. Mix the sauce through the pasta.",
                "pasta,200,g", "tomato,4,piece", "onion,1,piece", "garlic,2,piece");

        addRecipe(db, "Pancakes",
                "1. Whisk the flour, milk, egg and sugar into a smooth batter.\n2. Melt a little butter in a pan.\n3. Cook spoonfuls of batter until golden on both sides.",
                "flour,150,g", "milk,250,ml", "egg,1,piece", "sugar,1,tbsp", "butter,20,g");

        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry the chopped onion and carrot, then push them aside and scramble the eggs.\n3. Add the rice and soy sauce and stir-fry for a few minutes.",
                "rice,200,g", "egg,2,piece", "onion,1,piece", "carrot,1,piece", "soy sauce,2,tbsp");

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Place the cheese between the slices.\n3. Fry on both sides until golden and the cheese melts.",
                "bread,2,piece", "cheese,60,g", "butter,10,g");

        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with the butter and warm milk until smooth.",
                "potato,4,piece", "butter,30,g", "milk,100,ml");

        addRecipe(db, "Vegetable Soup",
                "1. Chop the potatoes, carrots and onion.\n2. Simmer them in the stock until soft.\n3. Blend or mash lightly and serve hot.",
                "potato,2,piece", "carrot,2,piece", "onion,1,piece", "vegetable stock,500,ml");

        addRecipe(db, "Tuna Pasta Bake",
                "1. Boil the pasta.\n2. Mix with the tuna and chopped onion in an oven dish.\n3. Top with cheese and bake until golden.",
                "pasta,200,g", "tuna,1,piece", "onion,1,piece", "cheese,40,g");

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas and mix with the eggs and flour.\n2. Cook small spoonfuls in a lightly greased pan until golden on both sides.",
                "banana,2,piece", "egg,2,piece", "flour,50,g");

        addRecipe(db, "Chicken Rice Bowl",
                "1. Cook the rice.\n2. Fry the chopped onion and garlic, then add the sliced chicken and cook through.\n3. Serve the chicken over the rice.",
                "chicken breast,300,g", "rice,200,g", "onion,1,piece", "garlic,2,piece");

        addRecipe(db, "Tomato Soup",
                "1. Fry the chopped onion and garlic.\n2. Add the chopped tomatoes and stock and simmer for 20 minutes.\n3. Blend until smooth.",
                "tomato,6,piece", "onion,1,piece", "garlic,2,piece", "vegetable stock,300,ml");

        addRecipe(db, "Potato Omelette",
                "1. Slice the potatoes and onion and fry until soft.\n2. Beat the eggs and pour over the potatoes.\n3. Cook until set, flipping once.",
                "potato,2,piece", "egg,4,piece", "onion,1,piece");

        addRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar.\n2. Dip the bread slices in the mixture.\n3. Fry until golden on both sides.",
                "bread,4,piece", "egg,2,piece", "milk,100,ml", "sugar,1,tbsp");

        addRecipe(db, "Garlic Butter Pasta",
                "1. Boil the pasta.\n2. Melt the butter and fry the crushed garlic gently.\n3. Toss the pasta in the garlic butter and sprinkle with cheese.",
                "pasta,200,g", "garlic,4,piece", "butter,40,g", "cheese,30,g");

        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Top with sliced banana.",
                "bread,2,piece", "peanut butter,2,tbsp", "banana,1,piece");

        addRecipe(db, "Banana Oatmeal",
                "1. Heat the oats and milk in a pot, stirring until thick.\n2. Top with sliced banana and honey.",
                "oats,80,g", "milk,250,ml", "banana,1,piece", "honey,1,tbsp");

        addRecipe(db, "Cheesy Rice",
                "1. Cook the rice.\n2. Fry the chopped onion in butter.\n3. Stir the onion and cheese through the hot rice until melted.",
                "rice,200,g", "cheese,80,g", "butter,20,g", "onion,1,piece");

        addRecipe(db, "Baked Potatoes with Cheese",
                "1. Bake the whole potatoes until soft.\n2. Cut them open and add butter and cheese.\n3. Return to the oven until the cheese melts.",
                "potato,2,piece", "cheese,60,g", "butter,20,g");

        addRecipe(db, "Egg Fried Noodles",
                "1. Boil the noodles and drain.\n2. Stir-fry the grated carrot, then scramble in the eggs.\n3. Add the noodles and soy sauce and toss together.",
                "noodles,200,g", "egg,2,piece", "carrot,1,piece", "soy sauce,2,tbsp");
    }

    /** Inserts one recipe and all of its ingredient lines. */
    private static void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(DatabaseHelper.COL_RECIPE_NAME, name);
        recipeValues.put(DatabaseHelper.COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(DatabaseHelper.TABLE_RECIPE, null, recipeValues);

        for (String line : ingredients) {
            String[] parts = line.split(",");
            String ingredientName = parts[0].trim();
            double quantity = Double.parseDouble(parts[1].trim());
            String unit = parts[2].trim();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_RECIPE_ID, recipeId);
            values.put(DatabaseHelper.COL_NAME, ingredientName);
            values.put(DatabaseHelper.COL_NORMALIZED, NameNormalizer.normalize(ingredientName));
            values.put(DatabaseHelper.COL_QUANTITY, UnitConverter.toBaseQuantity(quantity, unit));
            values.put(DatabaseHelper.COL_UNIT, UnitConverter.toBaseUnit(unit));
            db.insert(DatabaseHelper.TABLE_RECIPE_ING, null, values);
        }
    }
}
