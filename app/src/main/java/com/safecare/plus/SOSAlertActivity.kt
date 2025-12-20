package com.safecare.plus

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SOSAlertActivity : AppCompatActivity() {

    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mostrar sobre la pantalla de bloqueo y mantener encendida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_sos_alert)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        startStrongVibration()

        findViewById<Button>(R.id.btn_dismiss).setOnClickListener {
            vibrator?.cancel()
            finish()
        }

        findViewById<Button>(R.id.btn_check_cameras).setOnClickListener {
            vibrator?.cancel()
            // Iniciar HomeActivity y decirle que abra el fragmento de cámaras
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("OPEN_CAMERAS", true)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun startStrongVibration() {
        val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000) // Vibra 1s, pausa 0.5s...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 significa repetir
        } else {
            vibrator?.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vibrator?.cancel()
    }
}