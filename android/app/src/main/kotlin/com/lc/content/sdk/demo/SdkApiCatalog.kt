package com.lc.content.sdk.demo

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.lc.content.sdk.AndroidContentSdk
import com.lc.content.sdk.api.Drama
import com.lc.content.sdk.api.SdkException
import com.lc.content.sdk.api.event.AdEvent
import com.lc.content.sdk.api.event.PlayerEvent
import com.lc.content.sdk.api.event.PlayerPlaybackState
import com.lc.content.sdk.api.strategy.ContentSdkStrategy
import com.lc.content.sdk.api.theme.Colors
import com.lc.content.sdk.api.theme.SubtitleStyle
import com.lc.content.sdk.ui.DramaHomeFragment

/*
 * =================================================================================================
 *  Content SDK - API catalog (Android)
 * =================================================================================================
 *
 *  Every public call of `AndroidContentSdk`, what it is for, and where to find an example.
 *  The main flow (Steps 1-6) is already wired in this project; everything marked "optional" is
 *  shown in this file only. Pick what you need and copy it into your app.
 *  The functions here compile but are never called by the demo.
 *
 *  Rules that apply to every call:
 *  - Callbacks and listeners are always delivered on the MAIN thread.
 *  - `initialize` once in Application.onCreate; `start` must succeed before any SDK page,
 *    `login` or `getDramaList`.
 *  - Errors are `SdkException` with `code` and `msg`.
 *
 *  | API                                     | Required | When to call                         | Section |
 *  |-----------------------------------------|----------|--------------------------------------|---------|
 *  | initialize(application, appId, ...)     | yes      | Application.onCreate, once           | Step 4  |
 *  | showAdConsentIfRequired(activity)       | optional | before start; skip if you run UMP    | Step 5  |
 *  | start()                                 | yes      | after consent, once per process      | Step 5  |
 *  | openDramaHomePage(context)              | yes *    | after start succeeded                | Step 6  |
 *  | setLoggingEnabled(enabled)              | optional | any time, usually before initialize  | 1       |
 *  | setStrategy(strategy)                   | optional | before start                         | 2       |
 *  | setColorPalette / resetColorPalette     | optional | any time; also applies to open pages | 3       |
 *  | setSubtitleStyle / resetSubtitleStyle   | optional | any time; overrides subtitle files   | 3       |
 *  | start / consent (suspend versions)      | optional | coroutine versions of Step 5         | 4       |
 *  | openDramaHomePage(context, title)       | optional | home with your own title             | 5.1     |
 *  | openDramaPlayerPage(context, params)    | optional | open one drama directly              | 5.2     |
 *  | createDramaHomeFragment(title, insets)  | optional | home inside your screen (tab, ...)   | 5.3     |
 *  | createDramaPlayerFragment(params)       | optional | player inside your screen            | 5.4     |
 *  | DramaHomeScreen(...) @Composable        | optional | home inside your Compose UI          | 5.5     |
 *  | DramaPlayerScreen(...) @Composable      | optional | player inside your Compose UI        | 5.6     |
 *  | login(userId)                           | optional | user logs in / switches / logs out   | 6.1     |
 *  | setLanguage(language)                   | optional | your app language changed            | 6.2     |
 *  | setPlayerEventListener(listener)        | optional | playback events                      | 7.1     |
 *  | setAdEventListener(listener)            | optional | ad events                            | 7.2     |
 *  | getDramaList(page, pageSize, ...)       | optional | build your own drama entry UI        | 8       |
 *
 *  * Any one of Step 6 or 5.1 - 5.6 is enough to show SDK content.
 * =================================================================================================
 */
@Suppress("unused")
object SdkApiCatalog {

    private const val TAG = "ContentSdkDemo"

    // ---------------------------------------------------------------------------------------------
    // 1. Logging
    // ---------------------------------------------------------------------------------------------

