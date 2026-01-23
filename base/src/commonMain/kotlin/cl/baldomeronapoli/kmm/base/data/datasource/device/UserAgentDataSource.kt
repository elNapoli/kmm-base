package cl.baldomeronapoli.kmm.base.data.datasource.device

import cl.baldomeronapoli.kmm.base.domain.models.UserAgent

/**
 * Multiplatform data source for UserAgent information.
 * Implementations are platform-specific (Android/iOS).
 */
interface UserAgentDataSource {
    /**
     * Provides platform-specific UserAgent information including:
     * - OS and OS version
     * - Device model and manufacturer
     * - Device type (smartphone/tablet/emulator)
     * - App ID and version
     */
    fun provide(): UserAgent
}
