package com.safecare.plus

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val welcomeMessage: TextView = view.findViewById(R.id.welcome_message)
        if (user != null) {
            welcomeMessage.text = "¡Bienvenido de vuelta, ${user.email}!"
        } else {
            welcomeMessage.text = "¡Bienvenido de vuelta!"
        }
        
        val logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }
    }
}