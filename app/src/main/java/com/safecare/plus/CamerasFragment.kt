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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.safecare.plus.camera.MjpegStreamHandler

class CamerasFragment : Fragment() {

    private var streamHandler: MjpegStreamHandler? = null
    private lateinit var emptyState: LinearLayout
    private lateinit var cameraCard: CardView
    private lateinit var streamView: ImageView
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var streamContainer: FrameLayout
    private var isFullscreen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cameras, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyState = view.findViewById(R.id.empty_camera_state)
        cameraCard = view.findViewById(R.id.card_camera_detail)
        streamView = view.findViewById(R.id.camera_stream_view)
        fullscreenContainer = view.findViewById(R.id.fullscreen_container)
        streamContainer = view.findViewById(R.id.stream_container)
        
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val btnFullscreen: ImageButton = view.findViewById(R.id.btn_fullscreen)

        backButton.setOnClickListener {
            if (isFullscreen) {
                toggleFullscreen()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()

                val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav.selectedItemId = R.id.nav_home
            }
        }

        btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }

        // Inicializar el manejador
        streamHandler = MjpegStreamHandler(streamView) { isConnected ->
            if (isConnected) {
                emptyState.visibility = View.GONE
                cameraCard.visibility = View.VISIBLE
            } else {
                emptyState.visibility = View.VISIBLE
                cameraCard.visibility = View.GONE
            }
        }
        
        streamHandler?.streamUrl = "http://10.236.177.237:81/stream"
        streamHandler?.startCamera()
    }

    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        val activity = requireActivity()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val header = view?.findViewById<CardView>(R.id.cameras_header_card)
        val scroll = view?.findViewById<View>(R.id.cameras_scroll)

        if (isFullscreen) {
            // Modo Horizontal y Pantalla Completa
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            bottomNav.visibility = View.GONE
            header?.visibility = View.GONE
            scroll?.visibility = View.GONE
            
            // Mover el ImageView al contenedor de pantalla completa
            (streamView.parent as? ViewGroup)?.removeView(streamView)
            fullscreenContainer.addView(streamView)
            fullscreenContainer.visibility = View.VISIBLE
            
            // Ajustar imagen para que llene la pantalla
            streamView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            streamView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            streamView.scaleType = ImageView.ScaleType.FIT_CENTER
            
            // Ocultar barra de estado y navegación
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN 
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            // Volver a Vertical
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            bottomNav.visibility = View.VISIBLE
            header?.visibility = View.VISIBLE
            scroll?.visibility = View.VISIBLE
            
            fullscreenContainer.visibility = View.GONE
            fullscreenContainer.removeView(streamView)
            
            // Devolver el ImageView a su contenedor original
            streamContainer.addView(streamView, 0)
            
            streamView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            streamView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            streamView.scaleType = ImageView.ScaleType.FIT_CENTER
            
            // Mostrar barras de nuevo
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        streamHandler?.stopCamera()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onDestroyView() {
        super.onDestroyView()
        streamHandler?.stopCamera()
    }
}