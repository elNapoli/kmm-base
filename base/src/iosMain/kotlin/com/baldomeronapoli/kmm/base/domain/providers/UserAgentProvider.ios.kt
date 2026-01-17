package com.baldomeronapoli.kmm.base.domain.providers

import com.baldomeronapoli.kmm.base.domain.models.UserAgent
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad

private const val OS = "os"
private const val OS_VERSION = "os_version"
private const val DEVICE_MODEL = "device_model"
private const val DEVICE_MAKER = "device_maker"
private const val DEVICE_TYPE = "device_type"
private const val APP_ID = "app_id"
private const val APP_VERSION = "app_version"
private const val SDK_V = "sdkv"
private const val ALIAS = "app_alias"
private const val UNDEFINED = "undefined"
private const val IOS = "ios"
private const val APPLE = "Apple"
private const val APP_ALIAS = "AN"
private const val TABLET = "tablet"
private const val EMULATOR = "emulator"
private const val SMART_PHONE = "smartphone"

class UserAgentProviderImpl : UserAgentProvider {

    private val osVersion: String
        get() = UIDevice.currentDevice.systemVersion

    private val model: String
        get() = UIDevice.currentDevice.model

    private val brand: String = APPLE

    override fun provide(): UserAgent {
        val bundle = NSBundle.mainBundle
        val appId = bundle.bundleIdentifier ?: UNDEFINED
        val appVersion =
            bundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: UNDEFINED
        val deviceType = obtainDeviceType()

        return UserAgent.Builder()
            .addValue(OS, IOS)
            .addValue(OS_VERSION, osVersion)
            .addValue(DEVICE_MODEL, model)
            .addValue(DEVICE_MAKER, brand)
            .addValue(DEVICE_TYPE, deviceType)
            .addValue(APP_ID, appId)
            .addValue(APP_VERSION, appVersion)
            .addValue(SDK_V, appVersion)
            .addValue(ALIAS, APP_ALIAS)
            .build()
    }

    private fun obtainDeviceType(): String {
        val device = UIDevice.currentDevice

        // Check if running on simulator
        val isSimulator = isRunningOnSimulator()

        // Check device type (iPhone, iPad, etc.)
        val isTablet = device.userInterfaceIdiom == UIUserInterfaceIdiomPad

        return when {
            isSimulator -> EMULATOR
            isTablet -> TABLET
            else -> SMART_PHONE
        }
    }

    private fun isRunningOnSimulator(): Boolean {
        // In iOS, we can check if it's a simulator by checking the architecture
        // or by using platform-specific checks
        return platform.Foundation.NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    }
}
