   Smart Pantry Manager

An Android app written in Java that helps reduce food waste. The user records the
ingredients they already have at home, and the app suggests only the recipes they can
cook right now, with no shopping trip required.

Built for Mobile App Development 700 (Richfield) by Munyai Masindi, student number 402411898.

  Features

- Home screen with live statistics (items in pantry, recipes you can cook, items expiring soon)
- Add, view, edit and delete pantry items (name, quantity, unit, optional expiry date)
- 20 pre-loaded recipes, seeded on first run
- Create, view and delete your own recipes
- **Strict matching:** a recipe is suggested only if every ingredient is in the pantry
  in at least the required quantity. One missing ingredient excludes the recipe.
- Name matching that handles plurals and capital letters ("Tomatoes" matches "tomato")
- Unit conversion so that 1 kg satisfies 500 g (g, kg, ml, l, cup, tbsp, tsp, piece)
- Input validation, including a check that the unit suits the ingredient
- Recipe detail screen with the full ingredient list and method
- Settings screen: highlight expired and expiring-soon items, with a choice of 1, 3 or 7 days
- A friendly message when no recipes match the pantry

  Database choice: SQLite

The app uses SQLite through `SQLiteOpenHelper`. I chose it because:

- It runs on the device, so the app works offline and needs no accounts or servers.
- A pantry is personal data, so keeping it on the phone is simple and private.
- It is covered in the module and supports full CRUD with relationships
  (one recipe has many recipe_ingredient rows).

Tables: `pantry_item`, `recipe`, `recipe_ingredient`. Settings are stored with
`SharedPreferences`.

  ## Project structure

  ```
  com.example.smartpantrymanager
  ├── model/      PantryItem, Recipe, RecipeIngredient
  ├── data/       DatabaseHelper, RecipeSeeder, SettingsManager
  ├── logic/      RecipeMatcher, NameNormalizer, UnitConverter,
  │               IngredientUnits, ExpiryHelper
  ├── adapter/    PantryAdapter, RecipeAdapter
  ├── ui/         Home, Pantry, Recipes and Settings fragments,
  │               plus the Add/Edit, Recipe Detail and Add Recipe activities
  └── MainActivity
  ```

 How the strict-matching rule works

1. The pantry is totalled into a lookup of "normalized name + base unit" and quantity.
2. Each recipe ingredient is checked against that lookup.
3. If any ingredient is missing or the quantity is too small, the whole recipe is excluded.

Names are cleaned by `NameNormalizer` and quantities are converted to base units
(g, ml, piece) by `UnitConverter`, so trivial differences do not break matching.

  How to set up and run

1. Install [Android Studio](https://developer.android.com/studio).
2. Clone the repository:
   `git clone https://github.com/cindymunyai07/SmartPantryManager.git`
3. In Android Studio choose **File > Open** and select the project folder.
4. Wait for the Gradle sync to finish.
5. Create a virtual device in **Device Manager** (for example Small Phone, API 34),
   or connect an Android phone with USB debugging turned on.
6. Press the green **Run** button.

Minimum SDK: API 24. Language: Java.

## Scope

The app does not use Google Maps, location or GPS services. It focuses only on the
user's own pantry and recipe matching.
