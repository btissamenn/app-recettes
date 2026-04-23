package com.example.app_recettes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.app_recettes.adapters.RecipeAdapter
import com.example.app_recettes.database.DatabaseHelper
import com.example.app_recettes.models.Recipe
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecipeListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: RecipeAdapter
    private lateinit var recipes: MutableList<Recipe>
    private lateinit var searchInput: EditText

    private lateinit var tvRecipeCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        db = DatabaseHelper(this)
        listView = findViewById(R.id.listView)
        searchInput = findViewById(R.id.searchInput)
        tvRecipeCount = findViewById(R.id.tvRecipeCount)

        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        setupSearch()
        setupCategories()
        loadData()

        listView.setOnItemClickListener { _, _, position, _ ->
            val recipe = adapter.getItem(position) as Recipe
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("recipe_id", recipe.id)
            startActivity(intent)
        }
    }

    private fun setupCategories() {
        val categories = listOf<TextView>(
            findViewById(R.id.btnCategoryAll),
            findViewById(R.id.btnCategoryFavorites),
            findViewById(R.id.btnCategoryStarter),
            findViewById(R.id.btnCategoryMain),
            findViewById(R.id.btnCategoryDessert),
            findViewById(R.id.btnCategoryBreakfast),
            findViewById(R.id.btnCategoryDrinks),
            findViewById(R.id.btnCategorySnacks)
        )

        categories.forEach { btn ->
            btn.setOnClickListener {
                // Reset all to Dark Green background with White text
                categories.forEach {
                    it.setBackgroundResource(R.drawable.bg_chip_unselected)
                    it.setTextColor(android.graphics.Color.WHITE)
                    it.alpha = 0.8f 
                    it.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
                // Select clicked to Yellow with Dark Green text
                btn.setBackgroundResource(R.drawable.bg_chip_selected)
                btn.setTextColor(android.graphics.Color.parseColor("#1B5E20"))
                btn.alpha = 1.0f
                btn.setTypeface(null, android.graphics.Typeface.BOLD)
                
                filterByCategory(btn.text.toString())
            }
        }
    }

    private fun filterByCategory(category: String) {
        val filteredList = when (category) {
            "Tout" -> recipes
            "Favoris" -> recipes.filter { it.isFavorite }
            else -> recipes.filter { it.category == category }
        }
        adapter.updateData(filteredList)
        updateCount(filteredList.size)
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecipes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterRecipes(query: String) {
        val filteredList = recipes.filter { it.name.contains(query, ignoreCase = true) || it.ingredients.contains(query, ignoreCase = true) }
        adapter.updateData(filteredList)
        updateCount(filteredList.size)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        recipes = db.getAllRecipes()
        adapter = RecipeAdapter(this, recipes)
        listView.adapter = adapter
        updateCount(recipes.size)
    }

    private fun updateCount(count: Int) {
        tvRecipeCount.text = getString(R.string.recipes_count_format, count)
    }
}