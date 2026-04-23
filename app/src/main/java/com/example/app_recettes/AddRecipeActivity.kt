package com.example.app_recettes

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.app_recettes.database.DatabaseHelper
import com.example.app_recettes.models.Recipe
import java.io.File
import java.io.FileOutputStream

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var db: DatabaseHelper
    private var selectedImageUri: String? = null
    private lateinit var imgRecipe: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        db = DatabaseHelper(this)
        imgRecipe = findViewById(R.id.imgRecipe)

        val recipeId = intent.getIntExtra("recipe_id", -1)
        if (recipeId != -1) {
            loadRecipeForEdit(recipeId)
            supportActionBar?.title = "Modifier la recette"
            findViewById<Button>(R.id.btnSaveRecipe).text = "Mettre à jour"
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ingredientsContainer = findViewById(R.id.ingredientsContainer)
        val btnAddIngredient = findViewById<Button>(R.id.btnAddIngredient)
        val btnChoosePhoto = findViewById<Button>(R.id.btnChoosePhoto)
        val spinnerTime = findViewById<Spinner>(R.id.spinnerTime)
        val spinnerPortions = findViewById<Spinner>(R.id.spinnerPortions)

        val times = (5..120 step 5).map { "$it" }
        spinnerTime.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, times)

        val portions = (1..10).map { "$it" }
        spinnerPortions.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, portions)

        btnAddIngredient.setOnClickListener { addIngredientField() }
        addIngredientField()

        btnChoosePhoto.setOnClickListener {
            pickImage()
        }

        findViewById<Button>(R.id.btnSaveRecipe).setOnClickListener {
            saveRecipe()
        }
    }

    private val pickImageLauncher = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    private val getImage = registerForActivityResult(pickImageLauncher) { uri ->
        uri?.let {
            val localPath = saveImageToInternalStorage(it)
            selectedImageUri = localPath
            imgRecipe.setImageURI(it)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "recipe_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun pickImage() {
        getImage.launch("image/*")
    }

    private fun addIngredientField() {
        val editText = EditText(this)
        editText.hint = getString(R.string.ingredient_hint, ingredientsContainer.childCount + 1)
        ingredientsContainer.addView(editText)
    }

    private fun saveRecipe() {
        val name = findViewById<EditText>(R.id.etRecipeName).text.toString()
        val description = findViewById<EditText>(R.id.etSteps).text.toString()
        
        val ingredientsList = mutableListOf<String>()
        for (i in 0 until ingredientsContainer.childCount) {
            val child = ingredientsContainer.getChildAt(i)
            if (child is EditText) {
                ingredientsList.add(child.text.toString())
            }
        }
        val ingredients = ingredientsList.joinToString(", ")

        if (name.isNotEmpty()) {
            val recipeId = intent.getIntExtra("recipe_id", -1)
            val recipe = Recipe(if (recipeId != -1) recipeId else 0, name, ingredients, description, "Tout", false, selectedImageUri)
            
            if (recipeId != -1) {
                db.updateRecipe(recipe)
                Toast.makeText(this, "Recette mise à jour !", Toast.LENGTH_SHORT).show()
            } else {
                db.addRecipe(recipe)
                Toast.makeText(this, getString(R.string.recipe_saved), Toast.LENGTH_SHORT).show()
            }
            finish()
        } else {
            Toast.makeText(this, getString(R.string.enter_name_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecipeForEdit(id: Int) {
        val recipe = db.getRecipeById(id)
        if (recipe != null) {
            findViewById<EditText>(R.id.etRecipeName).setText(recipe.name)
            findViewById<EditText>(R.id.etSteps).setText(recipe.description)
            
            val ingredients = recipe.ingredients.split(", ")
            ingredientsContainer.removeAllViews()
            ingredients.forEach { 
                val editText = EditText(this)
                editText.setText(it)
                ingredientsContainer.addView(editText)
            }
            
            if (recipe.imagePath != null) {
                selectedImageUri = recipe.imagePath
                val file = File(recipe.imagePath)
                if (file.exists()) {
                    imgRecipe.setImageURI(Uri.fromFile(file))
                }
            }
        }
    }
}