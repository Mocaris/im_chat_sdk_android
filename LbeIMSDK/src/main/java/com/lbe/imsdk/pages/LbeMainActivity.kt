package com.lbe.imsdk.pages

import android.app.*
import android.content.*
import android.os.*
import android.os.Build.VERSION.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.compose.runtime.*
import androidx.core.view.*
import com.lbe.imsdk.pages.navigation.LbeNavBackStackPage
import com.lbe.imsdk.pages.vm.LbeMainViewModel
import com.lbe.imsdk.provider.LocalMainViewModel
import com.lbe.imsdk.provider.LocalSDKInitConfig
import com.lbe.imsdk.repository.model.SDKInitConfig
import com.lbe.imsdk.theme.LbeIMTheme
import java.util.*

/**
 * lbe chat main  activity
 * use [LbeMainActivity.start]
 *
 * @Date 2025-08-15
 */
class LbeMainActivity : ComponentActivity() {

    companion object {

        /**
         * portable start lbe chat activity
         */
        fun start(context: Context, args: SDKInitConfig) {
            val intent = Intent(context, LbeMainActivity::class.java).also {
                if (context !is Activity) {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            intent.putExtra("sdkConfig", args)
            context.startActivity(intent)
        }
    }

    val sdkInitConfig: SDKInitConfig by lazy {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("sdkConfig", SDKInitConfig::class.java)
        } else {
            intent.getSerializableExtra("sdkConfig") as SDKInitConfig
        } ?: throw Exception("sdkConfig is null")
    }

    val viewModel by viewModels<LbeMainViewModel>()
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(checkUpdateLanguage(newBase, sdkInitConfig.supportLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.enableEdgeToEdge(window)
        super.onCreate(savedInstanceState)

        viewModel.initSdk(sdkInitConfig)
        setContent {
            LbeIMTheme {
                CompositionLocalProvider(
                    LocalMainViewModel provides viewModel,
                    LocalSDKInitConfig provides sdkInitConfig
                ) {
                    LbeNavBackStackPage()
                }
            }
        }
    }

    /**
     * update chat language
     */
    private fun checkUpdateLanguage(context: Context, lang: String): Context? {
        val locale = if (lang.startsWith("zh") || lang == "0") {
            Locale.CHINESE
        } else {
            Locale.ENGLISH
        }
        val config = context.resources.configuration.also {
            it.setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }


}