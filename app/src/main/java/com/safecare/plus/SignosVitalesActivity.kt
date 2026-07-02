package com.safecare.plus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class SignosVitalesActivity : AppCompatActivity() {

    private lateinit var tvHeartValue: TextView
    private lateinit var tvHeartStatus: TextView
    private lateinit var tvOxygenValue: TextView
    private lateinit var tvTempValue: TextView
    private lateinit var tvInfoTitle: TextView

    private val ESP32_URL = "http://192.168.0.50/datos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signos_vitales)

        tvHeartValue = findViewById(R.id.tv_heart_value)
        tvHeartStatus = findViewById(R.id.tv_heart_status)
        tvOxygenValue = findViewById(R.id.tv_oxygen_value)
        tvTempValue = findViewById(R.id.tv_temp_value)
        tvInfoTitle = findViewById(R.id.tv_info_title)

        val backButton: ImageButton = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }

        val btnActualizar: Button = findViewById(R.id.btn_actualizar)
        btnActualizar.setOnClickListener {
            actualizarSignosVitales()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_vitales
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_cameras -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("OPEN_CAMERAS", true)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_vitales -> {
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("OPEN_PROFILE", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarSignosVitales()
    }

    private fun actualizarSignosVitales() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(ESP32_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                connection.disconnect()

                val json = JSONObject(response.toString())
                val bpm = json.getInt("bpm")
                val spo2 = json.getInt("spo2")
                val temperatura = json.getDouble("temperatura")

                withContext(Dispatchers.Main) {
                    mostrarValores(bpm, spo2, temperatura)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SignosVitalesActivity,
                        "No se pudo conectar con el brazalete",
                        Toast.LENGTH_SHORT
                    ).show()
                    usarDatosSimulados()
                }
            }
        }
    }

    private fun mostrarValores(bpm: Int, spo2: Int, temperatura: Double) {
        tvHeartValue.text = "$bpm BPM"
        tvOxygenValue.text = "$spo2%"
        tvTempValue.text = "${"%.1f".format(temperatura)} C°"

        if (bpm in 60..100 && spo2 in 95..100 && temperatura in 36.0..37.2) {
            tvHeartStatus.text = "Normal"
            tvInfoTitle.text = getString(R.string.vitales_stable)
        } else {
            tvHeartStatus.text = "Revisar"
            tvInfoTitle.text = "Algunos signos requieren atención"
        }
    }

    private fun usarDatosSimulados() {
        val heartRate = Random.nextInt(65, 101)
        val oxygen = Random.nextInt(95, 100)
        val temp = 36.0 + Random.nextDouble() * 1.5

        tvHeartValue.text = "$heartRate BPM"
        tvOxygenValue.text = "$oxygen%"
        tvTempValue.text = "${"%.1f".format(temp)} C°"

        if (heartRate in 60..100 && oxygen in 95..100 && temp in 36.0..37.5) {
            tvHeartStatus.text = "Normal"
            tvInfoTitle.text = getString(R.string.vitales_stable)
        } else {
            tvHeartStatus.text = "Alerta"
            tvInfoTitle.text = "Algunos signos requieren atención"
        }
    }
}
