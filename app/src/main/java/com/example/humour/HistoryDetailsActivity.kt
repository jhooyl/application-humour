package com.example.humour

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.humour.databinding.ActivityHistoryDetailsBinding

class HistoryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Récupérer les données envoyées par le clic
        val mood = intent.getStringExtra("MOOD") ?: "Unknown"
        val date = intent.getStringExtra("DATE") ?: "Unknown Date"
        val notes = intent.getStringExtra("NOTES") ?: ""

        // 2. Afficher les données dans les vues
        binding.tvDate.text = date
        binding.tvMood.text = mood
        binding.tvNotes.text = if (notes.isNotEmpty()) notes else "No additional notes."
        binding.dateTextView.text = date // La date dans le header

        // 3. Mettre la bonne image
        val iconRes = when (mood) {
            "Great" -> R.drawable.star
            "Good" -> R.drawable.smiling
            "Meh" -> R.drawable.neutral
            "Bad" -> R.drawable.confused
            "Terrible" -> R.drawable.disappointed
            else -> R.drawable.smiling
        }
        binding.ivEmoji.setImageResource(iconRes)

        // 4. Bouton retour
        binding.backButton.setOnClickListener {
            finish()
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.statsButton.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

    }
}