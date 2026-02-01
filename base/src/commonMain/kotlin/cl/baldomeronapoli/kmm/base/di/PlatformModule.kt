package cl.baldomeronapoli.kmm.base.di

import org.koin.core.module.Module

/**
 * Módulo de dependencias específico de cada plataforma
 * Cada plataforma provee su propia implementación
 */
expect fun platformModule(): Module
