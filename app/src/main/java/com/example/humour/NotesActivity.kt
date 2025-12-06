package com.example.humour

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.humour.databinding.ActivityNotesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var receivedMood: String? = null

    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { it[0] }

            if (!spokenText.isNullOrEmpty()) {
                val currentText = binding.notesEditText.text.toString()
                if (currentText.isEmpty()) {
                    binding.notesEditText.setText(spokenText)
                } else {
                    binding.notesEditText.setText("$currentText $spokenText")
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
        if (isGranted) {
            startSpeechToText()
        } else {
            Toast.makeText(this, "Microphone permission is required to use this feature.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receivedMood = intent.getStringExtra("SELECTED_MOOD")

        setupDate()
        setupClickListeners()
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.dateTextView.text = currentDate
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.doneBtn.setOnClickListener {
            saveToFirebase()
        }

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        binding.statsButton.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        binding.historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.micButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startSpeechToText()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Start speaking...")
        }
        try {
            speechResultLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToFirebase() {
        val notes = binding.notesEditText.text.toString().trim()
        val dateStr = binding.dateTextView.text.toString()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Erreur : Vous n'êtes pas connecté.", Toast.LENGTH_LONG).show()
            return
        }

        binding.doneBtn.isEnabled = false
        binding.doneBtn.text = "Saving..."

        val journalEntry = hashMapOf(
            "userId" to userId,
            "date" to dateStr,
            "mood" to receivedMood,
            "notes" to notes,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("journal_entries")
            .add(journalEntry)
            .addOnSuccessListener {
                Toast.makeText(this, "Sauvegardé avec succès !", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur lors de la sauvegarde : ${e.message}", Toast.LENGTH_LONG).show()
                binding.doneBtn.isEnabled = true
                binding.doneBtn.text = "Done"
            }
    }
}