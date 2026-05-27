# Firestore schema — multi-device monitoring

This is the contract shared by the **Android cloud-sync module** (writer) and the **React admin
panel** (reader). Sync is **opt-in per device** and only runs when the device user enables it.

```
pairingCodes/{code}                        (document, id = the short pairing code)
  ownerUid: string                         # admin who generated the code
  createdAt: timestamp
  expiresAt: timestamp                      # short-lived (e.g. now + 15 min)

devices/{deviceId}                         (document, deviceId = stable per-install UUID)
  deviceId: string
  label: string                            # e.g. "Samsung SM-G991B"
  model: string
  manufacturer: string
  appVersion: string
  ownerUid: string                         # owner account, copied from the pairing code at claim time
  claimedByUid: string                     # the device's anonymous-auth uid (write permission key)
  pairingCode: string                      # the code used to claim (validated by rules on create)
  monitoringConsent: boolean               # the device user acknowledged monitoring
  lastSeen: timestamp
  summary: {
    screenTimeMsToday: number
    unlocksToday: number
    notificationsToday: number
    messagesToday: number
    securityScore: number                  # 0..100
    focusScore: number                     # 0..100
    batteryLevel: number                   # 0..100
    isCharging: boolean
  }

devices/{deviceId}/usage/{epochDay}        (document, epochDay = days since epoch)
  epochDay: number
  totalForegroundMs: number
  apps: [ { packageName, appName, totalForegroundMs, launchCount, lastTimeUsed } ]   # top N
  updatedAt: timestamp

devices/{deviceId}/messages/{messageId}    (collection)
  platform: string                         # WHATSAPP | INSTAGRAM | MESSENGER | ...
  appName: string
  sender: string
  conversation: string | null
  preview: string                          # short notification preview only
  isGroup: boolean
  timestamp: number

devices/{deviceId}/events/{eventId}        (collection)
  type: string                             # SCREEN_ON | CHARGING_CONNECTED | ...
  label: string
  detail: string | null
  timestamp: number

devices/{deviceId}/security/latest         (document)
  score: number
  scannedAppCount: number
  findings: [ { id, title, description, severity, recommendation } ]
  updatedAt: timestamp
```

## Security rules

The full, deployable rules live in [`../../firestore.rules`](../../firestore.rules)
(`firebase deploy --only firestore:rules`). They implement the **pairing-code** model below — a
device can only claim itself under an owner if it presents a valid, unexpired pairing code, and
thereafter may only touch its own subtree. Don't loosen them: they are what keeps monitoring scoped
to devices the owner actually controls.

## Device registration (pairing-code flow)

```
Admin panel (owner signed in)            Device (Device Insight Pro)
─────────────────────────────           ───────────────────────────
1. "Add device" →
   create pairingCodes/{CODE}
   { ownerUid, expiresAt }
   show CODE to the admin   ───────────▶ 2. Settings → Cloud sync → "Link device"
                                            enter CODE, confirm consent
                                         3. sign in anonymously  → uid
                                         4. read pairingCodes/{CODE} → ownerUid
                                         5. create devices/{deviceId}
                                            { ownerUid, claimedByUid: uid, pairingCode: CODE, … }
                                            (rules validate the code server-side)
6. panel lists devices       ◀──────────  7. CloudSyncWorker keeps the doc + subcollections fresh
   where ownerUid == myUid                    (allowed because claimedByUid == uid)
```

- The owner **never shares their password**; the device authenticates anonymously.
- The pairing code is a short-lived secret (≈15 min). A leaked code can only register a device under
  that owner (which the owner can then see and delete) — it grants no read access to existing data.
- Enable **Anonymous** auth (for devices) and **Email/Password** auth (for admins) in the Firebase
  console.
