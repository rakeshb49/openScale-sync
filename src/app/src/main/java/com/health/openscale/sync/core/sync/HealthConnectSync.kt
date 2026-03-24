/*
 *  Copyright (C) 2025  olie.xdev <olie.xdev@googlemail.com>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package com.health.openscale.sync.core.sync

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import androidx.health.connect.client.units.Power
import com.health.openscale.sync.core.datatypes.OpenScaleMeasurement
import com.health.openscale.sync.core.service.SyncResult
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import kotlin.reflect.KClass

class HealthConnectSync(private var healthConnectClient: HealthConnectClient) : SyncInterface(){
    suspend fun fullSync(measurements: List<OpenScaleMeasurement>) : SyncResult<Unit> {
        val records = mutableListOf<Record>()

        measurements.forEach { measurement ->
            val weightRecord = buildWeightRecord(measurement)
            records.add(weightRecord)

            val waterRecord = buildWaterRecord(measurement)
            records.add(waterRecord)

            val fatRecord = buildFatRecord(measurement)
            records.add(fatRecord)

            if (measurement.muscle > 0f) {
                records.add(buildMuscleRecord(measurement))
            }

            if (measurement.bone > 0f) {
                records.add(buildBoneRecord(measurement))
            }

            if (measurement.bmr > 0f) {
                records.add(buildBmrRecord(measurement))
            }
        }

        try {
            healthConnectClient.insertRecords(records)
            return SyncResult.Success(Unit)
        } catch (e: Exception) {
            return SyncResult.Failure(SyncResult.ErrorType.UNKNOWN_ERROR,null ,e)
        }
    }

    suspend fun insert(measurement: OpenScaleMeasurement) : SyncResult<Unit> {
        val records = mutableListOf<Record>()

        val weightRecord = buildWeightRecord(measurement)
        records.add(weightRecord)

        val waterRecord = buildWaterRecord(measurement)
        records.add(waterRecord)

        val fatRecord = buildFatRecord(measurement)
        records.add(fatRecord)

        if (measurement.muscle > 0f) {
            records.add(buildMuscleRecord(measurement))
        }

        if (measurement.bone > 0f) {
            records.add(buildBoneRecord(measurement))
        }

        if (measurement.bmr > 0f) {
            records.add(buildBmrRecord(measurement))
        }

        try {
            healthConnectClient.insertRecords(records)
            return SyncResult.Success(Unit)
        } catch (e: Exception) {
            return SyncResult.Failure(SyncResult.ErrorType.UNKNOWN_ERROR,null ,e)
        }
    }

    suspend fun delete(date: Date) : SyncResult<Unit> {
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val timeRange = TimeRangeFilter.between(startOfDay, endOfDay)

        try {
            healthConnectClient.deleteRecords(
                    WeightRecord::class,
                    timeRange
                )
            healthConnectClient.deleteRecords(
                    BodyFatRecord::class,
                    timeRange
                )
            healthConnectClient.deleteRecords(
                    BodyWaterMassRecord::class,
                    timeRange
                )
            healthConnectClient.deleteRecords(
                    LeanBodyMassRecord::class,
                    timeRange
                )
            healthConnectClient.deleteRecords(
                    BoneMassRecord::class,
                    timeRange
                )
            healthConnectClient.deleteRecords(
                    BasalMetabolicRateRecord::class,
                    timeRange
                )

            return SyncResult.Success(Unit)
        } catch (e: Exception) {
            return SyncResult.Failure(SyncResult.ErrorType.UNKNOWN_ERROR,null ,e)
        }
    }

    suspend fun clear() : SyncResult<Unit> {
        val localDate = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfDay = localDate.minusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = localDate.plusYears(10).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val timeRange = TimeRangeFilter.between(startOfDay, endOfDay)

        try {
            healthConnectClient.deleteRecords(
                WeightRecord::class,
                timeRange
            )
            healthConnectClient.deleteRecords(
                BodyFatRecord::class,
                timeRange
            )
            healthConnectClient.deleteRecords(
                BodyWaterMassRecord::class,
                timeRange
            )
            healthConnectClient.deleteRecords(
                LeanBodyMassRecord::class,
                timeRange
            )
            healthConnectClient.deleteRecords(
                BoneMassRecord::class,
                timeRange
            )
            healthConnectClient.deleteRecords(
                BasalMetabolicRateRecord::class,
                timeRange
            )

            return SyncResult.Success(Unit)
        } catch (e: Exception) {
            return SyncResult.Failure(SyncResult.ErrorType.UNKNOWN_ERROR,null ,e)
        }
    }

    suspend fun update(measurement: OpenScaleMeasurement) : SyncResult<Unit> {
        val records = mutableListOf<Record>()

        try {
            val weightRecord = readWeightRecord(measurement)
            val waterRecord = readWaterRecord(measurement)
            val fatRecord = readFatRecord(measurement)
            val muscleRecord = readMuscleRecord(measurement)
            val boneRecord = readBoneRecord(measurement)
            val bmrRecord = readBmrRecord(measurement)

            if (weightRecord != null) {
                records.add(buildWeightRecord(measurement))
            }
            if (waterRecord != null) {
                records.add(buildWaterRecord(measurement))
            }
            if (fatRecord != null) {
                records.add(buildFatRecord(measurement))
            }
            if (muscleRecord != null && measurement.muscle > 0f) {
                records.add(buildMuscleRecord(measurement))
            }
            if (boneRecord != null && measurement.bone > 0f) {
                records.add(buildBoneRecord(measurement))
            }
            if (bmrRecord != null && measurement.bmr > 0f) {
                records.add(buildBmrRecord(measurement))
            }

            if (records.isNotEmpty()) {
                healthConnectClient.insertRecords(records)
                return SyncResult.Success(Unit)
            } else {
                return SyncResult.Failure(SyncResult.ErrorType.API_ERROR,"No records found to update for measurement: ${measurement.id}")
            }

        } catch (e: Exception) {
            return SyncResult.Failure(SyncResult.ErrorType.UNKNOWN_ERROR,null ,e)
        }
    }

    private suspend fun readWeightRecord(measurement: OpenScaleMeasurement): WeightRecord? {
        return readRecord(measurement, WeightRecord::class)
    }

    private suspend fun readWaterRecord(measurement: OpenScaleMeasurement): BodyWaterMassRecord? {
        return readRecord(measurement, BodyWaterMassRecord::class)
    }

    private suspend fun readFatRecord(measurement: OpenScaleMeasurement): BodyFatRecord? {
        return readRecord(measurement,BodyFatRecord::class)
    }

    private suspend fun readMuscleRecord(measurement: OpenScaleMeasurement): LeanBodyMassRecord? {
        return readRecord(measurement, LeanBodyMassRecord::class)
    }

    private suspend fun readBoneRecord(measurement: OpenScaleMeasurement): BoneMassRecord? {
        return readRecord(measurement, BoneMassRecord::class)
    }

    private suspend fun readBmrRecord(measurement: OpenScaleMeasurement): BasalMetabolicRateRecord? {
        return readRecord(measurement, BasalMetabolicRateRecord::class)
    }

    private suspend fun <T : Record> readRecord(
        measurement: OpenScaleMeasurement,
        recordType: KClass<T>,
    ): T? {
        val timeRangeFilter = TimeRangeFilter.between(measurement.date.toInstant().minusSeconds(1), measurement.date.toInstant().plusSeconds(1))
        val dataOriginFilter = setOf(DataOrigin("com.health.openscale.sync"))
        val readRequest = ReadRecordsRequest(
            recordType = recordType,
            timeRangeFilter = timeRangeFilter,
            dataOriginFilter = dataOriginFilter
        )
        val response: ReadRecordsResponse<T> = healthConnectClient.readRecords(readRequest)
        return response.records.firstOrNull()
    }

    private fun buildMetadata(measurement: OpenScaleMeasurement, type: String): Metadata {
        return Metadata.manualEntry(
            clientRecordId = measurement.id.toString() + "_" + type,
            clientRecordVersion = Instant.now().toEpochMilli()
        )
    }

    private fun buildWeightRecord(measurement: OpenScaleMeasurement): WeightRecord {
        val measurementInstant = measurement.date.toInstant()
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(measurementInstant)

        return WeightRecord(
            time = measurementInstant,
            zoneOffset = zoneOffset,
            weight = Mass.kilograms(measurement.weight.toDouble()),
            metadata = buildMetadata(measurement, "weight")
        )
    }

    private fun buildWaterRecord(measurement: OpenScaleMeasurement): BodyWaterMassRecord {
        val measurementInstant = measurement.date.toInstant()
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(measurementInstant)

        return BodyWaterMassRecord(
            time = measurementInstant,
            zoneOffset = zoneOffset,
            mass = Mass.kilograms(measurement.weight.toDouble() * measurement.water.toDouble() / 100),
            metadata = buildMetadata(measurement, "water")
        )
    }

    private fun buildFatRecord(measurement: OpenScaleMeasurement): BodyFatRecord {
        val measurementInstant = measurement.date.toInstant()
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(measurementInstant)

        return BodyFatRecord(
            time = measurementInstant,
            zoneOffset = zoneOffset,
            percentage = Percentage(measurement.fat.toDouble()),
            metadata = buildMetadata(measurement, "fat")
        )
    }

    private fun buildMuscleRecord(measurement: OpenScaleMeasurement): LeanBodyMassRecord {
        val measurementInstant = measurement.date.toInstant()
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(measurementInstant)

        return LeanBodyMassRecord(
            time = measurementInstant,
            zoneOffset = zoneOffset,
            mass = Mass.kilograms(measurement.weight.toDouble() * measurement.muscle.toDouble() / 100),
            metadata = buildMetadata(measurement, "muscle")
        )
    }

    private fun buildBoneRecord(measurement: OpenScaleMeasurement): BoneMassRecord {
        val measurementInstant = measurement.date.toInstant()
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(measurementInstant)

        return BoneMassRecord(
            time = measurementInstant,
            zoneOffset = zoneOffset,
            mass = Mass.kilograms(measurement.bone.toDouble()),
            metadata = buildMetadata(measurement, "bone")
        )
    }

    private fun buildBmrRecord(measurement: OpenScaleMeasurement): BasalMetabolicRateRecord {
        val measurementInstant = measurement.date.toInstant()
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(measurementInstant)

        return BasalMetabolicRateRecord(
            time = measurementInstant,
            zoneOffset = zoneOffset,
            basalMetabolicRate = Power.kilocaloriesPerDay(measurement.bmr.toDouble()),
            metadata = buildMetadata(measurement, "bmr")
        )
    }
}