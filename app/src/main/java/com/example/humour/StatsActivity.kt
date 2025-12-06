package com.example.humour

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.humour.databinding.ActivityStatsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDate()
        setupBottomNavigation()
        setupMonthNavigation()

        loadStatsForMonth()
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        binding.dateTextView.text = dateFormat.format(Date())
        updateMonthDisplay(null) // Initial display
    }

    private fun updateMonthDisplay(count: Int?) {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        var text = monthFormat.format(currentCalendar.time)
        if (count != null) {
            text += " (found: $count)"
        }
        binding.tvMonthYear.text = text
    }

    private fun setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            loadStatsForMonth()
        }
        binding.btnNextMonth.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            loadStatsForMonth()
        }
    }

    private fun loadStatsForMonth() {
        val userId = auth.currentUser?.uid ?: return

        val startOfMonth = (currentCalendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfNextMonth = (startOfMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }

        val startOfMonthTimestamp = Timestamp(startOfMonth.time)
        val startOfNextMonthTimestamp = Timestamp(startOfNextMonth.time)

        db.collection("journal_entries")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startOfMonthTimestamp)
            .whereLessThan("timestamp", startOfNextMonthTimestamp)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                Log.d("StatsActivity", "Found $count entries for this month.")
                updateMonthDisplay(count) // Update UI with count

                val moodCounts = hashMapOf(
                    "Great" to 0, "Good" to 0, "Meh" to 0, "Bad" to 0, "Terrible" to 0
                )

                for (doc in documents) {
                    val mood = doc.getString("mood") ?: continue
                    if (moodCounts.containsKey(mood)) {
                        moodCounts[mood] = moodCounts[mood]!! + 1
                    }
                }
                updatePieChart(moodCounts)
            }
            .addOnFailureListener { e ->
                Log.e("StatsActivity", "Error loading stats", e)
                updateMonthDisplay(null)
                Toast.makeText(this, "Error loading stats: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updatePieChart(moodCounts: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val moodOrder = listOf("Great", "Good", "Meh", "Bad", "Terrible")
        val moodColors = mapOf(
            "Great" to Color.parseColor("#4CAF50"),
            "Good" to Color.parseColor("#8BC34A"),
            "Meh" to Color.parseColor("#FFEB3B"),
            "Bad" to Color.parseColor("#FF9800"),
            "Terrible" to Color.parseColor("#F44336")
        )

        for (mood in moodOrder) {
            val count = moodCounts[mood]
            if (count != null && count > 0) {
                entries.add(PieEntry(count.toFloat(), mood))
                colors.add(moodColors[mood]!!)
            }
        }

        if (entries.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.valueTextSize = 0f

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.legend.isEnabled = false
        binding.pieChart.holeRadius = 60f
        binding.pieChart.setHoleColor(Color.TRANSPARENT)
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()
    }

    private fun setupBottomNavigation() {
        binding.homeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(0, 0)
        }
        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }
}
