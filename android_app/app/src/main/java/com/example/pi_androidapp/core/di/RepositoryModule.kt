package com.example.pi_androidapp.core.di

import com.example.pi_androidapp.data.repository.AuthRepositoryImpl
import com.example.pi_androidapp.data.repository.CategoriasRepositoryImpl
import com.example.pi_androidapp.data.repository.ComprasRepositoryImpl
import com.example.pi_androidapp.data.repository.ProductosRepositoryImpl
import com.example.pi_androidapp.domain.repository.AuthRepository
import com.example.pi_androidapp.domain.repository.CategoriasRepository
import com.example.pi_androidapp.domain.repository.ComprasRepository
import com.example.pi_androidapp.domain.repository.ProductosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Módulo de Hilt que vincula las interfaces de repositorios con sus implementaciones concretas. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** Vincula AuthRepository con su implementación. */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    /** Vincula ProductosRepository con su implementación. */
    @Binds
    @Singleton
    abstract fun bindProductosRepository(
            productosRepositoryImpl: ProductosRepositoryImpl
    ): ProductosRepository

    /** Vincula ComprasRepository con su implementación. */
    @Binds
    @Singleton
    abstract fun bindComprasRepository(
            comprasRepositoryImpl: ComprasRepositoryImpl
    ): ComprasRepository

    /** Vincula CategoriasRepository con su implementación. */
    @Binds
    @Singleton
    abstract fun bindCategoriasRepository(
            categoriasRepositoryImpl: CategoriasRepositoryImpl
    ): CategoriasRepository
}
