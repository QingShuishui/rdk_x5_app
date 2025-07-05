package top.minepixel.rdk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RdkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 应用程序初始化代码
    }
} 