    /**
     * Prints SDK status lines to Logcat (tag "ContentSdk"), e.g. `[Lifecycle] start_succeeded`.
     * Secrets and URLs are never printed. Default: off.
     *
     * Usually `setLoggingEnabled(BuildConfig.DEBUG)` before `initialize` (see DemoApplication.kt).
     * Attach these lines when you report an issue.
     */
    fun enableLogs(enabled: Boolean) {
        AndroidContentSdk.setLoggingEnabled(enabled)
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Resource strategy
    // ---------------------------------------------------------------------------------------------

    /**
     * Chooses how much memory, disk and network the SDK may use. Call BEFORE `start`.
     *
     * - Adaptive (default, recommended): adjusts to the device automatically. No call needed.
     * - Standard: fixed, balanced for mainstream devices.
     * - LowPerformance: fixed, lowest memory / disk / data usage.
     * - HighPerformance: fixed, fastest episode switching on high-end devices.
     */
    fun chooseStrategy() {
        AndroidContentSdk.setStrategy(ContentSdkStrategy.Adaptive)
    }

    // ---------------------------------------------------------------------------------------------
    // 3. Theme colors
    // ---------------------------------------------------------------------------------------------

    /**
     * Switches SDK pages to the built-in dark palette; also applies to SDK pages already open.
     * Built-in palettes: `Colors.light` (default) and `Colors.dark`.
     */
    fun useDarkTheme() {
        AndroidContentSdk.setColorPalette(Colors.dark)
    }

    /** Back to the default light palette. */
    fun useDefaultTheme() {
        AndroidContentSdk.resetColorPalette()
    }

    /**
     * Subtitle appearance for all SDK players. Each field you set wins over the subtitle file;
     * unset fields keep the file's styling or the SDK default. Applies to subtitles already on screen.
     * Fields: fontScale (0.5..2.0), textColor, backgroundColor, outlineColor, bold, bottomMargin (0..0.4 of the player height).
     */
    fun useLargeSubtitles() {
        AndroidContentSdk.setSubtitleStyle(SubtitleStyle(fontScale = 1.2f, textColor = Colors.argb(0xFFFFFF00)))
        // Back to file styles and SDK defaults: AndroidContentSdk.resetSubtitleStyle()
    }

    // ---------------------------------------------------------------------------------------------
    // 4. Step 5 with coroutines
    // ---------------------------------------------------------------------------------------------

    /**
     * Same as `MainActivity.startSdk`, written with suspend functions.
     * Call from your own lifecycle scope, e.g. `lifecycleScope.launch { SdkApiCatalog.startWithCoroutines(this@MainActivity) }`.
     *
     * @return true when SDK pages can be opened.
     */
    suspend fun startWithCoroutines(activity: Activity): Boolean {
        val consent = AndroidContentSdk.showAdConsentIfRequired(activity)
        if (!consent.canRequestAds) {
            Log.w(TAG, "ads not allowed: ${consent.errorMessage}")
            return false
        }
        return try {
            AndroidContentSdk.start()
            true
        } catch (error: SdkException) {
            Log.w(TAG, "start failed: ${error.code} ${error.msg}")
            false
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 5. Other ways to show SDK pages (pick one; all need `start` to have succeeded)
    // ---------------------------------------------------------------------------------------------

    /** 5.1 Full-screen home with a title bar. `title = null` (the default) shows no title bar. */
    fun openHomeWithTitle(context: Context) {
        AndroidContentSdk.openDramaHomePage(context, title = "Dramas")
    }

    /**
     * 5.2 Opens the player full screen for one drama, e.g. from your own list (section 8),
     * a push notification or a deep link.
     *
     * `Drama.PlayerParams`:
     * - id: drama id.
     * - episodeOrder: episode to start from, 1-based. 0 (default) = first episode; above the count = last episode.
     * - drama: (optional) a Drama you already have, so title and cover show without waiting.
     */
    fun openPlayer(context: Context, dramaId: String) {
        AndroidContentSdk.openDramaPlayerPage(context, Drama.PlayerParams(id = dramaId, episodeOrder = 1))
        // With a Drama from getDramaList: AndroidContentSdk.openDramaPlayerPage(context, drama.asPlayerParams())
    }

    /**
     * 5.3 Home as a Fragment inside your own screen. Tapping a drama opens the SDK player page.
     *
     * @param containerId id of a container view in [activity] (usually a FrameLayout in your layout).
     */
    fun showHomeFragment(activity: FragmentActivity, containerId: Int) {
        // Add only once; after a rotation the FragmentManager restores it for you.
        if (activity.supportFragmentManager.findFragmentById(containerId) != null) return
        val fragment = AndroidContentSdk.createDramaHomeFragment(
            // Title bar text; null (the default) shows no title bar.
            title = null,
            // SystemBarsPadding (default) | StatusBarsPadding | NavigationBarsPadding | None.
            insetsMode = DramaHomeFragment.InsetsMode.SystemBarsPadding,
        )
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }

    /** 5.4 Player as a Fragment inside your own screen. */
    fun showPlayerFragment(activity: FragmentActivity, containerId: Int, dramaId: String) {
        if (activity.supportFragmentManager.findFragmentById(containerId) != null) return
        val fragment = AndroidContentSdk.createDramaPlayerFragment(Drama.PlayerParams(id = dramaId, episodeOrder = 0))
        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }

    /**
     * 5.5 Home as a Composable inside your Compose UI.
     *
     * - title: defaults to the SDK title; null shows no title bar.
     * - onDramaClick: (optional) handle taps yourself. Left out, the SDK opens its own player page.
     */
    @Composable
    fun HomeInCompose() {
        AndroidContentSdk.DramaHomeScreen(modifier = Modifier.fillMaxSize())
    }

    /** 5.6 Player as a Composable. `onCloseRequest`: the player asked to close; pop your navigation there. */
    @Composable
    fun PlayerInCompose(dramaId: String, onClose: () -> Unit) {
        AndroidContentSdk.DramaPlayerScreen(
            params = Drama.PlayerParams(id = dramaId, episodeOrder = 0),
            onCloseRequest = onClose,
        )
    }

    // ---------------------------------------------------------------------------------------------
    // 6. User and language
    // ---------------------------------------------------------------------------------------------

    /**
     * 6.1 Tells the SDK which of YOUR users is active, so watch history and unlocks follow them.
     * Call on login, account switch or logout. Not needed at launch if `initialize` got the userId.
     *
     * @param userId any stable id from your account system; "" = anonymous (use on logout).
     * Callback: `error == null` = new identity active; otherwise the previous identity stays.
     * Coroutine version: `AndroidContentSdk.login(userId)` (throws SdkException).
     */
    fun onUserChanged(userId: String) {
        AndroidContentSdk.login(userId) { error ->
            if (error != null) Log.w(TAG, "login failed: ${error.code} ${error.msg}")
        }
    }

    /**
     * 6.2 Your app language changed; later content requests use it.
     *
     * @param language BCP-47 code such as "en-US" or "zh-CN". Unsupported codes fall back to "en-US".
     * Callback: `effectiveLanguage` is the language the SDK really uses.
     */
    fun onAppLanguageChanged(language: String) {
        AndroidContentSdk.setLanguage(language) { effectiveLanguage, error ->
            Log.d(TAG, "SDK language = $effectiveLanguage, error = ${error?.code}")
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 7. Event listeners (one per process; setting again replaces; pass null to stop)
    // ---------------------------------------------------------------------------------------------

    /**
     * 7.1 Playback events. Every event has `resource` (null before an episode is bound):
     * dramaId, dramaName, episodeId, episodeNumber (1-based), episodeName, episodeCount.
     * Most also have `progress`: positionMs, durationMs, bufferedPositionMs.
     */
    fun listenToPlayerEvents() {
        AndroidContentSdk.setPlayerEventListener { event ->
            val episode = event.resource?.let { "${it.dramaName} #${it.episodeNumber}" } ?: "-"
            when (event) {
                // First frame on screen; startupDurationMs = prepare to first frame.
                is PlayerEvent.FirstFrameRendered -> Log.d(TAG, "[$episode] first frame in ${event.startupDurationMs} ms")
                // Playing, paused, buffering, ended, ...
                is PlayerEvent.PlaybackStateChanged ->
                    if (event.state == PlayerPlaybackState.Playing) Log.d(TAG, "[$episode] playing")
                // About every 300 ms while playing (best-effort, not a precise timer).
                is PlayerEvent.PlaybackProgressUpdated -> Unit
                is PlayerEvent.PlaybackEnded -> Log.d(TAG, "[$episode] finished")
                // recoverable = the SDK may retry by itself.
                is PlayerEvent.PlaybackError -> Log.w(TAG, "[$episode] error ${event.error.code} recoverable=${event.recoverable}")
                // Also: PositionDiscontinuity (seek), QualityChanged, QualityResolved,
                // PlaybackSpeedChanged, ControlDispatched.
                else -> Unit
            }
        }
    }

    /** 7.2 Ad events. Every event has `ad`: format (Rewarded / Interstitial), scene, adUnitId. */
    fun listenToAdEvents() {
        AndroidContentSdk.setAdEventListener { event ->
            when (event) {
                is AdEvent.Impression -> Log.d(TAG, "ad shown: ${event.ad.format}")
                is AdEvent.RewardEarned -> Log.d(TAG, "reward: ${event.rewardAmount} ${event.rewardType}")
                // Impression value, separate from Impression and not guaranteed for every impression.
                // valueMicros / 1_000_000 = value in currencyCode; final earnings come from the ad platform.
                is AdEvent.Paid -> Log.d(TAG, "paid: ${event.valueMicros} micros ${event.currencyCode} (${event.precision})")
                is AdEvent.Dismissed -> Log.d(TAG, "ad closed")
                // stage = Loading or Presentation.
                is AdEvent.Error -> Log.w(TAG, "ad error at ${event.stage}: ${event.error.code}")
                // Also: LoadStarted, Loaded, Clicked.
                else -> Unit
            }
        }
    }

    /** Stops both listeners, e.g. on logout. */
    fun stopListening() {
        AndroidContentSdk.setPlayerEventListener(null)
        AndroidContentSdk.setAdEventListener(null)
    }

    // ---------------------------------------------------------------------------------------------
    // 8. Drama list (your own entry UI, then open the player with 5.2)
    // ---------------------------------------------------------------------------------------------

    /**
     * Loads one page of dramas. Call from your lifecycle scope.
     *
     * - page: 1-based (>= 1). pageSize: >= 1.
     * - dramaIds: (optional) only these ids; null or empty = no filter.
     * - recentDays: (optional) only dramas published in the last N days (>= 0); null = no filter.
     *
     * Each Drama: id, name, coverImageUrl, episodeSize, freeCount, description, tags.
     * Empty list = no more data. Invalid arguments throw code `INVALID_ARGUMENT`.
     */
    suspend fun loadDramas(): List<Drama> = try {
        AndroidContentSdk.getDramaList(page = 1, pageSize = 20)
        // Filtered: AndroidContentSdk.getDramaList(page = 1, pageSize = 20, dramaIds = listOf("<ID>"), recentDays = 7)
    } catch (error: SdkException) {
        Log.w(TAG, "getDramaList failed: ${error.code} ${error.msg}")
        emptyList()
    }
}
