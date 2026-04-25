package com.example.app_recettes

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.app_recettes.database.DatabaseHelper
import com.example.app_recettes.database.IngredientDetail
import com.example.app_recettes.database.RecipeDetail
import com.example.app_recettes.databinding.ActivityRecipeDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding
    private lateinit var dbHelper: DatabaseHelper

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        if (recipeId == -1) { finish(); return }

        loadRecipeDetails(recipeId)
    }

    override fun onResume() {
        super.onResume()
        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        if (recipeId != -1) loadRecipeDetails(recipeId)
    }

    private fun loadRecipeDetails(recipeId: Int) {
        val recipe = dbHelper.getRecipeDetailById(recipeId) ?: run { finish(); return }
        val ingredients = dbHelper.getIngredientsByRecipeId(recipeId)
        displayRecipe(recipe, ingredients)
    }

    private fun displayRecipe(recipe: RecipeDetail, ingredients: List<IngredientDetail>) {
        binding.collapsingToolbar.title = recipe.name
        binding.tvRecipeName.text = recipe.name
        binding.tvCategory.text = recipe.category

        // Difficulté
        binding.tvDifficulty.text = recipe.difficulte ?: "—"
        binding.tvDifficulty.setTextColor(
            ContextCompat.getColor(this, when (recipe.difficulte) {
                "Facile"    -> R.color.difficulty_easy
                "Moyen"     -> R.color.difficulty_medium
                "Difficile" -> R.color.difficulty_hard
                else        -> R.color.text_secondary
            })
        )

        // Temps
        binding.tvTime.text = recipe.tempsMinutes?.takeIf { it > 0 }?.let { t ->
            if (t >= 60) {
                val h = t / 60; val m = t % 60
                if (m > 0) "${h}h ${m}min" else "${h}h"
            } else "${t} min"
        } ?: "—"

        // Portions
        binding.tvPortions.text = recipe.nbPersonnes?.takeIf { it > 0 }
            ?.let { "$it pers." } ?: "—"

        // Date
        recipe.dateAjout?.let {
            binding.tvDate.text = "Ajoutée le $it"
            binding.tvDate.visibility = View.VISIBLE
        } ?: run { binding.tvDate.visibility = View.GONE }

        displayIngredients(ingredients)
        displaySteps(recipe.etapes)
    }

    private fun displayIngredients(ingredients: List<IngredientDetail>) {
        binding.containerIngredients.removeAllViews()
        binding.tvIngredientCount.visibility =
            if (ingredients.isEmpty()) View.GONE else View.VISIBLE
        binding.tvIngredientCount.text = ingredients.size.toString()

        if (ingredients.isEmpty()) {
            binding.containerIngredients.addView(emptyText("Aucun ingrédient renseigné."))
            return
        }

        ingredients.forEachIndexed { i, ingredient ->
            if (i > 0) binding.containerIngredients.addView(divider())

            val row = layoutInflater.inflate(
                R.layout.item_ingredient_detail,
                binding.containerIngredients,
                false
            )
            row.findViewById<TextView>(R.id.tv_ingredient_number).text = (i + 1).toString()
            row.findViewById<TextView>(R.id.tv_ingredient_name).text = ingredient.nom

            val qty = buildString {
                ingredient.quantite?.takeIf { it.isNotBlank() }?.let { append(it) }
                ingredient.unite?.takeIf { it.isNotBlank() }?.let { u ->
                    if (isNotEmpty()) append(" "); append(u)
                }
            }
            row.findViewById<TextView>(R.id.tv_ingredient_qty).text = qty.ifBlank { "—" }
            binding.containerIngredients.addView(row)
        }
    }

    private fun displaySteps(etapes: String?) {
        if (etapes.isNullOrBlank()) {
            binding.tvSteps.text = "Aucune étape renseignée."
            binding.tvSteps.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            return
        }
        val lines = etapes.trim().split("\n")
            .map { it.trim() }.filter { it.isNotBlank() }
        val alreadyNumbered = lines.all { it.matches(Regex("^\\d+[.)].+")) }
        binding.tvSteps.text = if (alreadyNumbered) etapes.trim()
        else lines.mapIndexed { i, l -> "${i + 1}. $l" }.joinToString("\n\n")
        binding.tvSteps.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
    }

    private fun divider() = android.view.View(this).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
        ).also { it.setMargins(40, 0, 0, 0) }
        setBackgroundColor(ContextCompat.getColor(context, R.color.divider_color))
    }

    private fun emptyText(msg: String) = TextView(this).apply {
        text = msg
        textSize = 14f
        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
    }
}