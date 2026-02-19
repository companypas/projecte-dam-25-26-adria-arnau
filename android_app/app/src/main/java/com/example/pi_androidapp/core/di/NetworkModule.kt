package com.example.pi_androidapp.core.di

import com.example.pi_androidapp.core.network.ApiConstants
import com.example.pi_androidapp.core.network.AuthInterceptor
import com.example.pi_androidapp.data.remote.api.AuthApiService
import com.example.pi_androidapp.data.remote.api.CategoriasApiService
import com.example.pi_androidapp.data.remote.api.ComprasApiService
import com.example.pi_androidapp.data.remote.api.ConversacionesApiService
import com.example.pi_androidapp.data.remote.api.ProductosApiService
import com.example.pi_androidapp.data.remote.api.UsuariosApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Módulo de Hilt que provee las dependencias de red. Configura OkHttp, Retrofit y los servicios de
 * API.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Provee el interceptor de logging para debug. */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS }
    }

    /** Provee el cliente OkHttp con interceptores. */
    @Provides
    @Singleton
    fun provideOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build()
    }

    /** Provee la instancia de Retrofit. */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    /** Provee el servicio de autenticación. */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    /** Provee el servicio de productos. */
    @Provides
    @Singleton
    fun provideProductosApiService(retrofit: Retrofit): ProductosApiService {
        return retrofit.create(ProductosApiService::class.java)
    }

    /** Provee el servicio de compras. */
    @Provides
    @Singleton
    fun provideComprasApiService(retrofit: Retrofit): ComprasApiService {
        return retrofit.create(ComprasApiService::class.java)
    }

    /** Provee el servicio de categorías. */
    @Provides
    @Singleton
    fun provideCategoriasApiService(retrofit: Retrofit): CategoriasApiService {
        return retrofit.create(CategoriasApiService::class.java)
    }

    /** Provee el servicio de usuarios. */
    @Provides
    @Singleton
    fun provideUsuariosApiService(retrofit: Retrofit): UsuariosApiService {
        return retrofit.create(UsuariosApiService::class.java)
    }

    /** Provee el servicio de conversaciones. */
    @Provides
    @Singleton
    fun provideConversacionesApiService(retrofit: Retrofit): ConversacionesApiService {
        return retrofit.create(ConversacionesApiService::class.java)
    }
}
