package com.example.app_recettes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.app_recettes.R
import com.example.app_recettes.models.Recette

class RecetteAdapter(
    private val context: Context,
    private var recettes: List<Recette>,
    private val onCardClick: (Recette) -> Unit
) : RecyclerView.Adapter<RecetteAdapter.RecetteViewHolder>() {

    private val categoryEmoji = mapOf(
        "Entrée"  to "🥙",
        "Plat"    to "🍲",
        "Dessert" to "🎂",
        "Boisson" to "🥤",
        "Snack"   to "🥨"
    )

    private val categoryBgColor = mapOf(
        "Entrée"  to R.color.ci_green_start,
        "Plat"    to R.color.ci_peach_start,
        "Dessert" to R.color.ci_pink_start,
        "Boisson" to R.color.ci_amber_start,
        "Snack"   to R.color.ci_amber_start
    )

    inner class RecetteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardImageBg: View  = view.findViewById(R.id.cardImageBg)
        val tvEmoji: TextView  = view.findViewById(R.id.tvEmoji)
        val tvName: TextView   = view.findViewById(R.id.tvRecetteName)
        val tvDesc: TextView   = view.findViewById(R.id.tvRecetteDesc)
        val tvTemps: TextView  = view.findViewById(R.id.tvTemps)
        val tvDiff: TextView   = view.findViewById(R.id.tvDifficulte)
        val btnHeart: TextView = view.findViewById(R.id.btnHeart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetteViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_recette, parent, false)
        return RecetteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetteViewHolder, position: Int) {
        val recette = recettes[position]

        holder.tvName.text = recette.nom
        holder.tvDesc.text = recette.ingredientsPreview.ifEmpty { "Aucun ingrédient" }

        holder.tvTemps.text = if ((recette.tempsMinutes ?: 0) >= 60) {
            val h = (recette.tempsMinutes ?: 0) / 60
            val m = (recette.tempsMinutes ?: 0) % 60
            if (m == 0) "${h}h" else "${h}h${m}min"
        } else {
            "${recette.tempsMinutes ?: "?"} min"
        }

        holder.tvDiff.text = recette.difficulte ?: "—"
        when (recette.difficulte) {
            "Facile" -> {
                holder.tvDiff.setTextColor(ContextCompat.getColor(context, R.color.badge_easy_text))
                holder.tvDiff.setBackgroundResource(R.drawable.bg_badge_easy)
            }
            "Moyen" -> {
                holder.tvDiff.setTextColor(ContextCompat.getColor(context, R.color.badge_med_text))
                holder.tvDiff.setBackgroundResource(R.drawable.bg_badge_med)
            }
            "Difficile" -> {
                holder.tvDiff.setTextColor(ContextCompat.getColor(context, R.color.badge_hard_text))
                holder.tvDiff.setBackgroundResource(R.drawable.bg_badge_hard)
            }
        }

        val emoji = categoryEmoji[recette.categorieNom] ?: "🍽️"
        val bgColor = categoryBgColor[recette.categorieNom] ?: R.color.ci_amber_start
        holder.tvEmoji.text = emoji
        holder.cardImageBg.setBackgroundColor(ContextCompat.getColor(context, bgColor))

        var isFav = false
        holder.btnHeart.setOnClickListener {
            isFav = !isFav
            holder.btnHeart.text = if (isFav) "❤️" else "🤍"
        }

        holder.itemView.setOnClickListener {
            onCardClick(recette)
        }
    }

    override fun getItemCount() = recettes.size

    fun updateList(newList: List<Recette>) {
        recettes = newList
        notifyDataSetChanged()
    }
}