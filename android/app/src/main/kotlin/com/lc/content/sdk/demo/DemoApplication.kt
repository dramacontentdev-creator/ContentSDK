package com.lc.content.sdk.demo

import android.app.Application
import com.lc.content.sdk.AndroidContentSdk

/**
 * Content SDK - integration Step 4 (initialize)
 *
 * Call `initialize` exactly ONCE, as early as possible, from your `Application.onCreate()`.
 * It only stores your configuration - no network, ads, or player start here, so it is cheap.
 *
 * Remember to register your Application class in AndroidManifest.xml:
 *   <application android:name=".YourApplication" ...>
 */
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // (Optional) Print SDK status messages to Logcat. Keep it off in your release builds.
        AndroidContentSdk.setLoggingEnabled(BuildConfig.DEBUG)

        AndroidContentSdk.initialize(
            application = this,
            appId = SdkConfig.APP_ID,
            appSecret = SdkConfig.APP_SECRET,
            userId = SdkConfig.USER_ID,
            // (Optional) Content language, e.g. "en-US". Pass "" to follow your app's locale.
            language = "",
        )
    }
}

/**
 * Your SDK credentials.
 *
 * The values below are DEMO credentials so this project runs out of the box.
 * Replace them with YOUR App ID / App Secret from the SDK provider - they are bound to your
 * application id (package name), so the demo values will not work in your app.
 * Tip: do not hard-code your production secret in a public repository.
 */
object SdkConfig {
    /** YOUR Content App ID. */
    const val APP_ID = "1d724208-378c-489a-8462-f9038fa1ffd8"

    /** YOUR Content App Secret. */
    const val APP_SECRET = "427908ce2f21dc75bab6202bd7a3390f29e27bfe0b3e686b4570af7e4ef972fd"

    /**
     * YOUR logged-in user's id, so watch history and unlocks follow the user across devices.
     * Pass "" if your app has no login - the SDK then creates an anonymous user.
     */
    const val USER_ID = ""
}
