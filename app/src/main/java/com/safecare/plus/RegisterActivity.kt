package com.safecare.plus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var nameEditText: EditText
    private lateinit var profileSpinner: Spinner
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.input_name)
        profileSpinner = findViewById(R.id.spinner_profile)
        emailEditText = findViewById(R.id.input_email)
        phoneEditText = findViewById(R.id.input_phone)
        passwordEditText = findViewById(R.id.input_password)
        confirmPasswordEditText = findViewById(R.id.input_confirm_password)

        setupLoginText()

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val startButton: Button = findViewById(R.id.button_start_register)
        startButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = nameEditText.text.toString().trim()
        val profile = profileSpinner.selectedItem.toString()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || confirmPassword.isBlank() || profile == "Seleccione su rol") {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, introduce un correo electrónico válido.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            return
        }

        val fullPhoneNumber = "+507$phone"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserToFirestore(name, profile, email, fullPhoneNumber)
                } else {
                    Log.e("RegisterActivity", "User creation failed", task.exception)
                    Toast.makeText(this, "El registro falló: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(name: String, profile: String, email: String, phone: String) {
        val userId = auth.currentUser?.uid ?: return

        val userMap = mutableMapOf<String, Any?>(
            "name" to name,
            "profile" to profile,
            "email" to email,
            "phone" to phone
        )

        firestore.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "User data saved to Firestore")
                Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "Error saving user data", e)
                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupLoginText() {
        val fullText = getString(R.string.sesion_button_clickable)
        val loginText = "Iniciar sesión"
        val startIndex = fullText.indexOf(loginText)

        if (startIndex == -1) return

        val endIndex = startIndex + loginText.length

        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.login_link_blue)
                ds.isUnderlineText = true
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
        sessionButtonText.movementMethod = LinkMovementMethod.getInstance()
        sessionButtonText.highlightColor = ContextCompat.getColor(this, android.R.color.transparent)
    }
}
