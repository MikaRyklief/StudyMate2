package com.example.studymate2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.studymate2.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Use Activity Result API instead of deprecated startActivityForResult
    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                } else {
                    showError("Google sign-in returned null account.")
                }
            } catch (e: ApiException) {
                showError("Google Sign-in failed: ${e.statusCode}")
                Log.e("GoogleSignIn", "ApiException", e)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-In button click
        binding.signInButton.setOnClickListener {
            binding.signInProgress.visibility = View.VISIBLE
            signInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        // If user is already signed in, skip login
        auth.currentUser?.let {
            navigateToMain()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.signInProgress.visibility = View.GONE
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    showError("Firebase authentication failed.")
                    Log.e("FirebaseAuth", "signInWithCredential failed", task.exception)
                }
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        binding.signInProgress.visibility = View.GONE
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
