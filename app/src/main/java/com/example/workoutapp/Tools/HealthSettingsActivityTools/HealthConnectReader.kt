package com.example.workoutapp.Tools.HealthSettingsActivityTools

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class DailyHealthData(
    val steps: Int,
    val calories: Float,
    val distance: Float // Дистанция в метрах
)

class HealthConnectReader(context: Context) {

    private val client = HealthConnectClient.getOrCreate(context)

    fun readToday(callback: (DailyHealthData) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val startOfDay = ZonedDateTime.now(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
            val now = Instant.now()
            val filter = TimeRangeFilter.between(startOfDay, now)

            try {
                // 1. Читаем шаги, калории и дистанцию ОДНИМ запросом (оптимизация)
                val response = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(
                            StepsRecord.COUNT_TOTAL,
                            ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                            DistanceRecord.DISTANCE_TOTAL // Добавили дистанцию
                        ),
                        timeRangeFilter = filter
                    )
                )

                val totalSteps = response[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
                val activeCals = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toFloat() ?: 0f
                val totalCals = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toFloat() ?: 0f

                // Дистанция в метрах (конвертируем в float)
                val totalDistance = response[DistanceRecord.DISTANCE_TOTAL]?.inMeters?.toFloat() ?: 0f

                Log.d("HealthConnectReader", "Steps: $totalSteps, Distance: $totalDistance m")

                val finalCalories = if (activeCals > 0) activeCals else totalCals

                withContext(Dispatchers.Main) {
                    callback(DailyHealthData(totalSteps, finalCalories, totalDistance))
                }
            } catch (e: Exception) {
                Log.e("HealthConnectReader", "Ошибка при чтении: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(DailyHealthData(0, 0f, 0f))
                }
            }
        }
    }
}