package com.safecare.plus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencia a los botones de la interfaz
        val loginButton: Button = findViewById(R.id.button_login)
        val registerButton: Button = findViewById(R.id.button_register)

        // Lógica del botón Iniciar Sesión (con Toast e Intent)
        loginButton.setOnClickListener {
            Toast.makeText(this, "Navegando a la pantalla de Inicio de Sesión...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Lógica del botón Registrarse
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * NOTA IMPORTANTE: Para las funcionalidades de CÁMARA y BLUETOOTH (BLE),
     * DEBERÁS implementar la lógica para solicitar los permisos definidos
     * en AndroidManifest.xml en tiempo de ejecución (runtime permissions)
     * tan pronto como el usuario inicie sesión.
     * Esto es CRÍTICO para versiones de Android 6.0 (Marshmallow) en adelante.
     */
}