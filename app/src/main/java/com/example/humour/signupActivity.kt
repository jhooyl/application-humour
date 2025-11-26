package com.example.humour

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.humour.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            handleSignUp()
        }
    }

    private fun handleSignUp() {
        val email = binding.emailEditText.editText!!.text.toString().trim()
        val password = binding.passwordEditText.editText!!.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.editText!!.text.toString().trim()

        if (validateInputs(email, password, confirmPassword)) {
            createUserWithFirebase(email, password)
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        // Reset errors
        binding.emailEditText.error = null
        binding.passwordEditText.error = null
        binding.confirmPasswordEditText.error = null

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Please enter a valid email"
            return false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordEditText.error = "Please confirm your password"
            return false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun createUserWithFirebase(email: String, password: String) {
        // Afficher un loading
        binding.signupButton.isEnabled = false
        binding.signupButton.text = "Creating account..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inscription réussie
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToFirestore(user.uid, email)
                        Toast.makeText(this, "Account created successfully! 🎉", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                } else {
                    // Échec de l'inscription
                    val errorMessage = task.exception?.message ?: "Registration failed"

                    when {
                        errorMessage.contains("email address is already in use") -> {
                            binding.emailEditText.error = "This email is already registered"
                        }
                        errorMessage.contains("invalid email") -> {
                            binding.emailEditText.error = "Invalid email format"
                        }
                        errorMessage.contains("password is too weak") -> {
                            binding.passwordEditText.error = "Password is too weak"
                        }
                        else -> {
                            Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Réactiver le bouton
                binding.signupButton.isEnabled = true
                binding.signupButton.text = "Sign Up"
            }
    }

    private fun saveUserToFirestore(userId: String, email: String) {
        val user = hashMapOf(
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "moodHistory" to arrayListOf<String>()
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                println("User saved to Firestore: $email")
            }
            .addOnFailureListener { e ->
                println("Error saving user to Firestore: $e")
            }
    }

    private fun navigateToMainActivity() {
        // Remplacez MainActivity par le nom de votre écran principal
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}