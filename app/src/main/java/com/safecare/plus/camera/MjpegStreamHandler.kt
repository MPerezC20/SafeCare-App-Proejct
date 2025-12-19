package com.safecare.plus.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class MjpegStreamHandler(private val imageView: ImageView) {
    private var job: Job? = null
    private val streamUrl = "http://192.168.4.1:81/stream"

    fun startCamera() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val url = URL(streamUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    
                    val inputStream = BufferedInputStream(connection.inputStream)
                    val reader = MjpegInputStream(inputStream)

                    while (isActive) {
                        val bitmap = reader.readMjpegFrame()
                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                imageView.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(2000) // Retry delay
                }
            }
        }
    }

    fun stopCamera() {
        job?.cancel()
    }

    private class MjpegInputStream(private val inputStream: BufferedInputStream) {
        private val mContentLength = "Content-Length: "

        fun readMjpegFrame(): Bitmap? {
            try {
                var header = readLine()
                while (header != null && !header.startsWith(mContentLength)) {
                    header = readLine()
                }
                
                if (header == null) return null
                
                val contentLength = header.substring(mContentLength.length).trim().toInt()
                
                // Skip one more line (empty line after header)
                readLine()
                
                val frameData = ByteArray(contentLength)
                var bytesRead = 0
                while (bytesRead < contentLength) {
                    val n = inputStream.read(frameData, bytesRead, contentLength - bytesRead)
                    if (n == -1) break
                    bytesRead += n
                }
                
                return BitmapFactory.decodeByteArray(frameData, 0, frameData.size)
            } catch (e: Exception) {
                return null
            }
        }

        private fun readLine(): String? {
            val sb = StringBuilder()
            var b: Int
            while (true) {
                b = inputStream.read()
                if (b == -1 || b == '\n'.toInt()) break
                if (b != '\r'.toInt()) sb.append(b.toChar())
            }
            return if (b == -1 && sb.isEmpty()) null else sb.toString()
        }
    }
}