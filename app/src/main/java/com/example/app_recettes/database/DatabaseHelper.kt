package com.example.app_recettes.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.app_recettes.models.Recipe

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recipes.db"
        private const val DATABASE_VERSION = 4  // incrémenté de 3 à 4

        // ── Table de l'équipe ──────────────────────
        private const val TABLE_RECIPES = "recipes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_INGREDIENTS = "ingredients"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_FAVORITE = "is_favorite"
        private const val COLUMN_IMAGE = "image_path"

        // ── Nouvelles colonnes pour l'écran détail ─
        private const val COLUMN_DIFFICULTE = "difficulte"
        private const val COLUMN_TEMPS = "temps_minutes"
        private const val COLUMN_PERSONNES = "nb_personnes"
        private const val COLUMN_ETAPES = "etapes"
        private const val COLUMN_DATE = "date_ajout"

        // ── Nouvelles tables ───────────────────────
        private const val TABLE_CATEGORIES = "categories"
        private const val TABLE_INGREDIENTS = "ingredients_detail"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Table de l'équipe avec nouvelles colonnes
        val createRecipesTable = ("CREATE TABLE $TABLE_RECIPES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_INGREDIENTS TEXT," +
                "$COLUMN_DESCRIPTION TEXT," +
                "$COLUMN_CATEGORY TEXT," +
                "$COLUMN_FAVORITE INTEGER DEFAULT 0," +
                "$COLUMN_IMAGE TEXT," +
                "$COLUMN_DIFFICULTE TEXT CHECK($COLUMN_DIFFICULTE IN ('Facile','Moyen','Difficile'))," +
                "$COLUMN_TEMPS INTEGER," +
                "$COLUMN_PERSONNES INTEGER," +
                "$COLUMN_ETAPES TEXT," +
                "$COLUMN_DATE TEXT DEFAULT (date('now')))")
        db?.execSQL(createRecipesTable)

        // Table catégories
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                id  INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

        // Table ingrédients détaillée
        db?.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_INGREDIENTS (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                recette_id INTEGER NOT NULL,
                nom        TEXT    NOT NULL,
                quantite   TEXT,
                unite      TEXT,
                FOREIGN KEY (recette_id) REFERENCES $TABLE_RECIPES($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        // Catégories de base
        listOf("Entrée", "Plat", "Dessert", "Boisson", "Snack").forEach { cat ->
            db?.execSQL("INSERT OR IGNORE INTO $TABLE_CATEGORIES (nom) VALUES ('$cat')")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_IMAGE TEXT")
        }
        if (oldVersion < 4) {
            // Ajouter les nouvelles colonnes à la table existante
            db?.execSQL("ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_DIFFICULTE TEXT")
            db?.execSQL("ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_TEMPS INTEGER")
            db?.execSQL("ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_PERSONNES INTEGER")
            db?.execSQL("ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_ETAPES TEXT")
            db?.execSQL("ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_DATE TEXT")

            // Créer les nouvelles tables
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                    id  INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom TEXT NOT NULL UNIQUE
                )
            """.trimIndent())

            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_INGREDIENTS (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    recette_id INTEGER NOT NULL,
                    nom        TEXT    NOT NULL,
                    quantite   TEXT,
                    unite      TEXT,
                    FOREIGN KEY (recette_id) REFERENCES $TABLE_RECIPES($COLUMN_ID) ON DELETE CASCADE
                )
            """.trimIndent())

            listOf("Entrée", "Plat", "Dessert", "Boisson", "Snack").forEach { cat ->
                db?.execSQL("INSERT OR IGNORE INTO $TABLE_CATEGORIES (nom) VALUES ('$cat')")
            }
        }
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db?.execSQL("PRAGMA foreign_keys = ON")
    }

    // ── Méthodes de l'équipe (inchangées) ────────────────────────────────────

    fun getAllRecipes(): MutableList<Recipe> {
        val recipeList = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RECIPES", null)
        if (cursor.moveToFirst()) {
            do {
                val recipe = Recipe(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)) ?: "Tout",
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1,
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
                )
                recipeList.add(recipe)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return recipeList
    }

    fun addRecipe(recipe: Recipe): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, recipe.name)
        values.put(COLUMN_INGREDIENTS, recipe.ingredients)
        values.put(COLUMN_DESCRIPTION, recipe.description)
        values.put(COLUMN_CATEGORY, recipe.category)
        values.put(COLUMN_FAVORITE, if (recipe.isFavorite) 1 else 0)
        values.put(COLUMN_IMAGE, recipe.imagePath)
        return db.insert(TABLE_RECIPES, null, values)
    }

    fun updateFavorite(recipeId: Int, isFavorite: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_FAVORITE, if (isFavorite) 1 else 0)
        db.update(TABLE_RECIPES, values, "$COLUMN_ID = ?", arrayOf(recipeId.toString()))
    }

    fun updateRecipe(recipe: Recipe) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, recipe.name)
        values.put(COLUMN_INGREDIENTS, recipe.ingredients)
        values.put(COLUMN_DESCRIPTION, recipe.description)
        values.put(COLUMN_CATEGORY, recipe.category)
        values.put(COLUMN_IMAGE, recipe.imagePath)
        db.update(TABLE_RECIPES, values, "$COLUMN_ID = ?", arrayOf(recipe.id.toString()))
        db.close()
    }

    fun deleteRecipe(recipeId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_RECIPES, "$COLUMN_ID = ?", arrayOf(recipeId.toString()))
        db.close()
    }

    fun getRecipeById(id: Int): Recipe? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RECIPES, null,
            "$COLUMN_ID = ?", arrayOf(id.toString()),
            null, null, null
        )
        var recipe: Recipe? = null
        if (cursor.moveToFirst()) {
            recipe = Recipe(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)) ?: "Tout",
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
            )
        }
        cursor.close()
        return recipe
    }

    // ── Méthodes pour l'écran détail ─────────────────────────────────────────

    fun getRecipeDetailById(id: Int): RecipeDetail? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RECIPES, null,
            "$COLUMN_ID = ?", arrayOf(id.toString()),
            null, null, null
        )
        var detail: RecipeDetail? = null
        if (cursor.moveToFirst()) {
            detail = RecipeDetail(
                id          = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                name        = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                category    = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)) ?: "Tout",
                difficulte  = cursor.getStringOrNull(COLUMN_DIFFICULTE),
                tempsMinutes = cursor.getIntOrNull(COLUMN_TEMPS),
                nbPersonnes = cursor.getIntOrNull(COLUMN_PERSONNES),
                etapes      = cursor.getStringOrNull(COLUMN_ETAPES),
                dateAjout   = cursor.getStringOrNull(COLUMN_DATE),
                imagePath   = cursor.getStringOrNull(COLUMN_IMAGE)
            )
        }
        cursor.close()
        return detail
    }

    fun getIngredientsByRecipeId(recipeId: Int): List<IngredientDetail> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_INGREDIENTS WHERE recette_id = ? ORDER BY id ASC",
            arrayOf(recipeId.toString())
        )
        val list = mutableListOf<IngredientDetail>()
        while (cursor.moveToNext()) {
            list.add(
                IngredientDetail(
                    id       = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nom      = cursor.getString(cursor.getColumnIndexOrThrow("nom")),
                    quantite = cursor.getStringOrNull("quantite"),
                    unite    = cursor.getStringOrNull("unite")
                )
            )
        }
        cursor.close()
        return list
    }

    // ── Helpers null-safe ─────────────────────────────────────────────────────

    private fun android.database.Cursor.getStringOrNull(col: String): String? {
        val idx = getColumnIndex(col)
        return if (idx != -1 && !isNull(idx)) getString(idx) else null
    }

    private fun android.database.Cursor.getIntOrNull(col: String): Int? {
        val idx = getColumnIndex(col)
        return if (idx != -1 && !isNull(idx)) getInt(idx) else null
    }
}

// ── Data classes pour l'écran détail ─────────────────────────────────────────

data class RecipeDetail(
    val id: Int,
    val name: String,
    val category: String,
    val difficulte: String?,
    val tempsMinutes: Int?,
    val nbPersonnes: Int?,
    val etapes: String?,
    val dateAjout: String?,
    val imagePath: String?
)

data class IngredientDetail(
    val id: Int,
    val nom: String,
    val quantite: String?,
    val unite: String?
)