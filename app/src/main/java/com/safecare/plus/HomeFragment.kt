package com.safecare.plus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.safecare.plus.ble.BLEService

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var wasDeviceEverConnected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val welcomeMessage: TextView = view.findViewById(R.id.welcome_message)
        welcomeMessage.text = "¡Bienvenido de vuelta!"
        
        val logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }

        val viewCamerasButton: Button = view.findViewById(R.id.button_view_cameras)
        viewCamerasButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CamerasFragment())
                .addToBackStack(null)
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_cameras
        }

        // UI de estado BLE
        val bleStatusCard: CardView = view.findViewById(R.id.ble_status_card)
        val bleStatusText: TextView = view.findViewById(R.id.ble_status_text)
        val bleProgress: ProgressBar = view.findViewById(R.id.ble_progress)
        val bleIcon: ImageView = view.findViewById(R.id.ble_icon)
        
        // UI de la tarjeta del dispositivo SOS
        val cardEsp32: CardView = view.findViewById(R.id.card_esp32_sos)
        val connectionStatusText: TextView = view.findViewById(R.id.connection_status)
        val powerSwitch: LinearLayout = view.findViewById(R.id.power_switch)
        val powerIcon: ImageView = view.findViewById(R.id.power_icon_internal)
        val powerText: TextView = view.findViewById(R.id.power_text_internal)

        // UI de Cámara
        val cardCamera: CardView = view.findViewById(R.id.card_camera)
        
        // Estado del switch basado en la conexión real
        var isSwitchOn = true

        powerSwitch.setOnClickListener {
            isSwitchOn = !isSwitchOn
            updateSwitchUI(isSwitchOn, powerSwitch, powerIcon, powerText)
            
            if (isSwitchOn) {
                // Si pasamos a amarillo (ON), intentamos conectar
                BLEService.connectDevice()
                connectionStatusText.text = "Buscando..."
                connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
            } else {
                // Si pasamos a gris (OFF), desconectamos e inmediatamente ponemos "Desconectado"
                BLEService.disconnectDevice()
                connectionStatusText.text = "Desconectado"
                connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            }
        }

        // Simulación de cámara en Home
        val isCameraConnected = false 
        if (isCameraConnected) {
            cardCamera.visibility = View.VISIBLE
        } else {
            cardCamera.visibility = View.GONE
        }

        // Observar el estado de BLE desde el Service
        BLEService.bleStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                BLEService.BLEState.SCANNING -> {
                    if (!wasDeviceEverConnected) {
                        bleStatusCard.visibility = View.VISIBLE
                        bleStatusText.text = "Buscando dispositivo SOS..."
                        bleProgress.visibility = View.VISIBLE
                        bleIcon.visibility = View.GONE
                        cardEsp32.visibility = View.GONE
                    } else {
                        bleStatusCard.visibility = View.GONE
                        cardEsp32.visibility = View.VISIBLE
                        // Solo actualizamos si el switch está en ON
                        if (isSwitchOn) {
                            connectionStatusText.text = "Reconectando..."
                            connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                        }
                    }
                }
                BLEService.BLEState.CONNECTED -> {
                    wasDeviceEverConnected = true
                    bleStatusCard.visibility = View.GONE
                    cardEsp32.visibility = View.VISIBLE
                    connectionStatusText.text = "Conectado"
                    connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    
                    isSwitchOn = true
                    updateSwitchUI(true, powerSwitch, powerIcon, powerText)
                }
                BLEService.BLEState.DISCONNECTED -> {
                    if (wasDeviceEverConnected) {
                        bleStatusCard.visibility = View.GONE
                        cardEsp32.visibility = View.VISIBLE
                        connectionStatusText.text = "Desconectado"
                        connectionStatusText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                        
                        isSwitchOn = false
                        updateSwitchUI(false, powerSwitch, powerIcon, powerText)
                    } else {
                        bleStatusCard.visibility = View.VISIBLE
                        bleStatusText.text = "Dispositivo desconectado"
                        bleProgress.visibility = View.GONE
                        bleIcon.visibility = View.VISIBLE
                        cardEsp32.visibility = View.GONE
                    }
                }
                else -> {
                    if (!wasDeviceEverConnected) {
                        bleStatusCard.visibility = View.GONE
                        cardEsp32.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun updateSwitchUI(enabled: Boolean, container: LinearLayout, icon: ImageView, text: TextView) {
        if (enabled) {
            // Estado ON (Amarillo) - Icono a la izquierda
            container.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            container.setBackgroundResource(R.drawable.rounded_corner)
            
            container.removeAllViews()
            container.addView(icon)
            container.addView(text)
            
            icon.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_orange_light)
            icon.alpha = 1.0f
            text.text = "ON"
            text.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
        } else {
            // Estado OFF (Gris) - Icono a la derecha
            container.removeAllViews()
            
            val newText = TextView(context).apply {
                this.text = "OFF"
                this.textAlignment = View.TEXT_ALIGNMENT_CENTER
                this.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
                this.textSize = 12f
                this.setTypeface(null, android.graphics.Typeface.BOLD)
                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
                params.setMargins(12, 0, 12, 0)
                this.layoutParams = params
            }
            
            container.addView(newText)
            container.addView(icon)
            
            icon.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            icon.alpha = 0.6f
            container.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
        }
    }
}