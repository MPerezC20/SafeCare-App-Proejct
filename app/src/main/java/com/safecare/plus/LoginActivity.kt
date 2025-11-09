package com.safecare.plus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Configurar los enlaces Clickeables
        setupLinks()

        // 2. Configurar botón de Atrás
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // 3. Configurar botón de Iniciar Sesión
        val loginButton: Button = findViewById(R.id.button_login)
        loginButton.setOnClickListener {
            // Simular inicio de sesión y navegar a la pantalla principal
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupLinks() {
        // --- 1. Enlace "Olvidé contraseña" (Aplica color azul + subrayado) ---
        val forgotPasswordText: TextView = findViewById(R.id.forgot_password_link)
        val forgotText = getString(R.string.link_forgot_password)
        val spannableForgot = SpannableString(forgotText)

        val forgotSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "Ir a Restablecer Contraseña", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                // El TextView original probablemente es negro, forzamos el azul
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.login_link_blue)
                ds.bgColor = Color.TRANSPARENT
            }
        }

        spannableForgot.setSpan(forgotSpan, 0, forgotText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        forgotPasswordText.text = spannableForgot
        forgotPasswordText.movementMethod = LinkMovementMethod.getInstance()
        forgotPasswordText.highlightColor = Color.TRANSPARENT

        // --- 2. Enlace "Crear una cuenta" (Mantiene color del XML + subrayado) ---
        // Nota: Asumo que el ID es 'link_create_account' y que ya tiene el color azul en el XML.
        // Si estás usando el LinearLayout con dos TextViews, debes adaptar el ID al del TextView azul.
        val createAccountLink: TextView = findViewById(R.id.link_create_account)
        val createAccountText = getString(R.string.link_create_account)
        val spannableCreate = SpannableString(createAccountText)

        val createSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navegar a la pantalla de registro
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)

                // CLAVE: Usa el color que el TextView ya tiene para evitar sobrescribir con el color del tema
                ds.color = createAccountLink.currentTextColor

                ds.isUnderlineText = true // Solo se añade el subrayado
                ds.bgColor = Color.TRANSPARENT
            }
        }

        spannableCreate.setSpan(createSpan, 0, createAccountText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        createAccountLink.text = spannableCreate
        createAccountLink.movementMethod = LinkMovementMethod.getInstance()
        createAccountLink.highlightColor = Color.TRANSPARENT
    }
}