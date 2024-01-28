package com.mikael.mkutilslegacy.api.lib

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlin.concurrent.thread


object MineWebsocket {
    open class Client(url: String) : WebSocketClient(URI(url)) {

        override fun onOpen(handshakedata: ServerHandshake?) {}

        override fun onMessage(message: String?) {}

        override fun onClose(code: Int, reason: String?, remote: Boolean) {}

        override fun onError(ex: Exception?) {
            ex?.printStackTrace()
        }

        override fun reconnect() {
            thread {
                super.reconnect()
            }
        }

        fun waitForConnection() {
            while (!isOpen) {
                Thread.sleep(100)
            }
        }
    }
}