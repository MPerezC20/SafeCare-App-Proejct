package com.safecare.plus

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.safecare.plus.camera.MjpegStreamHandler

class CamerasFragment : Fragment() {

    private var streamHandler: MjpegStreamHandler? = null
    private lateinit var emptyState: LinearLayout
    private lateinit var statusMessage: TextView
    private lateinit var cameraCard: CardView
    private lateinit var streamView: ImageView
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var streamContainer: FrameLayout
    private var isFullscreen = false

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFullscreen) toggleFullscreen() else navigateToHome()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cameras, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        emptyState = view.findViewById(R.id.empty_camera_state)
        statusMessage = view.findViewById(R.id.empty_state_text) // Asegúrate de que este ID exista en tu XML
        cameraCard = view.findViewById(R.id.card_camera_detail)
        streamView = view.findViewById(R.id.camera_stream_view)
        fullscreenContainer = view.findViewById(R.id.fullscreen_container)
        streamContainer = view.findViewById(R.id.stream_container)
        
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            if (isFullscreen) toggleFullscreen() else navigateToHome()
        }

        view.findViewById<ImageButton>(R.id.btn_fullscreen).setOnClickListener {
            toggleFullscreen()
        }

        // --- LÓGICA DE CONTROL SEGÚN SWITCH DEL HOME ---
        if (HomeFragment.isCameraSwitchOn) {
            setupStream()
        } else {
            showCameraOffState()
        }
    }

    private fun setupStream() {
        streamHandler = MjpegStreamHandler(streamView) { isConnected ->
            if (isConnected) {
                emptyState.visibility = View.GONE
                cameraCard.visibility = View.VISIBLE
            } else {
                emptyState.visibility = View.VISIBLE
                cameraCard.visibility = View.GONE
                statusMessage.text = "Cámara no disponible en la red"
            }
        }
        streamHandler?.streamUrl = "http://10.236.177.237:81/stream"
        streamHandler?.startCamera()
    }

    private fun showCameraOffState() {
        emptyState.visibility = View.VISIBLE
        cameraCard.visibility = View.GONE
        statusMessage.text = "La cámara está apagada desde el Home"
        streamView.setImageResource(R.drawable.no_video) // Usar el recurso que ya tienes
    }

    private fun navigateToHome() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        val activity = requireActivity()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val header = view?.findViewById<CardView>(R.id.cameras_header_card)
        val scroll = view?.findViewById<View>(R.id.cameras_scroll)

        if (isFullscreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            bottomNav.visibility = View.GONE
            header?.visibility = View.GONE
            scroll?.visibility = View.GONE
            
            (streamView.parent as? ViewGroup)?.removeView(streamView)
            fullscreenContainer.addView(streamView)
            fullscreenContainer.visibility = View.VISIBLE
            streamView.scaleType = ImageView.ScaleType.FIT_CENTER
            
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN 
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            bottomNav.visibility = View.VISIBLE
            header?.visibility = View.VISIBLE
            scroll?.visibility = View.VISIBLE
            
            fullscreenContainer.visibility = View.GONE
            fullscreenContainer.removeView(streamView)
            streamContainer.addView(streamView, 0)
            streamView.scaleType = ImageView.ScaleType.FIT_CENTER
            
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        streamHandler?.stopCamera()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
