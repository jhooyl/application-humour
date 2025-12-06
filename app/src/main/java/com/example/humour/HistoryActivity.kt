package com.example.humour

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.humour.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    // 1. On prépare la liste vide
    private val historyList = mutableListOf<JournalEntry>()

    // 2. On déclare l'adapter (mais on l'initialise plus tard)
    private lateinit var adapter: HistoryAdapter

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDate()
        setupBottomNavigation() // Ta navigation du bas

        // 3. On configure le RecyclerView
        setupRecyclerView()

        // 4. On va chercher les données
        fetchHistoryData()
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        binding.dateTextView.text = dateFormat.format(Date())
    }

    private fun setupRecyclerView() {
        // C'est ici qu'on branche l'adapter
        adapter = HistoryAdapter(historyList)
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = adapter
    }

    private fun fetchHistoryData() {
        val userId = auth.currentUser?.uid

        // Si personne n'est connecté, on arrête
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("journal_entries")
            .whereEqualTo("userId", userId) // Filtre : seulement MES notes
            .orderBy("timestamp", Query.Direction.DESCENDING) // Tri : du plus récent au plus vieux
            .get()
            .addOnSuccessListener { documents ->
                // 1. On vide la liste pour éviter les doublons
                historyList.clear()

                // 2. Pour chaque document trouvé dans Firebase...
                for (document in documents) {
                    // On transforme le JSON Firebase en objet JournalEntry
                    val entry = document.toObject(JournalEntry::class.java)
                    // On récupère l'ID automatique de Firebase et on le stocke
                    entry.id = document.id
                    // On ajoute à la liste
                    historyList.add(entry)
                }

                // 3. Gestion de l'affichage (Liste vide ou pleine ?)
                if (historyList.isEmpty()) {
                    binding.historyRecyclerView.visibility = View.GONE
                    binding.emptyStateView.visibility = View.VISIBLE
                } else {
                    binding.historyRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateView.visibility = View.GONE

                    // 4. C'EST ICI LA SOLUTION : On dit à l'adapter que les données ont changé
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupBottomNavigation() {
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            // Animation fluide
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

        // History : on ne fait rien car on y est déjà
    }
}