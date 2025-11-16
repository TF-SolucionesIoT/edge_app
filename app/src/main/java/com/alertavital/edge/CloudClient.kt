package com.alertavital.edge

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class CloudClient(private val url: String) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("CloudClient", "✅ Conectado al backend")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("CloudClient", "Mensaje recibido: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("CloudClient", "Mensaje bytes recibido")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("CloudClient", "Error WebSocket", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("CloudClient", "WebSocket cerrado: $reason")
            }
        })
    }

    fun sendData(data: String) {
        webSocket?.send(data)
    }

    fun disconnect() {
        webSocket?.close(1000, "Cerrando conexión")
    }
}