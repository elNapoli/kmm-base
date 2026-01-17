package com.baldomeronapoli.kmm.base.domain.providers

import com.baldomeronapoli.kmm.base.domain.models.UserAgent

/**
 * Multiplatform provider for UserAgent information.
 * Implementations are platform-specific (Android/iOS).
 */
interface UserAgentProvider {
    /**
     * Provides platform-specific UserAgent information including:
     * - OS and OS version
     * - Device model and manufacturer
     * - Device type (smartphone/tablet/emulator)
     * - App ID and version
     */
    fun provide(): UserAgent
}