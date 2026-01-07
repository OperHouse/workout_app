package com.example.workoutapp

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class DailyHealthData(
    val steps: Int
)

class HealthConnectReader(context: Context) {

    private val client = HealthConnectClient.getOrCreate(context)

    /**
     * Читаем шаги за сегодня
     */
    fun readToday(callback: (DailyHealthData) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val startOfDay = Instant.now()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
            val now = Instant.now()

            Log.d("HealthConnectReader", "Reading steps from $startOfDay to $now")

            val stepsResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )

            Log.d("HealthConnectReader", "Raw steps records count: ${stepsResponse.records.size}")
            stepsResponse.records.forEachIndexed { index, record ->
                Log.d(
                    "HealthConnectReader",
                    "Record #$index: count=${record.count}, startTime=${record.startTime}, endTime=${record.endTime}"
                )
            }

            val totalSteps = stepsResponse.records.sumOf { it.count.toInt() }
            Log.d("HealthConnectReader", "Total steps calculated: $totalSteps")

            withContext(Dispatchers.Main) {
                callback(DailyHealthData(totalSteps))
            }
        }
    }
}
