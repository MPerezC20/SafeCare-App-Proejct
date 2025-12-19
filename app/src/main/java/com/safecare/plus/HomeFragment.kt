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
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
        
        // Nueva UI de la tarjeta del dispositivo
        val cardEsp32: CardView = view.findViewById(R.id.card_esp32_sos)
        val powerSwitch: LinearLayout = view.findViewById(R.id.power_switch)
        val powerIcon: ImageView = powerSwitch.getChildAt(0) as ImageView
        val powerText: TextView = powerSwitch.getChildAt(1) as TextView

        // Variable para controlar el estado del switch visualmente
        var isDeviceEnabled = true

        powerSwitch.setOnClickListener {
            isDeviceEnabled = !isDeviceEnabled
            updateSwitchUI(isDeviceEnabled, powerSwitch, powerIcon, powerText)
            
            if (isDeviceEnabled) {
                // Lógica para conectar/re-escanear si fuera necesario
            } else {
                // Lógica para desconectar si se desea
            }
        }

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
                    bleStatusCard.visibility = View.GONE
                    cardEsp32.visibility = View.VISIBLE
                    // Al conectar, aseguramos que el switch esté en ON (o como prefieras)
                    isDeviceEnabled = true
                    updateSwitchUI(true, powerSwitch, powerIcon, powerText)
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

    private fun updateSwitchUI(enabled: Boolean, container: LinearLayout, icon: ImageView, text: TextView) {
        if (enabled) {
            // Estado ON (Basado en tu imagen: Icono a la derecha, fondo gris, icono con fondo oscuro)
            container.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            container.setBackgroundResource(R.drawable.rounded_corner) // Usar el drawable de fondo redondeado
            
            // Reordenar para que el icono esté a la derecha
            container.removeAllViews()
            
            val newText = TextView(context).apply {
                this.text = "ON"
                this.textAlignment = View.TEXT_ALIGNMENT_CENTER
                this.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
                this.textSize = 12f // Cambiado a float (SP por defecto en setTextSize)
                this.setTypeface(null, android.graphics.Typeface.BOLD)
                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
                params.setMargins(12, 0, 12, 0)
                this.layoutParams = params
            }
            
            container.addView(newText)
            container.addView(icon)
            
            icon.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            icon.alpha = 0.6f // Color gris oscuro/café como la imagen
            text.text = "ON"
        } else {
            // Estado OFF (Icono a la izquierda, fondo amarillo)
            container.removeAllViews()
            container.addView(icon)
            container.addView(text)
            
            icon.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_orange_light)
            icon.alpha = 1.0f
            text.text = "OFF"
            container.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
        }
    }
}