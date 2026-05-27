# Firestore schema — multi-device monitoring

This is the contract shared by the **Android cloud-sync module** (writer) and the **React admin
panel** (reader). Sync is **opt-in per device** and only runs when the device user enables it.

```
devices/{deviceId}                         (document)
  deviceId: string
  label: string                            # e.g. "Samsung SM-G991B"
  model: string
  manufacturer: string
  appVersion: string
  ownerUid: string                         # the admin/owner account that registered the device
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

## Security rules (starting point)

Devices may only write their own document/subtree; admins may read devices they own.

```
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    match /devices/{deviceId} {
      allow read:  if request.auth != null && resource.data.ownerUid == request.auth.uid;
      allow write: if request.auth != null && request.resource.data.ownerUid == request.auth.uid;
      match /{sub=**} {
        allow read, write: if request.auth != null
          && get(/databases/$(db)/documents/devices/$(deviceId)).data.ownerUid == request.auth.uid;
      }
    }
  }
}
```

> Both the Android device and the admin panel authenticate as the **same owner account** (or the
> device uses a credential the owner provisioned). The admin only ever sees devices whose
> `ownerUid` matches their UID. Don't loosen these rules — they are what keeps monitoring scoped to
> devices the owner actually controls.
