package com.lc.content.sdk.demo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lc.content.sdk.AndroidContentSdk

/**
 * Demo entry screen.
 *
 * Everything related to the SDK lives in two functions below:
 *   - [startSdk]          -> Step 5: ad consent + start the SDK
 *   - [openDramaHome]     -> Step 6: show the SDK page
 * The rest of this file is just demo UI.
 */
class MainActivity : ComponentActivity() {

    /** Simple UI state so the demo can show what the SDK is doing. */
    private var sdkStatus by mutableStateOf<SdkStatus>(SdkStatus.NotStarted)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoHomeScreen(
                status = sdkStatus,
                onOpenDramaHome = ::onOpenDramaHomeClick,
            )
        }
    }

    private fun onOpenDramaHomeClick() {
        when (sdkStatus) {
            // Already started: open the page directly. `start` only needs to succeed once per process.
            SdkStatus.Ready -> openDramaHome()
            // A start request is running; ignore repeated taps.
            SdkStatus.Starting -> Unit
            // Not started yet (or the last attempt failed): start first, then open the page.
            else -> startSdk(onReady = ::openDramaHome)
        }
    }

    /**
     * Content SDK - integration Step 5 (consent + start)
     *
     * 1. `showAdConsentIfRequired` shows Google's privacy (UMP / GDPR) dialog when the user's region
     *    needs it. Otherwise it returns immediately. Skip it if your app already runs UMP.
     * 2. `start` boots the SDK (network, player, ads). Call it once; do NOT show SDK pages before it
     *    succeeds.
     *
     * Both callbacks run on the main thread, so it is safe to update UI or open pages inside them.
     * Tip: you can also call this early (e.g. on your splash screen) so the SDK is ready when needed.
     */
    private fun startSdk(onReady: () -> Unit) {
        sdkStatus = SdkStatus.Starting

        AndroidContentSdk.showAdConsentIfRequired(this) { consent ->
            if (!consent.canRequestAds) {
                // The user did not allow ad requests. Decide what your app should do here.
                sdkStatus = SdkStatus.Failed("Consent", consent.errorMessage ?: "Ads not allowed")
                return@showAdConsentIfRequired
            }

            AndroidContentSdk.start { error ->
                if (error == null) {
                    sdkStatus = SdkStatus.Ready
                    onReady()
                } else {
                    // Show a failure message or retry entry.
                    Log.w(TAG, "Content SDK start failed: ${error.code} ${error.msg}")
                    sdkStatus = SdkStatus.Failed(error.code, error.msg)
                    Toast.makeText(this, "SDK start failed: ${error.code}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Content SDK - integration Step 6 (show the SDK page)
     *
     * One line opens the SDK's own full-screen drama home. The SDK handles everything after that:
     * browsing, the player, episode switching, ad unlock.
     *
     * Want the page inside your own screen (a Fragment tab, a Composable) or to open the player
     * directly? See section 5 of SdkApiCatalog.kt.
     */
    private fun openDramaHome() {
        AndroidContentSdk.openDramaHomePage(this)
    }

    private companion object {
        const val TAG = "ContentSdkDemo"
    }
}

/** What the demo home screen shows about the SDK. Not part of the SDK API. */
sealed interface SdkStatus {
    data object NotStarted : SdkStatus
    data object Starting : SdkStatus
    data object Ready : SdkStatus
    data class Failed(val code: String, val message: String) : SdkStatus
}
