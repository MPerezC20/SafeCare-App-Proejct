package com.safecare.plus

import android.os.Bundle
import android.text.SpannableString // Importación necesaria
import android.text.Spanned // Importación necesaria
import android.text.TextPaint // Importación necesaria
import android.text.method.LinkMovementMethod // Importación necesaria
import android.text.style.ClickableSpan // Importación necesaria
import android.view.View // Importación necesaria
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView // Importación necesaria
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // Importación necesaria
import android.graphics.Color // Importación necesaria si usas Color.TRANSPARENT

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Configurar el Texto "Iniciar Sesión" como Clickeable, Azul y Subrayado
        setupLoginText()

        // 2. Configurar botón de Atrás
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // 3. Configurar botón de Iniciar (Registro)
        val startButton: Button = findViewById(R.id.button_start_register)
        startButton.setOnClickListener {
            Toast.makeText(this, "Simulando registro de usuario...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLoginText() {
        // Usamos la cadena simple definida en strings.xml
        val fullText = getString(R.string.sesion_button_clickable)
        val loginText = "Iniciar sesión"
        val startIndex = fullText.indexOf(loginText)

        if (startIndex == -1) return

        val endIndex = startIndex + loginText.length

        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Aquí va la lógica para navegar a la LoginActivity
                Toast.makeText(this@RegisterActivity, "¡Clic en Iniciar Sesión!", Toast.LENGTH_SHORT).show()
            }

            // MÉTODO CLAVE: Define el estilo (color y subrayado)
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)

                // Color Azul (Usando R.color.login_link_blue que definiste en colors.xml)
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.login_link_blue)

                // Subrayado
                ds.isUnderlineText = true

                // Opcional: Quitar cualquier color de fondo o negrita que Android aplique por defecto
                ds.isFakeBoldText = false
                ds.bgColor = Color.TRANSPARENT
            }
        }

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val sessionButtonText: TextView = findViewById(R.id.text_above_button)
        sessionButtonText.text = spannableString

        // HABILITAR CLICS: Permite que el TextView responda al toque en el ClickableSpan
        sessionButtonText.movementMethod = LinkMovementMethod.getInstance()

        // Quitar el sombreado gris/amarillo que aparece al presionar
        sessionButtonText.highlightColor = ContextCompat.getColor(this, android.R.color.transparent)
    }
}