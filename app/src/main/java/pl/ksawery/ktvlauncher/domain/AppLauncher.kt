package pl.ksawery.ktvlauncher.domain

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.widget.Toast
import pl.ksawery.ktvlauncher.R
import pl.ksawery.ktvlauncher.data.LauncherPreferences
import pl.ksawery.ktvlauncher.model.LaunchAnimationMode
import pl.ksawery.ktvlauncher.model.LaunchableApp

/**
 * App launch is performed immediately on OK release; NO Compose / coroutine delay.
 * Cross-app window animations use Android ActivityOptions and may be overridden
 * by Android TV firmware or by the target streaming app's own startup screen.
 */
class AppLauncher(
    private val activity: Activity,
    private val preferences: LauncherPreferences,
) {
    fun launch(app: LaunchableApp): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = app.componentName
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        return launchIntent(intent)
    }

    fun launchIntent(intent: Intent): Boolean {
        val mode = preferences.launchAnimationMode()
        val result = runCatching {
            val options: ActivityOptions? = when (mode) {
                LaunchAnimationMode.System -> null
                LaunchAnimationMode.GoogleTV -> ActivityOptions.makeCustomAnimation(
                    activity, R.anim.gtv_launch_enter, R.anim.gtv_launch_exit,
                )
                LaunchAnimationMode.AppleTV -> ActivityOptions.makeCustomAnimation(
                    activity, R.anim.appletv_launch_enter, R.anim.appletv_launch_exit,
                )
                LaunchAnimationMode.Fade -> ActivityOptions.makeCustomAnimation(
                    activity, R.anim.fade_launch_enter, R.anim.fade_launch_exit,
                )
                LaunchAnimationMode.Off -> ActivityOptions.makeCustomAnimation(
                    activity, 0, 0,
                )
            }
            if (options == null) activity.startActivity(intent)
            else activity.startActivity(intent, options.toBundle())
        }
        if (result.isFailure) {
            Toast.makeText(activity, R.string.launch_error, Toast.LENGTH_SHORT).show()
        }
        return result.isSuccess
    }
}
