package com.deviceinsight.pro.data.source

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.deviceinsight.pro.domain.model.SecurityFinding
import com.deviceinsight.pro.domain.model.SecurityReport
import com.deviceinsight.pro.domain.model.SecuritySeverity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Inspects installed apps, granted permissions and device posture to produce a
 * device security score. Read-only — it never modifies device state.
 */
@Singleton
class SecurityScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pm = context.packageManager

    private data class PermCount(var camera: Int = 0, var mic: Int = 0, var location: Int = 0, var sms: Int = 0, var overlay: Int = 0, var install: Int = 0)

    fun scan(): SecurityReport {
        val findings = mutableListOf<SecurityFinding>()
        val counts = PermCount()
        var scanned = 0

        val packages = runCatching {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }.getOrDefault(emptyList())

        for (info in packages) {
            val isSystem = (info.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
            if (isSystem) continue
            scanned++
            val granted = grantedPermissions(info.requestedPermissions, info.requestedPermissionsFlags)
            if ("android.permission.CAMERA" in granted) counts.camera++
            if ("android.permission.RECORD_AUDIO" in granted) counts.mic++
            if ("android.permission.ACCESS_FINE_LOCATION" in granted ||
                "android.permission.ACCESS_BACKGROUND_LOCATION" in granted
            ) counts.location++
            if ("android.permission.READ_SMS" in granted) counts.sms++
            if ("android.permission.SYSTEM_ALERT_WINDOW" in granted) counts.overlay++
            if ("android.permission.REQUEST_INSTALL_PACKAGES" in granted) counts.install++
        }

        addCount(findings, counts.camera, "camera", "Camera access", SecuritySeverity.MEDIUM,
            "Review which apps can use your camera and revoke access you don't recognize.")
        addCount(findings, counts.mic, "microphone", "Microphone access", SecuritySeverity.MEDIUM,
            "Revoke microphone access from apps that don't need it.")
        addCount(findings, counts.location, "fine/background location", "Location access", SecuritySeverity.MEDIUM,
            "Limit location to 'while using the app' where possible.")
        addCount(findings, counts.sms, "SMS read", "SMS access", SecuritySeverity.HIGH,
            "SMS access can expose 2FA codes — revoke from non-messaging apps.")
        addCount(findings, counts.overlay, "draw-over-other-apps (overlay)", "Overlay permission", SecuritySeverity.HIGH,
            "Overlays can be abused for tap-jacking; review carefully.")
        addCount(findings, counts.install, "install-unknown-apps", "Unknown-source installs", SecuritySeverity.HIGH,
            "Disable 'install unknown apps' for apps you don't trust.")

        // Accessibility services
        val a11y = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (!a11y.isNullOrBlank()) {
            val n = a11y.split(":").count { it.isNotBlank() }
            findings += SecurityFinding(
                id = "accessibility",
                title = "$n accessibility service(s) enabled",
                description = "Accessibility services can read screen content and perform actions on your behalf.",
                severity = SecuritySeverity.MEDIUM,
                recommendation = "Disable accessibility access for any app you don't actively rely on."
            )
        }

        // VPN active
        if (isVpnActive()) {
            findings += SecurityFinding(
                id = "vpn",
                title = "A VPN is currently active",
                description = "All traffic is routed through a VPN. Make sure it's one you installed.",
                severity = SecuritySeverity.INFO,
                recommendation = "Verify the active VPN profile in system settings."
            )
        }

        if (findings.isEmpty()) {
            findings += SecurityFinding(
                id = "clean",
                title = "No notable risks detected",
                description = "Your installed apps don't hold an unusual number of sensitive permissions.",
                severity = SecuritySeverity.INFO,
                recommendation = "Keep reviewing permissions periodically."
            )
        }

        return SecurityReport(
            score = computeScore(findings),
            scannedAppCount = scanned,
            findings = findings.sortedByDescending { it.severity.ordinal }
        )
    }

    private fun grantedPermissions(perms: Array<String>?, flags: IntArray?): Set<String> {
        if (perms == null) return emptySet()
        val result = HashSet<String>()
        for (i in perms.indices) {
            val isGranted = flags != null && i < flags.size &&
                (flags[i] and android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            if (isGranted) result += perms[i]
        }
        return result
    }

    private fun addCount(
        out: MutableList<SecurityFinding>,
        count: Int,
        what: String,
        title: String,
        severity: SecuritySeverity,
        recommendation: String
    ) {
        if (count <= 0) return
        out += SecurityFinding(
            id = "perm_$what",
            title = "$count app(s) with $what",
            description = "$title is granted to $count installed app(s).",
            severity = if (count >= 6) bump(severity) else severity,
            recommendation = recommendation
        )
    }

    private fun bump(s: SecuritySeverity) = when (s) {
        SecuritySeverity.LOW -> SecuritySeverity.MEDIUM
        SecuritySeverity.MEDIUM -> SecuritySeverity.HIGH
        SecuritySeverity.HIGH -> SecuritySeverity.CRITICAL
        else -> s
    }

    private fun isVpnActive(): Boolean = runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    }.getOrDefault(false)

    private fun computeScore(findings: List<SecurityFinding>): Int {
        var score = 100
        for (f in findings) {
            score -= when (f.severity) {
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
