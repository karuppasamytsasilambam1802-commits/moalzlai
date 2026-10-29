package com.example.myapplication.sync

data class DeviceSession(
    val deviceId: String,
    val deviceName: String,
    val lastActive: Long = System.currentTimeMillis()
)

object MultiDeviceSyncManager {

    private val connectedDevices = mutableListOf(
        DeviceSession("dev_01", "Android Phone (Primary)"),
        DeviceSession("dev_02", "Android Tablet / Web Portal")
    )

    fun getActiveDevices(): List<DeviceSession> = connectedDevices.toList()

    fun syncDeviceSession(deviceId: String, name: String) {
        val existing = connectedDevices.find { it.deviceId == deviceId }
        if (existing == null) {
            connectedDevices.add(DeviceSession(deviceId, name))
        }
    }
}
