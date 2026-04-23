package com.example.app_recettes

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.app_recettes.database.DatabaseHelper
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var recipeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        db = DatabaseHelper(this)
        recipeId = intent.getIntExtra("recipe_id", -1)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { finish() }

        loadRecipeDetails()

        findViewById<Button>(R.id.btnDeleteRecipe).setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadRecipeDetails() {
        val recipe = db.getRecipeById(recipeId)
        if (recipe != null) {
            findViewById<TextView>(R.id.tvRecipeNameDetail).text = recipe.name
            findViewById<TextView>(R.id.tvIngredientsDetail).text = recipe.ingredients.replace(", ", "\n• ")
            findViewById<TextView>(R.id.tvStepsDetail).text = recipe.description
            findViewById<TextView>(R.id.tvCategoryDetail).text = recipe.category
            findViewById<TextView>(R.id.tvTimeDetail).text = getString(R.string.time_format, "25")
            findViewById<TextView>(R.id.tvPortionsDetail).text = getString(R.string.portions_format, "4")

            val imgRecipe = findViewById<ImageView>(R.id.imgRecipeDetail)
            if (recipe.imagePath != null) {
                val file = File(recipe.imagePath)
                if (file.exists()) {
                    imgRecipe.setImageURI(Uri.fromFile(file))
                    imgRecipe.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    setCategoryPlaceholder(imgRecipe, recipe.category)
                }
            } else {
                setCategoryPlaceholder(imgRecipe, recipe.category)
            }
        } else {
            Toast.makeText(this, getString(R.string.error_loading_recipe), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setCategoryPlaceholder(imageView: ImageView, category: String) {
        val iconRes = when (category) {
            "Entrée" -> R.drawable.ic_category_starter
            "Plat" -> R.drawable.ic_category_main
            "Dessert" -> R.drawable.ic_category_dessert
            "Petit-déj" -> R.drawable.ic_category_breakfast
            "Boissons" -> R.drawable.ic_category_drinks
            "Snacks" -> R.drawable.ic_category_snacks
            else -> R.drawable.ic_launcher_foreground
        }
        imageView.setImageResource(iconRes)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setBackgroundColor(android.graphics.Color.parseColor("#F1F8E9"))
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                db.deleteRecipe(recipeId)
                Toast.makeText(this, getString(R.string.recipe_deleted), Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}