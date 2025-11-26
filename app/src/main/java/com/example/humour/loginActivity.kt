package com.example.humour

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.humour.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser Firebase Authentication
        auth = FirebaseAuth.getInstance()

        setupClickListeners()

        // Vérifier si l'utilisateur est déjà connecté
        checkCurrentUser()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            handleLogin()
        }

        binding.signUpText.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun handleLogin() {
        val email = binding.emailEditText.editText!!.text.toString().trim()
        val password = binding.passwordEditText.editText!!.text.toString().trim()

        // Validation basique
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Afficher un loading
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Logging in..."

        // Connexion avec Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Connexion réussie
                    Toast.makeText(this, "Login successful! 🎉", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    // Échec de la connexion
                    val errorMessage = task.exception?.message ?: "Login failed"
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }

                // Réactiver le bouton
                binding.loginButton.isEnabled = true
                binding.loginButton.text = "Login"
            }
    }

    private fun checkCurrentUser() {
        // Si l'utilisateur est déjà connecté, aller directement à l'écran principal
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMainActivity() {
        // Remplacez MainActivity par le nom de votre écran principal
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}