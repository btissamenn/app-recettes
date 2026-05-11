package com.example.app_recettes

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.app_recettes.database.DatabaseHelper
import com.example.app_recettes.models.Recette
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etSearch: EditText
    private lateinit var columnLeft: LinearLayout
    private lateinit var columnRight: LinearLayout
    private lateinit var bottomNav: BottomNavigationView

    // ← Now all TextViews, not LinearLayout
    private lateinit var catTout: TextView
    private lateinit var catEntrees: TextView
    private lateinit var catPlats: TextView
    private lateinit var catDesserts: TextView
    private lateinit var catBoissons: TextView

    private var allRecettes: List<Recette> = emptyList()
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        bindViews()
        setupCategoryTabs()
        setupSearch()
        setupBottomNav()
        insertTestDataIfEmpty()
        loadRecettes()
    }

    private fun bindViews() {
        etSearch    = findViewById(R.id.etSearch)
        columnLeft  = findViewById(R.id.columnLeft)
        columnRight = findViewById(R.id.columnRight)
        bottomNav   = findViewById(R.id.bottomNav)
        catTout     = findViewById(R.id.catTout)
        catEntrees  = findViewById(R.id.catEntrees)
        catPlats    = findViewById(R.id.catPlats)
        catDesserts = findViewById(R.id.catDesserts)
        catBoissons = findViewById(R.id.catBoissons)

        findViewById<TextView>(R.id.tvVoirTout).setOnClickListener {
            startActivity(Intent(this, RecipeListActivity::class.java))
        }
    }

    // ─── TEST DATA ────────────────────────────────────────────────────────────

    private fun insertTestDataIfEmpty() {
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM recipes", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            db.execSQL("""
                INSERT INTO recipes (name, ingredients, description, category,
                                   difficulte, temps_minutes, nb_personnes, etapes)
                VALUES
                ('Tarte tatin', 'Pommes, beurre, caramel', 'Une délicieuse tarte',
                 'Dessert', 'Facile', 45, 6, '1. Préchauffer le four\n2. Caraméliser les pommes\n3. Enfourner'),
                ('Tajine de poulet', 'Poulet, citron, olives', 'Tajine marocain traditionnel',
                 'Plat', 'Moyen', 90, 4, '1. Faire revenir le poulet\n2. Ajouter les épices\n3. Mijoter'),
                ('Salade niçoise', 'Thon, tomates, olives, oeufs', 'Salade fraîche',
                 'Entrée', 'Facile', 20, 2, '1. Cuire les oeufs\n2. Assembler la salade'),
                ('Jus avocat', 'Avocat, lait, sucre', 'Boisson crémeuse',
                 'Boisson', 'Facile', 10, 2, '1. Mixer tous les ingrédients')
            """.trimIndent())
        }
    }

    // ─── LOAD FROM DB ─────────────────────────────────────────────────────────

    private fun loadRecettes() {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT id, name, category, difficulte, 
                   temps_minutes, nb_personnes, etapes, 
                   date_ajout, ingredients, image_path
            FROM recipes
            ORDER BY date_ajout DESC
        """.trimIndent()

        val cursor: Cursor = db.rawQuery(query, null)
        val list = mutableListOf<Recette>()

        while (cursor.moveToNext()) {
            val id          = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nom         = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: ""
            val category    = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val difficulte  = cursor.getString(cursor.getColumnIndexOrThrow("difficulte"))
            val temps       = cursor.getInt(cursor.getColumnIndexOrThrow("temps_minutes"))
            val nbPersonnes = cursor.getInt(cursor.getColumnIndexOrThrow("nb_personnes"))
            val etapes      = cursor.getString(cursor.getColumnIndexOrThrow("etapes"))
            val dateAjout   = cursor.getString(cursor.getColumnIndexOrThrow("date_ajout"))
            val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")) ?: ""
            val imagePath   = cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
            val ingredientsPreview = getIngredientsPreview(id).ifEmpty { ingredients }

            list.add(Recette(
                id = id,
                nom = nom,
                categorieId = null,
                categorieNom = category,
                difficulte = difficulte,
                tempsMinutes = temps,
                nbPersonnes = nbPersonnes,
                etapes = etapes,
                dateAjout = dateAjout,
                ingredientsPreview = ingredientsPreview,
                imagePath = imagePath
            ))
        }
        cursor.close()
        allRecettes = list
        displayRecettes(list)
    }

    private fun getIngredientsPreview(recetteId: Int): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT nom FROM ingredients_detail WHERE recette_id = ? LIMIT 3",
            arrayOf(recetteId.toString())
        )
        val noms = mutableListOf<String>()
        while (cursor.moveToNext()) {
            noms.add(cursor.getString(0))
        }
        cursor.close()
        return noms.joinToString(", ")
    }

    // ─── DISPLAY STAGGERED CARDS ──────────────────────────────────────────────

    private fun displayRecettes(list: List<Recette>) {
        columnLeft.removeAllViews()
        columnRight.removeAllViews()

        list.forEachIndexed { index, recette ->
            val cardView = layoutInflater.inflate(R.layout.item_recette, null, false)

            val tvName   = cardView.findViewById<TextView>(R.id.tvRecetteName)
            val tvDesc   = cardView.findViewById<TextView>(R.id.tvRecetteDesc)
            val tvTemps  = cardView.findViewById<TextView>(R.id.tvTemps)
            val tvDiff   = cardView.findViewById<TextView>(R.id.tvDifficulte)
            val tvEmoji  = cardView.findViewById<TextView>(R.id.tvEmoji)
            val imgBg    = cardView.findViewById<View>(R.id.cardImageBg)
            val btnHeart = cardView.findViewById<TextView>(R.id.btnHeart)
            val ivImage  = cardView.findViewById<ImageView>(R.id.ivRecetteImage)

            tvName.text  = recette.nom
            tvDesc.text  = recette.ingredientsPreview.ifEmpty { "Aucun ingrédient" }
            tvTemps.text = formatTemps(recette.tempsMinutes)
            tvDiff.text  = recette.difficulte ?: "—"

            when (recette.difficulte) {
                "Facile" -> {
                    tvDiff.setTextColor(getColor(R.color.badge_easy_text))
                    tvDiff.setBackgroundResource(R.drawable.bg_badge_easy)
                }
                "Moyen" -> {
                    tvDiff.setTextColor(getColor(R.color.badge_med_text))
                    tvDiff.setBackgroundResource(R.drawable.bg_badge_med)
                }
                "Difficile" -> {
                    tvDiff.setTextColor(getColor(R.color.badge_hard_text))
                    tvDiff.setBackgroundResource(R.drawable.bg_badge_hard)
                }
            }

            // ── Image or emoji placeholder ──────────────────────────────────
            if (!recette.imagePath.isNullOrEmpty()) {
                val file = File(recette.imagePath)
                if (file.exists()) {
                    ivImage.setImageURI(Uri.fromFile(file))
                    ivImage.visibility  = View.VISIBLE
                    tvEmoji.visibility  = View.GONE
                    imgBg.visibility    = View.GONE
                } else {
                    ivImage.visibility = View.GONE
                    tvEmoji.visibility = View.VISIBLE
                    imgBg.visibility   = View.VISIBLE
                    tvEmoji.text = emojiForCategory(recette.categorieNom)
                    imgBg.setBackgroundColor(getColor(colorForCategory(recette.categorieNom)))
                }
            } else {
                ivImage.visibility = View.GONE
                tvEmoji.visibility = View.VISIBLE
                imgBg.visibility   = View.VISIBLE
                tvEmoji.text = emojiForCategory(recette.categorieNom)
                imgBg.setBackgroundColor(getColor(colorForCategory(recette.categorieNom)))
            }

            var isFav = false
            btnHeart.setOnClickListener {
                isFav = !isFav
                btnHeart.text = if (isFav) "❤️" else "🤍"
            }

            cardView.setOnClickListener {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("recette_id", recette.id)
                startActivity(intent)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = (16 * resources.displayMetrics.density).toInt()
            cardView.layoutParams = params

            if (index % 2 == 0) columnLeft.addView(cardView)
            else columnRight.addView(cardView)
        }
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecettes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterRecettes(query: String) {
        val filtered = allRecettes.filter { recette ->
            val matchesSearch = recette.nom.contains(query, ignoreCase = true) ||
                    recette.ingredientsPreview.contains(query, ignoreCase = true)
            val matchesCategory = currentCategory == null ||
                    recette.categorieNom == currentCategory
            matchesSearch && matchesCategory
        }
        displayRecettes(filtered)
    }

    // ─── CATEGORY TABS ────────────────────────────────────────────────────────

    private fun setupCategoryTabs() {
        catTout.setOnClickListener     { selectCategory(null) }
        catEntrees.setOnClickListener  { selectCategory("Entrée") }
        catPlats.setOnClickListener    { selectCategory("Plat") }
        catDesserts.setOnClickListener { selectCategory("Dessert") }
        catBoissons.setOnClickListener { selectCategory("Boisson") }
    }

    private fun selectCategory(category: String?) {
        currentCategory = category
        etSearch.setText("")
        updateCategoryUI(category)
        filterRecettes("")
    }

    private fun updateCategoryUI(selected: String?) {
        // Reset all to inactive
        listOf(catTout, catEntrees, catPlats, catDesserts, catBoissons).forEach {
            it.setBackgroundResource(R.drawable.bg_cat_inactive_text)
            it.setTextColor(android.graphics.Color.parseColor("#555555"))
        }
        // Set active
        when (selected) {
            null      -> { catTout.setBackgroundResource(R.drawable.bg_cat_active); catTout.setTextColor(android.graphics.Color.WHITE) }
            "Entrée"  -> { catEntrees.setBackgroundResource(R.drawable.bg_cat_active); catEntrees.setTextColor(android.graphics.Color.WHITE) }
            "Plat"    -> { catPlats.setBackgroundResource(R.drawable.bg_cat_active); catPlats.setTextColor(android.graphics.Color.WHITE) }
            "Dessert" -> { catDesserts.setBackgroundResource(R.drawable.bg_cat_active); catDesserts.setTextColor(android.graphics.Color.WHITE) }
            "Boisson" -> { catBoissons.setBackgroundResource(R.drawable.bg_cat_active); catBoissons.setTextColor(android.graphics.Color.WHITE) }
        }
    }

    // ─── BOTTOM NAV ───────────────────────────────────────────────────────────

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_accueil
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_accueil -> true
                R.id.nav_ajouter -> {
                    startActivity(Intent(this, AddRecipeActivity::class.java))
                    true
                }
                R.id.nav_liste -> {
                    startActivity(Intent(this, RecipeListActivity::class.java))
                    true
                }
                R.id.nav_favoris -> true
                else -> false
            }
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private fun formatTemps(minutes: Int?): String {
        if (minutes == null || minutes == 0) return "? min"
        return if (minutes >= 60) {
            val h = minutes / 60
            val m = minutes % 60
            if (m == 0) "${h}h" else "${h}h${m}min"
        } else "$minutes min"
    }

    private fun emojiForCategory(cat: String?) = when (cat) {
        "Entrée"  -> "🥙"
        "Plat"    -> "🍲"
        "Dessert" -> "🎂"
        "Boisson" -> "🥤"
        "Snack"   -> "🥨"
        else      -> "🍽️"
    }

    private fun colorForCategory(cat: String?) = when (cat) {
        "Entrée"  -> R.color.ci_green_start
        "Plat"    -> R.color.ci_peach_start
        "Dessert" -> R.color.ci_pink_start
        "Boisson" -> R.color.ci_amber_start
        else      -> R.color.ci_amber_start
    }

    override fun onResume() {
        super.onResume()
        loadRecettes()
    }
}