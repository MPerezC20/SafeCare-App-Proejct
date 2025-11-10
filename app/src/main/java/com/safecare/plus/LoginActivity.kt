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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        setupLinks()

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val loginButton: Button = findViewById(R.id.button_login)
        loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = findViewById<EditText>(R.id.input_email).text.toString().trim()
        val password = findViewById<EditText>(R.id.input_password).text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "El inicio de sesión falló: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupLinks() {
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
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.login_link_blue)
                ds.bgColor = Color.TRANSPARENT
            }
        }

        spannableForgot.setSpan(forgotSpan, 0, forgotText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        forgotPasswordText.text = spannableForgot
        forgotPasswordText.movementMethod = LinkMovementMethod.getInstance()
        forgotPasswordText.highlightColor = Color.TRANSPARENT

        val createAccountLink: TextView = findViewById(R.id.link_create_account)
        val createAccountText = getString(R.string.link_create_account)
        val spannableCreate = SpannableString(createAccountText)

        val createSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = createAccountLink.currentTextColor
                ds.isUnderlineText = true
                ds.bgColor = Color.TRANSPARENT
            }
        }

        spannableCreate.setSpan(createSpan, 0, createAccountText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        createAccountLink.text = spannableCreate
        createAccountLink.movementMethod = LinkMovementMethod.getInstance()
        createAccountLink.highlightColor = Color.TRANSPARENT
    }
}