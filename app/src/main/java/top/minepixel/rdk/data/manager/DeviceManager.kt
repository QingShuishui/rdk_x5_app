package top.minepixel.rdk.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.minepixel.rdk.ui.screens.DeviceType
import top.minepixel.rdk.ui.screens.SmartDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManager @Inject constructor() {
    
    // 设备列表状态
    private val _devices = MutableStateFlow(getInitialDevices())
    val devices: StateFlow<List<SmartDevice>> = _devices.asStateFlow()
    
    // 设备统计
    val totalDevices: StateFlow<Int> = MutableStateFlow(1)
    val onlineDevices: StateFlow<Int> = MutableStateFlow(1)
    val connectedDevices: StateFlow<Int> = MutableStateFlow(1)
    
    init {
        // 初始化设备统计
        updateDeviceStats()
    }
    
    /**
     * 获取初始设备列表
     */
    private fun getInitialDevices(): List<SmartDevice> {
        return listOf(
            SmartDevice(
                id = "robot_001",
                name = "光净精灵",
                type = DeviceType.VACUUM_ROBOT,
                isOnline = true,
                isConnected = true,
                batteryLevel = 85,
                lastActivity = "2分钟前"
            )
        )
    }
    
    /**
     * 添加设备
     */
    fun addDevice(device: SmartDevice) {
        val currentDevices = _devices.value.toMutableList()
        currentDevices.add(device)
        _devices.value = currentDevices
        updateDeviceStats()
    }
    
    /**
     * 移除设备
     */
    fun removeDevice(deviceId: String) {
        val currentDevices = _devices.value.toMutableList()
        currentDevices.removeAll { it.id == deviceId }
        _devices.value = currentDevices
        updateDeviceStats()
    }
    
    /**
     * 更新设备状态
     */
    fun updateDevice(deviceId: String, updater: (SmartDevice) -> SmartDevice) {
        val currentDevices = _devices.value.toMutableList()
        val index = currentDevices.indexOfFirst { it.id == deviceId }
        if (index != -1) {
            currentDevices[index] = updater(currentDevices[index])
            _devices.value = currentDevices
            updateDeviceStats()
        }
    }
    
    /**
     * 切换设备连接状态
     */
    fun toggleDeviceConnection(deviceId: String) {
        updateDevice(deviceId) { device ->
            device.copy(isConnected = !device.isConnected)
        }
    }
    
    /**
     * 切换设备在线状态
     */
    fun toggleDeviceOnline(deviceId: String) {
        updateDevice(deviceId) { device ->
            device.copy(isOnline = !device.isOnline)
        }
    }
    
    /**
     * 更新设备统计信息
     */
    private fun updateDeviceStats() {
        val currentDevices = _devices.value
        (totalDevices as MutableStateFlow).value = currentDevices.size
        (onlineDevices as MutableStateFlow).value = currentDevices.count { it.isOnline }
        (connectedDevices as MutableStateFlow).value = currentDevices.count { it.isConnected }
    }
    
    /**
     * 获取设备数量
     */
    fun getTotalDevicesCount(): Int = _devices.value.size
    
    /**
     * 获取在线设备数量
     */
    fun getOnlineDevicesCount(): Int = _devices.value.count { it.isOnline }
    
    /**
     * 获取已连接设备数量
     */
    fun getConnectedDevicesCount(): Int = _devices.value.count { it.isConnected }
    
    /**
     * 获取连接率
     */
    fun getConnectionRate(): Int {
        val total = getTotalDevicesCount()
        val connected = getConnectedDevicesCount()
        return if (total > 0) (connected.toFloat() / total * 100).toInt() else 0
    }
}
