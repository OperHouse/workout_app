package com.example.workoutapp.Fragments.ProfileFragments.HealthSettingsActivity

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object HealthConnectHelper {
    @JvmStatic
    fun checkGrantedPermissions(
        context: Context,
        permissionsToCheck: Set<String>,
        callback: (Set<String>) -> Unit
    ) {
        val client = HealthConnectClient.getOrCreate(context)

        CoroutineScope(Dispatchers.IO).launch {
            // теперь метод вызывается без аргументов
            val allGranted = client.permissionController.getGrantedPermissions()

            // оставляем только те, что нам нужны
            val granted = allGranted.intersect(permissionsToCheck)

            withContext(Dispatchers.Main) {
                callback(granted)
            }
        }
    }
}
