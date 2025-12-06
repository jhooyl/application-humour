package com.example.humour

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.humour.databinding.ActivityChoseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ChoseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChoseBinding
    private var selectedMood: String? = null
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private val moodLayouts by lazy {
        listOf(
            binding.moodGreatLayout,
            binding.moodGoodLayout,
            binding.moodThenLayout,
            binding.moodBadLayout,
            binding.moodTerribleLayout
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupDate()
        setupClickListeners()
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.dateTextView.text = currentDate
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.moodGreatLayout.setOnClickListener { handleMoodSelection("Great", it) }
        binding.moodGoodLayout.setOnClickListener { handleMoodSelection("Good", it) }
        binding.moodThenLayout.setOnClickListener { handleMoodSelection("Meh", it) }
        binding.moodBadLayout.setOnClickListener { handleMoodSelection("Bad", it) }
        binding.moodTerribleLayout.setOnClickListener { handleMoodSelection("Terrible", it) }

        binding.nextButton.setOnClickListener {
            if (selectedMood != null) {
                saveMoodToFirebaseAndContinue()
            } else {
                Toast.makeText(this, "Please select a mood first!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.bottomNav.findViewById<View>(R.id.home_button).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        binding.bottomNav.findViewById<View>(R.id.stats_button).setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        binding.bottomNav.findViewById<View>(R.id.history_button).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleMoodSelection(mood: String, selectedView: View) {
        selectedMood = mood
        for (layout in moodLayouts) {
            layout.alpha = if (layout == selectedView) 1.0f else 0.5f
        }
    }

    // Remplace ton ancienne méthode saveMoodToFirebaseAndContinue par celle-ci :
    private fun saveMoodToFirebaseAndContinue() {
        // On ne vérifie pas Firebase tout de suite pour éviter le blocage
        val currentDateStr = binding.dateTextView.text.toString()

        // Création de l'intention pour aller vers NotesActivity
        val intent = Intent(this, NotesActivity::class.java)

        // On "attache" les informations à l'intent pour les récupérer après
        intent.putExtra("SELECTED_MOOD", selectedMood)
        intent.putExtra("SELECTED_DATE", currentDateStr)

        startActivity(intent)
    }
}