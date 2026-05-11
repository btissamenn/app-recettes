package com.example.app_recettes.models

data class Recette(
    val id: Int,
    val nom: String,
    val categorieId: Int?,
    val categorieNom: String?,
    val difficulte: String?,
    val tempsMinutes: Int?,
    val nbPersonnes: Int?,
    val etapes: String?,
    val dateAjout: String?,
    val ingredientsPreview: String = "",
    val imagePath: String? = null
)