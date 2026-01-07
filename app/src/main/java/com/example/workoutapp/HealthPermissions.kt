package com.example.workoutapp

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord

object HealthPermissions {

    val REQUIRED_PERMISSIONS: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )
}
