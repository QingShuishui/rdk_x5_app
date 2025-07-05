package top.minepixel.rdk.di

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMqttClient(): Mqtt5AsyncClient {
        return MqttClient.builder()
            .useMqttVersion5()
            .identifier("rdk-client-" + System.currentTimeMillis())
            .buildAsync()
    }
} 