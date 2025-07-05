package top.minepixel.rdk.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import top.minepixel.rdk.data.manager.SessionManager
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
}