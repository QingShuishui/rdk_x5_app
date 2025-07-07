package top.minepixel.rdk.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import top.minepixel.rdk.data.api.BaiduSpeechApiService
import top.minepixel.rdk.data.api.BaiduTokenApiService
import top.minepixel.rdk.data.api.ByteDanceTtsApiService
import top.minepixel.rdk.data.api.CozeApiService
import top.minepixel.rdk.data.manager.DeviceManager
import top.minepixel.rdk.data.manager.SessionManager
import top.minepixel.rdk.data.repository.CozeRepository
import top.minepixel.rdk.data.repository.CozeRepositoryImpl
import java.util.concurrent.TimeUnit
import top.minepixel.rdk.data.repository.AuthRepository
import top.minepixel.rdk.data.repository.AuthRepositoryImpl
import top.minepixel.rdk.data.repository.RobotRepository
import top.minepixel.rdk.data.repository.RobotRepositoryImpl
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRobotRepository(
        @ApplicationContext context: Context,
        @Named("IoDispatcher") ioDispatcher: CoroutineDispatcher
    ): RobotRepository {
        return RobotRepositoryImpl(context, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        sessionManager: SessionManager,
        @Named("IoDispatcher") ioDispatcher: CoroutineDispatcher
    ): AuthRepository {
        return AuthRepositoryImpl(context, sessionManager, ioDispatcher)
    }

    @Provides
    @Named("IoDispatcher")
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Named("DefaultDispatcher")
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CozeApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCozeApiService(retrofit: Retrofit): CozeApiService {
        return retrofit.create(CozeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCozeRepository(
        cozeApiService: CozeApiService,
        @Named("IoDispatcher") ioDispatcher: CoroutineDispatcher
    ): CozeRepository {
        return CozeRepositoryImpl(cozeApiService, ioDispatcher)
    }

    @Provides
    @Singleton
    @Named("BaiduTokenRetrofit")
    fun provideBaiduTokenRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BaiduSpeechApiService.TOKEN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("BaiduSpeechRetrofit")
    fun provideBaiduSpeechRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BaiduSpeechApiService.SPEECH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBaiduTokenApiService(@Named("BaiduTokenRetrofit") retrofit: Retrofit): BaiduTokenApiService {
        return retrofit.create(BaiduTokenApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBaiduSpeechApiService(@Named("BaiduSpeechRetrofit") retrofit: Retrofit): BaiduSpeechApiService {
        return retrofit.create(BaiduSpeechApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("ByteDanceTtsRetrofit")
    fun provideByteDanceTtsRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ByteDanceTtsApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideByteDanceTtsApiService(@Named("ByteDanceTtsRetrofit") retrofit: Retrofit): ByteDanceTtsApiService {
        return retrofit.create(ByteDanceTtsApiService::class.java)
    }
}