package com.safecare.plus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.safecare.plus.ble.BLEService

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

        val welcomeMessage: TextView = view.findViewById(R.id.welcome_message)
        welcomeMessage.text = "¡Bienvenido de vuelta!"
        
        val logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }

        // UI de estado BLE
        val bleStatusCard: CardView = view.findViewById(R.id.ble_status_card)
        val bleStatusText: TextView = view.findViewById(R.id.ble_status_text)
        val bleProgress: ProgressBar = view.findViewById(R.id.ble_progress)
        val bleIcon: ImageView = view.findViewById(R.id.ble_icon)
        val cardEsp32: CardView = view.findViewById(R.id.card_esp32_sos)

        // Observar el estado de BLE desde el Service
        BLEService.bleStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                BLEService.BLEState.SCANNING -> {
                    bleStatusCard.visibility = View.VISIBLE
                    bleStatusText.text = "Buscando dispositivo SOS..."
                    bleProgress.visibility = View.VISIBLE
                    bleIcon.visibility = View.GONE
                    cardEsp32.visibility = View.GONE
                }
                BLEService.BLEState.CONNECTED -> {
                    bleStatusCard.visibility = View.GONE // Ocultamos el banner de búsqueda
                    bleStatusText.text = "ESP32_SOS Conectado"
                    bleProgress.visibility = View.GONE
                    bleIcon.visibility = View.VISIBLE
                    cardEsp32.visibility = View.VISIBLE // Mostramos la tarjeta del dispositivo
                }
                BLEService.BLEState.DISCONNECTED -> {
                    bleStatusCard.visibility = View.VISIBLE
                    bleStatusText.text = "Dispositivo desconectado. Reconectando..."
                    bleProgress.visibility = View.VISIBLE
                    bleIcon.visibility = View.GONE
                    cardEsp32.visibility = View.GONE
                }
                else -> {
                    bleStatusCard.visibility = View.GONE
                    cardEsp32.visibility = View.GONE
                }
            }
        }
    }
}