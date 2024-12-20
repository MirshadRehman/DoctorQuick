package com.example.doctorquick.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doctorquick.MainActivity
import com.example.doctorquick.R
import com.example.doctorquick.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnSignUp.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val userType = binding.spUserType.selectedItem.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && userType.isNotEmpty()) {
                registerUser(name, email, password, userType)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupSpinner()
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.user_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spUserType.adapter = adapter
        }
    }

    private fun registerUser(name: String, email: String, password: String, userType: String) {
        // Show the ProgressBar when the registration process starts
        binding.progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    storeUserInFirestore(userId, name, email, userType)
                } else {
                    // Hide the ProgressBar if registration fails
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun storeUserInFirestore(userId: String, name: String, email: String, userType: String) {
        val user = hashMapOf(
            "userId" to userId,
            "userName" to name,
            "email" to email,
            "userType" to userType
        )

        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE  // Hide ProgressBar on success
                navigateToMain(userType)  // Navigate to MainActivity and pass userType
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE  // Hide ProgressBar on failure
                Toast.makeText(this, "Failed to save user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMain(userType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userType", userType)
        startActivity(intent)
        finish()  // Finish SignUpActivity to remove it from the back stack
    }

}
