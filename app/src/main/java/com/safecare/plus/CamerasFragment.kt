package com.safecare.plus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class CamerasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_cameras, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lógica para el botón de retroceso
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        // Elementos de la UI
        val emptyState: LinearLayout = view.findViewById(R.id.empty_camera_state)
        val cameraCard: CardView = view.findViewById(R.id.card_camera_detail)

        // --- LÓGICA DE CONEXIÓN ---
        // Aquí es donde controlarás si la cámara está conectada o no
        val isCameraConnected = false // Cambia esto a TRUE para simular la conexión

        if (isCameraConnected) {
            emptyState.visibility = View.GONE
            cameraCard.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.VISIBLE
            cameraCard.visibility = View.GONE
        }
    }
}
