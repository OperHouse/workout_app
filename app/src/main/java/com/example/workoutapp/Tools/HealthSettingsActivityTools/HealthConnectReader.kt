package com.example.workoutapp.Tools.HealthSettingsActivityTools

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
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
    val calories: Float
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
                // 1. Читаем шаги
                val stepsResponse = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = filter
                    )
                )
                val totalSteps = stepsResponse[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0

                // 2. Читаем АКТИВНЫЕ калории (энергия от упражнений и движения)
                val activeCalsResponse = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                        timeRangeFilter = filter
                    )
                )
                val activeCals = activeCalsResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toFloat() ?: 0f

                // 3. Читаем ОБЩИЕ калории (активные + базовый метаболизм BMR)
                val totalCalsResponse = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                        timeRangeFilter = filter
                    )
                )
                val totalCals = totalCalsResponse[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toFloat() ?: 0f

                Log.d("HealthConnectReader", "Steps: $totalSteps")
                Log.d("HealthConnectReader", "Active Calories: $activeCals")
                Log.d("HealthConnectReader", "Total Calories (inc. Metabolism): $totalCals")

                // Логика выбора:
                // Если активные калории есть (> 0), обычно лучше использовать их для "эквивалента еды".
                // Если активных 0, но общие есть, можно использовать общие (но помните, что там сидит метаболизм).
                val finalCalories = if (activeCals > 0) activeCals else totalCals

                withContext(Dispatchers.Main) {
                    callback(DailyHealthData(totalSteps, finalCalories))
                }
            } catch (e: Exception) {
                Log.e("HealthConnectReader", "Ошибка при чтении: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(DailyHealthData(0, 0f))
                }
            }
        }
    }
}