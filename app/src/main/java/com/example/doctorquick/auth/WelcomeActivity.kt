package com.example.doctorquick.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doctorquick.MainActivity
import com.example.doctorquick.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnSignUp.setOnClickListener {
            navigateToSignUpActivity()
        }

        binding.btnLogin.setOnClickListener {
            navigateToSignInActivity()
        }
    }

    private fun navigateToSignInActivity() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in, fetch user type and navigate to MainActivity
            firestore.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
                val userType = document.getString("userType")
                navigateToMain(userType)
            }.addOnFailureListener {
                // Handle error, for example logging or a fallback action
                // Optionally navigate to SignInActivity if fetching userType fails
                navigateToSignInActivityFallback()
            }
        } else {
            // No user logged in, navigate to SignInActivity
            navigateToSignInActivityFallback()
        }
    }

    private fun navigateToMain(userType: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("userType", userType)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSignInActivityFallback() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }
}
