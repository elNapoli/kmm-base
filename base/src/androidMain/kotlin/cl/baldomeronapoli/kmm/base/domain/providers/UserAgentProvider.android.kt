package cl.baldomeronapoli.kmm.base.domain.providers

import android.content.Context
import android.os.Build
import cl.baldomeronapoli.kmm.base.domain.models.UserAgent

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
private const val ANDROID = "android"
private const val HUAWEI = "huawei"
private const val APP_ALIAS = "AN"
private const val SDK = "sdk"
private const val TABLET_DP = 600
private const val TABLET = "tablet"
private const val EMULATOR = "emulator"
private const val SMART_PHONE = "smartphone"

class UserAgentProviderImpl(private val context: Context) :
    cl.baldomeronapoli.kmm.base.domain.providers.UserAgentProvider {

    private val osVersion = Build.VERSION.RELEASE ?: UNDEFINED
    private val model = Build.MODEL ?: UNDEFINED
    private val brand = Build.MANUFACTURER ?: UNDEFINED

    override fun provide(): UserAgent {
        val appId = context.packageName
        val os = if (!brand.contains(HUAWEI, ignoreCase = true)) ANDROID else HUAWEI
        val deviceType = obtainDeviceType(context)
        val packageInfo =
            context.packageManager.getPackageInfo(context.packageName, 0)
        val appVersion = packageInfo.versionName!!

        return UserAgent.Builder()
            .addValue(OS, os)
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

    private fun obtainDeviceType(context: Context): String {
        val isEmulator = model.contains(SDK, ignoreCase = true)
        val screenSize = context.resources.configuration.screenWidthDp
        val isTablet = screenSize >= TABLET_DP

        return when {
            isEmulator -> EMULATOR
            isTablet -> TABLET
            else -> SMART_PHONE
        }
    }
}
