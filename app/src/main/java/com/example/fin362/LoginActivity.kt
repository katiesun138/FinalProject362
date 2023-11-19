package com.example.fin362

import android.content.Intent
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : FirebaseUIActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is already signed in, proceed to the main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // User is not signed in, show the sign-in screen
            createSignInIntent()
        }
    }
}