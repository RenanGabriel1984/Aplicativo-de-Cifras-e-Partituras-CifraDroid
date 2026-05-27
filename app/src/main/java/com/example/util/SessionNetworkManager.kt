package com.example.util

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import org.json.JSONObject

object SessionNetworkManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Server state
    private var serverSocket: ServerSocket? = null
    private val clients = mutableListOf<Socket>()
    private val printWriters = mutableListOf<PrintWriter>()
    
    // Client state
    private var clientSocket: Socket? = null
    
    private val _connectionStatus = MutableStateFlow("Desconectado")
    val connectionStatus = _connectionStatus.asStateFlow()
    
    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount = _connectedClientsCount.asStateFlow()
    
    private val _serverIpAndPort = MutableStateFlow<String?>(null)
    val serverIpAndPort = _serverIpAndPort.asStateFlow()

    fun getLocalIpAddress(): String {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
            ""
        } catch (ex: Exception) {
            ""
        }
    }

    fun startServer(port: Int = 8080) {
        stopAll()
        SessionManager.setRole(SessionRole.LEADER)
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                val ip = getLocalIpAddress()
                _serverIpAndPort.value = if(ip.isNotEmpty()) "$ip:$port" else "127.0.0.1:$port"
                _connectionStatus.value = "Sessão Aberta (Maestro)"
                
                while (isActive) {
                    val client = serverSocket?.accept() ?: break
                    synchronized(clients) {
                        clients.add(client)
                        printWriters.add(PrintWriter(client.getOutputStream(), true))
                        _connectedClientsCount.value = clients.size
                    }
                    
                    // Emite o estado atual imediatamente pro novo cliente
                    val currentEvent = SessionManager.syncEvents.value
                    if (currentEvent != null) {
                        val json = JSONObject().apply {
                            put("manuscriptId", currentEvent.manuscriptId)
                            put("pageIndex", currentEvent.pageIndex)
                            put("timestamp", currentEvent.timestamp)
                            if (currentEvent.note != null) put("note", currentEvent.note)
                        }
                        synchronized(clients) {
                            printWriters.lastOrNull()?.println(json.toString())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SessionManager", "Server error", e)
            } finally {
                if (_connectionStatus.value != "Conectando...") {
                    _connectionStatus.value = "Desconectado"
                }
                _serverIpAndPort.value = null
            }
        }
    }

    fun connectAsFollower(ipString: String) {
        stopAll()
        SessionManager.setRole(SessionRole.FOLLOWER)
        val parts = ipString.split(":")
        val ip = parts.getOrNull(0) ?: return
        val port = parts.getOrNull(1)?.toIntOrNull() ?: 8080
        
        scope.launch {
            var failCount = 0
            while (isActive && SessionManager.currentRole.value == SessionRole.FOLLOWER) {
                try {
                    _connectionStatus.value = "Conectando a $ip..."
                    clientSocket = Socket()
                    clientSocket!!.connect(java.net.InetSocketAddress(ip, port), 5000)
                    val reader = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
                    _connectionStatus.value = "Conectado ao Maestro"
                    failCount = 0
                    
                    while (isActive && SessionManager.currentRole.value == SessionRole.FOLLOWER) {
                        val line = reader.readLine() ?: break
                        try {
                            val json = JSONObject(line)
                            val mId = json.optInt("manuscriptId", -1)
                            val pIndex = json.optInt("pageIndex", 0)
                            val ts = json.optLong("timestamp", 0L)
                            val note = if (json.has("note")) json.optString("note") else null
                            if (mId != -1) {
                                // Add small debounce to avoid bounce issues
                                SessionManager.onReceivedSyncEvent(SyncEvent(mId, pIndex, ts, note))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SessionManager", "Client error", e)
                } finally {
                    try { clientSocket?.close() } catch(e: Exception) {}
                    clientSocket = null
                    
                    if (SessionManager.currentRole.value == SessionRole.FOLLOWER) {
                        _connectionStatus.value = "Reconectando..."
                        delay(2500) // Delay before reconnect attempt
                        failCount++
                        // Silent infinity reconnect basically
                    }
                }
            }
            if (_connectionStatus.value.contains("Conectado") || _connectionStatus.value.contains("Reconectando")) {
                 _connectionStatus.value = "Desconectado"
            }
        }
    }

    fun broadcast(event: SyncEvent) {
        scope.launch {
            val json = JSONObject().apply {
                put("manuscriptId", event.manuscriptId)
                put("pageIndex", event.pageIndex)
                put("timestamp", event.timestamp)
                if (event.note != null) put("note", event.note)
            }
            val str = json.toString()
            synchronized(clients) {
                val iterClients = clients.iterator()
                val iterWriters = printWriters.iterator()
                while (iterClients.hasNext() && iterWriters.hasNext()) {
                    val c = iterClients.next()
                    val w = iterWriters.next()
                    try {
                        w.println(str)
                        if (w.checkError()) throw Exception("Write error")
                    } catch (e: Exception) {
                        try { c.close() } catch(e2: Exception){}
                        iterClients.remove()
                        iterWriters.remove()
                    }
                }
                _connectedClientsCount.value = clients.size
            }
        }
    }
    
    fun stopAll() {
        try { serverSocket?.close() } catch(e: Exception){}
        serverSocket = null
        try { clientSocket?.close() } catch(e: Exception){}
        clientSocket = null
        synchronized(clients) {
            clients.forEach { try { it.close() } catch(e: Exception){} }
            clients.clear()
            printWriters.clear()
            _connectedClientsCount.value = 0
        }
        _serverIpAndPort.value = null
        _connectionStatus.value = "Desconectado"
        SessionManager.setRole(SessionRole.STANDALONE)
    }
}
