package com.safecare.plus.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MjpegStreamHandler(
    private val imageView: ImageView,
    private val onStatusChange: ((Boolean) -> Unit)? = null
) {
    private var job: Job? = null
    var streamUrl = "http://10.236.177.237:81/stream" 

    fun startCamera() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            var connected = false
            while (isActive) {
                var connection: HttpURLConnection? = null
                try {
                    Log.d("MjpegStream", "Connecting to $streamUrl")
                    val url = URL(streamUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000 
                    connection.readTimeout = 10000    
                    
                    val inputStream = BufferedInputStream(connection.inputStream)
                    
                    if (!connected) {
                        connected = true
                        withContext(Dispatchers.Main) { 
                            onStatusChange?.invoke(true) 
                        }
                    }

                    while (isActive) {
                        val bitmap = decodeNextFrame(inputStream)
                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                imageView.setImageBitmap(bitmap)
                            }
                        } else {
                            Log.d("MjpegStream", "Failed to decode frame, reconnecting...")
                            break 
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MjpegStream", "Connection error: ${e.message}")
                    if (connected) {
                        connected = false
                        withContext(Dispatchers.Main) { 
                            onStatusChange?.invoke(false) 
                        }
                    }
                } finally {
                    connection?.disconnect()
                }
                
                if (isActive) delay(3000) 
            }
        }
    }

    fun stopCamera() {
        job?.cancel()
        job = null
    }

    private fun decodeNextFrame(inputStream: InputStream): Bitmap? {
        try {
            // 1. Buscar inicio del JPEG (SOI: FF D8)
            while (true) {
                var b = inputStream.read()
                if (b == -1) return null
                if (b == 0xFF) {
                    b = inputStream.read()
                    if (b == 0xD8) break
                }
            }

            // 2. Almacenar bytes hasta el fin del JPEG (EOI: FF D9)
            val buffer = ByteArrayOutputStream()
            buffer.write(0xFF)
            buffer.write(0xD8)

            var lastByte = 0
            while (true) {
                val b = inputStream.read()
                if (b == -1) return null
                buffer.write(b)
                if (lastByte == 0xFF && b == 0xD9) break
                lastByte = b
                
                if (buffer.size() > 1024 * 1024) return null // Límite de 1MB por frame
            }

            val data = buffer.toByteArray()
            return BitmapFactory.decodeByteArray(data, 0, data.size)
        } catch (e: Exception) {
            Log.e("MjpegStream", "Decode error: ${e.message}")
            return null
        }
    }
}