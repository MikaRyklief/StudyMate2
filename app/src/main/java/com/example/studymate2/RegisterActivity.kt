package com.example.studymate2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.studymate2.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val confirm = binding.inputConfirmPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Snackbar.make(binding.root, "Please fill in all fields.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Snackbar.make(binding.root, "Passwords do not match.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.registerProgress.visibility = android.view.View.VISIBLE
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.registerProgress.visibility = android.view.View.GONE
                    if (task.isSuccessful) {
                        Snackbar.make(binding.root, "Account created successfully!", Snackbar.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Snackbar.make(binding.root, "Registration failed.", Snackbar.LENGTH_SHORT).show()
                        Log.e("Register", "Failed", task.exception)
                    }
                }
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
