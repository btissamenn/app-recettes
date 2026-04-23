package com.example.app_recettes.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.app_recettes.models.Recipe

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recipes.db"
        private const val DATABASE_VERSION = 3 
        private const val TABLE_RECIPES = "recipes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_INGREDIENTS = "ingredients"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_FAVORITE = "is_favorite"
        private const val COLUMN_IMAGE = "image_path"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_RECIPES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_INGREDIENTS TEXT," +
                "$COLUMN_DESCRIPTION TEXT," +
                "$COLUMN_CATEGORY TEXT," +
                "$COLUMN_FAVORITE INTEGER DEFAULT 0," +
                "$COLUMN_IMAGE TEXT)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_IMAGE TEXT")
        }
    }

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
        val cursor = db.query(TABLE_RECIPES, null, "$COLUMN_ID = ?", arrayOf(id.toString()), null, null, null)
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
}