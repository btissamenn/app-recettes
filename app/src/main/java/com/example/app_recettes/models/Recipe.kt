package com.example.app_recettes.models

data class Recipe(
    val id: Int,
    val name: String,
    val ingredients: String,
    val description: String,
    val category: String = "Tout",
    var isFavorite: Boolean = false,
    val imagePath: String? = null
)