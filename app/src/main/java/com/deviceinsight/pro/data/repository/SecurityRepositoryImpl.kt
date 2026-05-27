package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.data.mapper.toDomain
import com.deviceinsight.pro.data.mapper.toEntity
import com.deviceinsight.pro.data.source.SecurityScanner
import com.deviceinsight.pro.database.dao.SecurityEventDao
import com.deviceinsight.pro.domain.model.SecurityReport
import com.deviceinsight.pro.domain.model.SecuritySeverity
import com.deviceinsight.pro.domain.repository.SecurityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    private val scanner: SecurityScanner,
    private val dao: SecurityEventDao
) : SecurityRepository {

    @Volatile
    private var lastScannedCount: Int = 0

    override fun observeReport(): Flow<SecurityReport> =
        dao.observeAll().map { rows ->
            val findings = rows.map { it.toDomain() }
            SecurityReport(
                score = scoreOf(findings.map { it.severity }),
                scannedAppCount = lastScannedCount,
                findings = findings
            )
        }

    override suspend fun runScan(): SecurityReport = withContext(Dispatchers.IO) {
        val report = scanner.scan()
        lastScannedCount = report.scannedAppCount
        val now = System.currentTimeMillis()
        dao.clear()
        dao.insertAll(report.findings.map { it.toEntity(now) })
        report
    }

    private fun scoreOf(severities: List<SecuritySeverity>): Int {
        var score = 100
        for (s in severities) {
            score -= when (s) {
                SecuritySeverity.CRITICAL -> 25
                SecuritySeverity.HIGH -> 15
                SecuritySeverity.MEDIUM -> 8
                SecuritySeverity.LOW -> 3
                SecuritySeverity.INFO -> 0
            }
        }
        return score.coerceIn(0, 100)
    }
}
