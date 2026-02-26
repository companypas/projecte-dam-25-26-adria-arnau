package com.example.pi_androidapp.core.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.pi_androidapp.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt que provee la base de datos SQLDelight y sus queries.
 * La base de datos se usa como caché local para evitar peticiones de red innecesarias.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Provee el driver SQLite de Android para SQLDelight. */
    @Provides
    @Singleton
    fun provideAndroidSqliteDriver(
        @ApplicationContext context: Context
    ): AndroidSqliteDriver {
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = "app_cache.db"
        )
    }

    /** Provee la instancia de la base de datos SQLDelight. */
    @Provides
    @Singleton
    fun provideAppDatabase(driver: AndroidSqliteDriver): AppDatabase {
        return AppDatabase(driver)
    }
}
