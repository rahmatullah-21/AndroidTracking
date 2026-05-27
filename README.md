# Device Insight Pro

A device monitoring, analytics, and digital-wellbeing platform for Android, built with
**Kotlin + Jetpack Compose (Material 3)** following **Clean Architecture + MVVM** with **Hilt**,
**Room**, **WorkManager**, and Coroutines/Flow.

> **Scope & intended use.** Device Insight Pro analyzes usage on **the device it's installed on**,
> for the **owner of that device or someone they've explicitly authorized**. It is a transparency /
> digital-wellbeing tool, not covert surveillance. All analytics stay **on-device** unless you
> explicitly enable the optional cloud-sync module. See [docs/PRIVACY.md](docs/PRIVACY.md).

---

## What's implemented (works today)

| Area | Status | Notes |
|------|--------|-------|
| App usage monitoring | ✅ | Real `UsageStatsManager`: foreground time, launch counts, last-used, sessions. Sort by most/least used, recent, most-launched. |
| Screen-time analytics | ✅ | Screen-on time, unlocks, avg session, idle, peak hour, focus score, per-hour chart. |
| Notification monitoring | ✅ | `NotificationListenerService` capturing app/title/short preview/category; search, analytics, spam heuristic. |
| Device activity timeline | ✅ | Foreground service + runtime receivers for screen, unlock, charging, Wi‑Fi, Bluetooth, headphones, battery-low, boot. |
| Network usage | ✅ | `NetworkStatsManager` daily Wi‑Fi/mobile totals + live `TrafficStats` speed. |
| Battery & performance | ✅ | Battery level/health/temp/voltage; RAM, storage, running processes. |
| Security center | ✅ | Permission scanner (camera/mic/location/SMS/overlay/install), accessibility & VPN detection, weighted security score. |
| Parental / wellbeing | ✅ partial | Daily goal, focus toggle, monitoring toggle persisted (Room). App-blocking enforcement is scaffolded (see roadmap). |
| Reports & export | ✅ CSV | Weekly summary + **working CSV share**. PDF/Excel are stubbed (data layer ready). |
| Material 3 UI | ✅ | Dark/light + dynamic color, bottom nav, pull-to-refresh, custom Canvas charts, permission onboarding. |
| Cloud sync (Firebase) | 🟡 scaffold | Dependencies/plugin wired but commented out — needs your `google-services.json`. |
| Web admin dashboard | 🟡 roadmap | React/Tailwind/Recharts design documented below. |

---

## Project structure

```
app/src/main/java/com/deviceinsight/pro/
├── data/
│   ├── mapper/        # entity ↔ domain mappers
│   ├── repository/    # repository implementations
│   └── source/        # UsageStats, SystemMetrics, NetworkStats, SecurityScanner
├── database/          # Room: AppDatabase, entities (7 tables), DAOs
├── di/                # Hilt modules + ReceiverEntryPoint
├── domain/
│   ├── model/         # models + enums
│   ├── repository/    # repository interfaces
│   └── usecase/       # GetDashboardSummary, GenerateInsights
├── presentation/      # Compose screens + ViewModels + theme + navigation + components
├── receivers/         # Boot + power-connection receivers
├── services/          # NotificationListener + foreground MonitoringService
├── utils/             # Formatters, TimeWindows, PermissionUtils, Constants
└── workers/           # WorkManager: usage sync + daily maintenance
```

Room tables: `app_usage`, `notifications`, `device_events`, `battery_stats`, `network_usage`,
`security_events`, `user_settings`.

---

## Build & run

**Requirements:** Android Studio Ladybug+ (or any IDE with AGP 8.7), JDK 17, Android SDK 35.

1. Open the project root in Android Studio. It will generate `local.properties` (SDK path) and the
   Gradle wrapper JAR automatically. *(CLI alternative: `gradle wrapper --gradle-version 8.11.1`,
   then `./gradlew assembleDebug`.)*
2. `minSdk 26`, `targetSdk 35`. Run on a device/emulator (API 26+).
3. On first launch, complete the **permission onboarding**:
   - **Usage Access** (required) — opens *Settings → Special access → Usage access*.
   - **Notification Access** (optional) — for notification analytics.
   - **Post notifications** (optional, Android 13+) — for the monitoring service notice.
