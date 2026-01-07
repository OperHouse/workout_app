package com.example.workoutapp

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
            // Вычисляем начало текущего дня в системном часовом поясе
            val startOfDay = ZonedDateTime.now(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
            val now = Instant.now()

            try {
                // Используем агрегацию для получения суммы шагов и калорий одним запросом
                val response = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(
                            StepsRecord.COUNT_TOTAL,
                            ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                        ),
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                    )
                )

                // Извлекаем данные (если данных нет, вернется null, поэтому используем ?: 0)
                val totalSteps = response[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
                val activityTotalCalories = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toFloat() ?: 0f

                Log.d("HealthConnectReader", "Final data - Steps: $totalSteps, Cals: $activityTotalCalories")

                withContext(Dispatchers.Main) {
                    callback(DailyHealthData(totalSteps, activityTotalCalories))
                }
            } catch (e: Exception) {
                Log.e("HealthConnectReader", "Ошибка при чтении данных: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(DailyHealthData(0, 0f))
                }
            }
        }
    }
}