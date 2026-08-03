package pl.ksawery.ktvlauncher.domain

import android.content.Context
import android.content.Intent
import android.widget.Toast
import pl.ksawery.ktvlauncher.R
import pl.ksawery.ktvlauncher.model.LaunchableApp

class AppLauncher(private val context: Context) {
    fun launch(app: LaunchableApp) {
        val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
            component = app.componentName
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }

        val result = runCatching { context.startActivity(explicitIntent) }
        if (result.isFailure) {
            Toast.makeText(context, R.string.launch_error, Toast.LENGTH_SHORT).show()
        }
    }
}

