package com.example.util

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PedalManager(context: Context) {
    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _deviceName = MutableStateFlow("")
    val deviceName: StateFlow<String> = _deviceName.asStateFlow()

    private val inputDeviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            checkDevices()
        }

        override fun onInputDeviceRemoved(deviceId: Int) {
            checkDevices()
        }

        override fun onInputDeviceChanged(deviceId: Int) {
            checkDevices()
        }
    }

    init {
        inputManager.registerInputDeviceListener(inputDeviceListener, null)
        checkDevices()
    }

    private fun checkDevices() {
        var foundPedal = false
        var pName = ""
        for (deviceId in inputManager.inputDeviceIds) {
            val device = inputManager.getInputDevice(deviceId)
            if (device != null) {
                val name = device.name?.lowercase() ?: ""
                if (name.contains("m-vave") || name.contains("cube turner") || name.contains("airturn")) {
                    foundPedal = true
                    pName = device.name ?: "Pedal M-VAVE"
                    break
                }
            }
        }
        _isConnected.value = foundPedal
        _deviceName.value = pName
    }
}