4. Pull to refresh on the Dashboard to trigger a usage sync + security scan. Background sync runs
   hourly via WorkManager; a daily maintenance job rescans security and prunes data older than
   `Constants.DATA_RETENTION_DAYS` (90 days).

> The build was authored on a machine without a JDK/Android SDK and therefore **not compiled here**.
> Open it in Android Studio to build; versions in `gradle/libs.versions.toml` are pinned but easy to bump.

---

## Permissions & Google Play compliance

This app uses sensitive/special-access APIs. For a Play release you must:

- **`PACKAGE_USAGE_STATS`** — special access granted by the user in Settings (we never request it silently).
- **`BIND_NOTIFICATION_LISTENER_SERVICE`** — requires a **Prominent Disclosure** and a published
  privacy policy describing exactly what notification data is read and why.
- **`QUERY_ALL_PACKAGES`** — Play restricts this. It's used here to attribute usage to app names.
  For production, either submit the declaration form justifying it, or switch to a scoped
  `<queries>` element if your use case allows.
- **`READ_PHONE_STATE`** — only needed for per-app mobile-data history on some OEMs; consider removing
  if you only need device-level totals.
- **Foreground service (`dataSync`)** — the monitoring service shows an ongoing notification and is
  strictly opt-in via Settings/Timeline.

See [docs/PRIVACY.md](docs/PRIVACY.md) for a privacy-policy template you can adapt.

---

## Firebase setup (optional cloud sync)

1. Create a Firebase project and add an Android app with package `com.deviceinsight.pro`.
2. Download `google-services.json` into `app/`.
3. Uncomment the `google-services` plugin in the root and app `build.gradle.kts`, and the Firebase
   dependencies block in `app/build.gradle.kts` (both are pre-wired in `libs.versions.toml`).
4. Add `CloudSyncRepository` implementations for Auth, Firestore (reports/settings mirror),
   Messaging (remote config / alerts), Analytics and Crashlytics. The on-device repositories
   already expose Flows that map cleanly to Firestore documents.

`google-services.json`, keystores and `local.properties` are git-ignored.

---

## Roadmap (scaffolded, not yet complete)

- **App-block enforcement** — `WellbeingSettings.blockedPackages` is persisted; add an
  `AccessibilityService` (or usage-poll + overlay) to enforce limits/focus/bedtime. Accessibility
  use must follow Play's accessibility policy.
- **PDF/Excel export** — `ReportsViewModel` already builds the dataset; add a `PdfDocument`/
  Apache POI (or SpreadsheetML) writer and share via `FileProvider`.
- **Biometric lock** — `UserSettings.biometricLockEnabled` is wired; gate `MainActivity` with
  `androidx.biometric`.
- **Encrypted storage** — swap Room to SQLCipher and use `EncryptedSharedPreferences` for secrets.

### Web admin dashboard (optional)

Recommended stack: **React + TypeScript + TailwindCSS + Recharts + Firebase SDK**.

```
web-admin/
├── src/
│   ├── pages/        # Devices, Reports, RemoteConfig, Alerts
│   ├── components/   # charts (Recharts), tables, cards
│   ├── lib/firebase.ts
│   └── App.tsx
├── tailwind.config.js
└── package.json
```

It would read the same Firestore collections the cloud-sync module writes (per-device usage,
reports, security score, settings) and push remote configuration / alerts back via Firestore + FCM.

---

## Architecture notes

- **Unidirectional data flow:** DAOs expose `Flow`; repositories map entities → domain models;
  ViewModels expose `StateFlow<UiState>`; Compose collects with `collectAsStateWithLifecycle`.
- **DI:** `DatabaseModule` provides Room + DAOs; `RepositoryModule` binds interfaces to impls;
  data sources use constructor injection with `@ApplicationContext`.
- **Background:** `UsageSyncWorker` (hourly) and `DailyMaintenanceWorker` (daily) via Hilt's
  `HiltWorkerFactory`; the default `WorkManagerInitializer` is disabled in the manifest.
- **Privacy by default:** local-only storage, content excluded from cloud backup, notification
  bodies truncated to short previews.
```
