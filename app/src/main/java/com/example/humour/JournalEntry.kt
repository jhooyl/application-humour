package com.example.humour

import com.google.firebase.Timestamp

// Cette classe sert juste à transporter les données de Firebase vers l'application
data class JournalEntry(
    var id: String = "",         // L'ID sera rempli automatiquement par le code
    val userId: String = "",     // Rempli par Firebase
    val date: String = "",       // Rempli par Firebase
    val mood: String = "",       // Rempli par Firebase
    val notes: String = "",      // Rempli par Firebase
    val timestamp: Timestamp? = null // Sert pour le tri
)