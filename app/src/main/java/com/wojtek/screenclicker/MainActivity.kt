package com.wojtek.screenclicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(AutoClickService.PREFS, MODE_PRIVATE)
        val input = findViewById<EditText>(R.id.inputInterval)
        input.setText(
            prefs.getLong(AutoClickService.KEY_INTERVAL, AutoClickService.DEFAULT_INTERVAL_MS)
                .toString()
        )

        findViewById<Button>(R.id.btnSaveInterval).setOnClickListener {
            val value = input.text.toString().toLongOrNull()
            if (value == null || value < AutoClickService.MIN_INTERVAL_MS) {
                toast(getString(R.string.interval_invalid, AutoClickService.MIN_INTERVAL_MS))
                return@setOnClickListener
            }
            prefs.edit().putLong(AutoClickService.KEY_INTERVAL, value).apply()
            AutoClickService.instance?.refreshPanel()
            toast(getString(R.string.interval_saved, value))
        }

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            val service = AutoClickService.instance
            if (service == null) {
                toast(getString(R.string.service_not_enabled))
            } else {
                service.toggleOverlay()
            }
        }
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
