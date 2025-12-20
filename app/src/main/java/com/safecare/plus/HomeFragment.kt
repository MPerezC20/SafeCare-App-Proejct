package com.safecare.plus

import android.graphics.Color
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.safecare.plus.ble.BLEService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    companion object {
        var isCameraSwitchOn = false
        var isSafeButtonSwitchOn = false
    }

    private lateinit var auth: FirebaseAuth
    private var cameraCheckJob: Job? = null
    private var isSafeButtonConnected = false
    private var isCameraConnected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        view.findViewById<TextView>(R.id.welcome_message).text = "¡Bienvenido de vuelta!"
        
        view.findViewById<ImageButton>(R.id.logout_button).setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }

        view.findViewById<Button>(R.id.button_view_cameras).setOnClickListener {
            navigateToCameras()
        }

        setupSafeButtonUI(view)
        setupCameraUI(view)
    }

    private fun setupSafeButtonUI(view: View) {
        val connectionStatusText: TextView = view.findViewById(R.id.connection_status)
        val powerSwitch: LinearLayout = view.findViewById(R.id.power_switch)
        val powerIcon: ImageView = view.findViewById(R.id.power_icon_internal)
        val powerText: TextView = view.findViewById(R.id.power_text_internal)

        // Estado inicial de la UI basado en el switch
        updateSwitchUI(isSafeButtonSwitchOn, powerSwitch, powerIcon, powerText)
        if (!isSafeButtonSwitchOn) {
            connectionStatusText.text = "Desconectado"
            connectionStatusText.setTextColor(Color.parseColor("#9E9E9E"))
        }

        powerSwitch.setOnClickListener {
            if (!isSafeButtonSwitchOn) {
                connectionStatusText.text = "Buscando..."
                connectionStatusText.setTextColor(Color.parseColor("#9E9E9E"))
                
                isSafeButtonSwitchOn = true
                updateSwitchUI(true, powerSwitch, powerIcon, powerText)
                BLEService.connectDevice()

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(5000) 
                    if (!isSafeButtonConnected && isSafeButtonSwitchOn) {
                        isSafeButtonSwitchOn = false
                        updateSwitchUI(false, powerSwitch, powerIcon, powerText)
                        connectionStatusText.text = "Desconectado"
                    }
                }
            } else {
                isSafeButtonSwitchOn = false
                updateSwitchUI(false, powerSwitch, powerIcon, powerText)
                BLEService.disconnectDevice()
                connectionStatusText.text = "Desconectado"
                connectionStatusText.setTextColor(Color.parseColor("#9E9E9E"))
            }
        }

        BLEService.bleStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                BLEService.BLEState.CONNECTED -> {
                    isSafeButtonConnected = true
                    connectionStatusText.text = "Conectado"
                    connectionStatusText.setTextColor(Color.parseColor("#4CAF50"))
                    if (!isSafeButtonSwitchOn) {
                        isSafeButtonSwitchOn = true
                        updateSwitchUI(true, powerSwitch, powerIcon, powerText)
                    }
                }
                BLEService.BLEState.DISCONNECTED -> {
                    isSafeButtonConnected = false
                    connectionStatusText.text = "Desconectado"
                    connectionStatusText.setTextColor(Color.parseColor("#9E9E9E"))
                    if (isSafeButtonSwitchOn) {
                        isSafeButtonSwitchOn = false
                        updateSwitchUI(false, powerSwitch, powerIcon, powerText)
                    }
                }
                BLEService.BLEState.SCANNING -> {
                    if (isSafeButtonSwitchOn) {
                        connectionStatusText.text = "Buscando..."
                        connectionStatusText.setTextColor(Color.parseColor("#9E9E9E"))
                    } else {
                        connectionStatusText.text = "Desconectado"
                    }
                }
                else -> {
                    if (!isSafeButtonSwitchOn) {
                        connectionStatusText.text = "Desconectado"
                    }
                }
            }
        }
    }

    private fun setupCameraUI(view: View) {
        val camPowerSwitch: LinearLayout = view.findViewById(R.id.cam_power_switch)
        val camPowerIcon: ImageView = view.findViewById(R.id.cam_power_icon)
        val camPowerText: TextView = view.findViewById(R.id.cam_power_text)
        val camStatusText: TextView = view.findViewById(R.id.cam_connection_status)

        updateSwitchUI(isCameraSwitchOn, camPowerSwitch, camPowerIcon, camPowerText)
        if (!isCameraSwitchOn) {
            camStatusText.text = "Desconectado"
            camStatusText.setTextColor(Color.parseColor("#9E9E9E"))
        }

        camPowerSwitch.setOnClickListener {
            if (!isCameraSwitchOn) {
                camStatusText.text = "Buscando..."
                camStatusText.setTextColor(Color.parseColor("#9E9E9E"))
                
                isCameraSwitchOn = true
                updateSwitchUI(true, camPowerSwitch, camPowerIcon, camPowerText)

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(3000)
                    if (!isCameraConnected && isCameraSwitchOn) {
                        isCameraSwitchOn = false
                        updateSwitchUI(false, camPowerSwitch, camPowerIcon, camPowerText)
                        camStatusText.text = "Desconectado"
                    }
                }
            } else {
                isCameraSwitchOn = false
                updateSwitchUI(false, camPowerSwitch, camPowerIcon, camPowerText)
                camStatusText.text = "Desconectado"
                camStatusText.setTextColor(Color.parseColor("#9E9E9E"))
            }
        }

        startCameraMonitoring(camStatusText, camPowerSwitch, camPowerIcon, camPowerText)
    }

    private fun startCameraMonitoring(statusText: TextView, container: LinearLayout, icon: ImageView, text: TextView) {
        cameraCheckJob?.cancel()
        cameraCheckJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val cameraUrl = "http://10.236.177.237:81/stream"
            while (true) {
                val isAvailable = try {
                    val url = URL(cameraUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 1500
                    connection.readTimeout = 1500
                    val responseCode = connection.responseCode
                    connection.disconnect()
                    responseCode == 200 || responseCode == 401
                } catch (e: Exception) { false }

                withContext(Dispatchers.Main) {
                    isCameraConnected = isAvailable
                    if (isAvailable) {
                        statusText.text = "Conectado"
                        statusText.setTextColor(Color.parseColor("#4CAF50"))
                        if (!isCameraSwitchOn) {
                            isCameraSwitchOn = true
                            updateSwitchUI(true, container, icon, text)
                        }
                    } else {
                        statusText.text = "Desconectado"
                        statusText.setTextColor(Color.parseColor("#9E9E9E"))
                        if (isCameraSwitchOn) {
                            isCameraSwitchOn = false
                            updateSwitchUI(false, container, icon, text)
                        }
                    }
                }
                delay(3000)
            }
        }
    }

    private fun navigateToCameras() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CamerasFragment())
            .addToBackStack(null)
            .commit()

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_cameras
    }

    private fun updateSwitchUI(enabled: Boolean, container: LinearLayout, icon: ImageView, text: TextView) {
        container.removeAllViews()
        if (enabled) {
            container.addView(icon)
            container.addView(text)
            icon.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_orange_light)
            icon.alpha = 1.0f
            text.text = "ON"
            text.setTextColor(Color.parseColor("#9E9E9E"))
        } else {
            val offText = TextView(context).apply {
                this.text = "OFF"
                this.textAlignment = View.TEXT_ALIGNMENT_CENTER
                this.setTextColor(Color.parseColor("#9E9E9E"))
                this.textSize = if (container.id == R.id.cam_power_switch) 10f else 14f
                this.setTypeface(null, android.graphics.Typeface.BOLD)
                this.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f).apply {
                    setMargins(8, 0, 8, 0)
                }
            }
            container.addView(offText)
            container.addView(icon)
            icon.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
            icon.alpha = 0.6f
            text.text = "OFF"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraCheckJob?.cancel()
    }
}
