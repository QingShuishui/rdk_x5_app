package top.minepixel.rdk.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import top.minepixel.rdk.data.manager.DeviceManager
import top.minepixel.rdk.ui.screens.SmartDevice
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceManager: DeviceManager
) : ViewModel() {
    
    // 设备列表
    val devices: StateFlow<List<SmartDevice>> = deviceManager.devices
    
    // 设备统计
    val totalDevices: StateFlow<Int> = deviceManager.totalDevices
    val onlineDevices: StateFlow<Int> = deviceManager.onlineDevices
    val connectedDevices: StateFlow<Int> = deviceManager.connectedDevices
    
    /**
     * 添加设备
     */
    fun addDevice(device: SmartDevice) {
        deviceManager.addDevice(device)
    }
    
    /**
     * 移除设备
     */
    fun removeDevice(deviceId: String) {
        deviceManager.removeDevice(deviceId)
    }
    
    /**
     * 切换设备连接状态
     */
    fun toggleDeviceConnection(deviceId: String) {
        deviceManager.toggleDeviceConnection(deviceId)
    }
    
    /**
     * 切换设备在线状态
     */
    fun toggleDeviceOnline(deviceId: String) {
        deviceManager.toggleDeviceOnline(deviceId)
    }
    
    /**
     * 更新设备
     */
    fun updateDevice(deviceId: String, updater: (SmartDevice) -> SmartDevice) {
        deviceManager.updateDevice(deviceId, updater)
    }
    
    /**
     * 获取设备数量
     */
    fun getTotalDevicesCount(): Int = deviceManager.getTotalDevicesCount()
    
    /**
     * 获取在线设备数量
     */
    fun getOnlineDevicesCount(): Int = deviceManager.getOnlineDevicesCount()
    
    /**
     * 获取连接率
     */
    fun getConnectionRate(): Int = deviceManager.getConnectionRate()
}
