package com.example.app_recettes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.app_recettes.R
import com.example.app_recettes.database.DatabaseHelper
import com.example.app_recettes.models.Recipe

class RecipeAdapter(private val context: Context, private var recipeList: List<Recipe>) : BaseAdapter() {

    private val db = DatabaseHelper(context)

    override fun getCount(): Int = recipeList.size

    override fun getItem(position: Int): Any = recipeList[position]

    override fun getItemId(position: Int): Long = recipeList[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)

        val recipe = getItem(position) as Recipe

        val tvName = view.findViewById<TextView>(R.id.tvRecipeName)
        val tvIngredients = view.findViewById<TextView>(R.id.tvIngredients)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val imgRecipe = view.findViewById<ImageView>(R.id.imgRecipe)
        val btnFavorite = view.findViewById<ImageButton>(R.id.btnFavorite)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        tvName.text = recipe.name
        tvIngredients.text = recipe.ingredients
        tvTime.text = context.getString(R.string.time_format, "25")

        if (recipe.imagePath != null) {
            val file = java.io.File(recipe.imagePath)
            if (file.exists()) {
                imgRecipe.setImageURI(android.net.Uri.fromFile(file))
                imgRecipe.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                setCategoryIcon(imgRecipe, recipe.category)
            }
        } else {
            setCategoryIcon(imgRecipe, recipe.category)
        }
        
        if (recipe.isFavorite) {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
        }

        // Handle Star Button Click
        btnFavorite.setOnClickListener {
            recipe.isFavorite = !recipe.isFavorite
            db.updateFavorite(recipe.id, recipe.isFavorite)
            notifyDataSetChanged()
        }

        btnDelete.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_confirmation_title))
                .setMessage(context.getString(R.string.delete_confirmation_message))
                .setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                    db.deleteRecipe(recipe.id)
                    updateData(db.getAllRecipes()) // Or notify parent to refresh
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        }

        return view
    }

    private fun setCategoryIcon(imageView: ImageView, category: String) {
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
        imageView.setBackgroundColor(android.graphics.Color.parseColor("#F1F8E9")) // Vert très clair
    }

    fun updateData(newList: List<Recipe>) {
        recipeList = newList
        notifyDataSetChanged()
    }
}