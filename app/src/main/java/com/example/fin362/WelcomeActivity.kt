package com.example.fin362

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val welcomeScreenShown = prefs.getBoolean(WELCOME_SCREEN_SHOWN, false)
        if (!welcomeScreenShown) {
            // The welcome screen has not been shown yet
            setContentView(R.layout.activity_welcome)
            val startButton = findViewById<Button>(R.id.startButton)
            startButton.setOnClickListener { // Set the flag to indicate that the welcome screen has been shown
                prefs.edit().putBoolean(WELCOME_SCREEN_SHOWN, true)
                    .apply()

                // Proceed to the next activity
                val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            // The welcome screen has already been shown, skip it
            proceedToNextActivity()
        }
    }

    private fun proceedToNextActivity() {
        // Proceed to the next activity
        val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val PREFS_NAME = "MyPrefsFile"
        private const val WELCOME_SCREEN_SHOWN = "WelcomeScreenShown"
    }
}
