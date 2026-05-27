package com.example.util

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import org.json.JSONObject

object SessionNetworkManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var serverJob: Job? = null
    private var clientJob: Job? = null
    private var discoveryJob: Job? = null
    
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
    
    private val _sessionPin = MutableStateFlow<String>("")
    val sessionPin = _sessionPin.asStateFlow()

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

    private fun startDiscoveryBroadcast(pin: String, ip: String, port: Int) {
        discoveryJob?.cancel()
        discoveryJob = scope.launch(Dispatchers.IO) {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true
                val message = "MAESTRO_DISCOVERY|$pin|$ip:$port"
                val buffer = message.toByteArray()
                val address = InetAddress.getByName("255.255.255.255")
                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size, address, 8081)
                    socket.send(packet)
                    delay(2000)
                }
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startServer(port: Int = 8080) {
        stopAll()
        SessionManager.setRole(SessionRole.LEADER)
        val pin = (1000..9999).random().toString()
        _sessionPin.value = pin
        
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                val ip = getLocalIpAddress()
                val addr = if(ip.isNotEmpty()) "$ip:$port" else "127.0.0.1:$port"
                _serverIpAndPort.value = addr
                _connectionStatus.value = "Sessão Aberta (PIN: $pin)"
                
                if (ip.isNotEmpty()) startDiscoveryBroadcast(pin, ip, port)
                
                while (isActive) {
                    val client = serverSocket?.accept() ?: break
                    synchronized(clients) {
                        clients.add(client)
                        printWriters.add(PrintWriter(client.getOutputStream(), true))
                        _connectedClientsCount.value = clients.size
                    }
                    
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

    fun connectViaPin(pin: String) {
        stopAll()
        SessionManager.setRole(SessionRole.FOLLOWER)
        clientJob = scope.launch(Dispatchers.IO) {
            _connectionStatus.value = "Buscando Maestro (PIN $pin)..."
            var targetIp: String? = null
            
            try {
                val socket = DatagramSocket(8081)
                socket.soTimeout = 10000 // 10 seconds timeout to find
                val buffer = ByteArray(256)
                while (isActive && targetIp == null) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val received = String(packet.data, 0, packet.length)
                    if (received.startsWith("MAESTRO_DISCOVERY|$pin|")) {
                        targetIp = received.split("|")[2]
                    }
                }
                socket.close()
            } catch (e: Exception) {
                _connectionStatus.value = "Sessão não encontrada."
                SessionManager.setRole(SessionRole.STANDALONE)
                return@launch
            }
            
            if (targetIp != null) {
                connectToDirectIp(targetIp)
            }
        }
    }

    fun connectToDirectIp(ipString: String) {
        // ... (called internally by connectViaPin)
        val parts = ipString.split(":")
        val ip = parts.getOrNull(0) ?: return
        val port = parts.getOrNull(1)?.toIntOrNull() ?: 8080
        
        scope.launch(Dispatchers.IO) {
            var failCount = 0
            while (isActive && SessionManager.currentRole.value == SessionRole.FOLLOWER) {
                try {
                    _connectionStatus.value = "Conectando a Maestro..."
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
                        delay(2500)
                        failCount++
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
        serverJob?.cancel()
        clientJob?.cancel()
        discoveryJob?.cancel()
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
        _sessionPin.value = ""
        _connectionStatus.value = "Desconectado"
        SessionManager.setRole(SessionRole.STANDALONE)
    }
}

