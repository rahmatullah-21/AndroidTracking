# Firebase cloud sync + multi-device admin (drop-in)

This folder turns the offline-by-default Android app into a multi-device system the
[web admin panel](../../web-admin/) can monitor — using a **pairing-code** device-linking flow
that needs **no Cloud Functions** (works on the free Spark plan).

## How a device registers (pairing code)

1. **Admin panel** → *Devices → Add device* → generates a short code in `pairingCodes/{code}`
   (stamped with the admin's `ownerUid`, expires in ~15 min).
2. **Device** → *Settings → Cloud sync → Link device* → enters the code + confirms consent.
3. The device **signs in anonymously**, reads that code, and creates its own
   `devices/{deviceId}` document with `ownerUid` (from the code) + `claimedByUid` (its anon uid).
4. **Firestore rules** ([`../../firestore.rules`](../../firestore.rules)) validate the code + expiry
   on create, then allow only that device (matching `claimedByUid`) to update its own subtree, and
   only the owner to read devices where `ownerUid == their uid`.
5. `CloudSyncWorker` keeps the document + subcollections fresh.

The owner never shares a password; the device never reads other devices' data. See
[FIRESTORE_SCHEMA.md](FIRESTORE_SCHEMA.md) for the data model + rules diagram.

## Enable it

**Firebase console**
1. Create a project; add an **Android app** (`com.deviceinsight.pro`) → download `google-services.json` into `app/`.
2. **Authentication → Sign-in method**: enable **Email/Password** (admins) and **Anonymous** (devices).
3. **Firestore**: create the database, then `firebase deploy --only firestore:rules` (or paste
   [`../../firestore.rules`](../../firestore.rules) in the Rules tab).

**Android app**
1. Uncomment the `google-services` plugin + Firebase deps (README → *Firebase setup*) and add
   `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")`.
2. Copy these files into the app and **remove** the no-op `bindCloudSyncRepository` binding in
   `RepositoryModule.kt`:
   - `FirestoreCloudSyncRepository.kt` → `data/repository/`
   - `DeviceLinkRepository.kt` → `data/repository/`
   - `CloudSyncModule.kt` → `di/`
   - `CloudSyncWorker.kt` → `workers/`
   - `CloudSyncScreen.kt` → `presentation/cloud/` (and add a route from `SettingsScreen`)
3. Schedule `CloudSyncWorker` (e.g. periodic every 2h + once right after linking).

**Web admin** — see [../../web-admin/README.md](../../web-admin/README.md): set `VITE_USE_MOCK=false`
and your `VITE_FIREBASE_*` values, then *Add device* to mint pairing codes.

## Files

| File | Role |
|------|------|
| `FirestoreCloudSyncRepository.kt` | Uploads consented analytics (merge-update; never changes ownership). |
| `DeviceLinkRepository.kt` | One-time claim: anonymous sign-in + create device doc from a code. |
| `CloudSyncWorker.kt` | Periodic background sync. |
| `CloudSyncModule.kt` | Hilt: provides Firebase singletons + binds the Firestore repo. |
| `CloudSyncScreen.kt` | Device-side "enter code + consent" UI (reference). |
