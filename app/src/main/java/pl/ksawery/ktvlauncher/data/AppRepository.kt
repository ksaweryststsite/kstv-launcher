package pl.ksawery.ktvlauncher.data

import pl.ksawery.ktvlauncher.model.LaunchableApp

interface AppRepository {
    suspend fun getLaunchableApps(): List<LaunchableApp>
}

