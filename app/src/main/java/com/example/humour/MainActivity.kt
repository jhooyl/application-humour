package com.example.humour

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This activity will act as a dispatcher.
        // It checks the user's login status and redirects to the appropriate screen.

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // Not logged in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // Already logged in, go to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // Finish this activity so the user can't navigate back to it.
        finish()
    }
}