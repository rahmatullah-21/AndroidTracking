import type {
  Device, DeviceEvent, SecurityReport, SocialMessage, UsageDay,
} from './types'

const NOW = Date.now()
const HOUR = 3_600_000
const DAY = 86_400_000

export const mockDevices: Device[] = [
  {
    deviceId: 'demo-pixel-8',
    label: 'Google Pixel 8',
    model: 'Pixel 8', manufacturer: 'Google', appVersion: '1.0.0',
    monitoringConsent: true, lastSeen: NOW - 5 * 60_000,
    summary: {
      screenTimeMsToday: 4.2 * HOUR, unlocksToday: 87, notificationsToday: 214,
      messagesToday: 63, securityScore: 78, focusScore: 64, batteryLevel: 72, isCharging: false,
    },
  },
  {
    deviceId: 'demo-galaxy-a54',
    label: 'Samsung Galaxy A54',
    model: 'SM-A546B', manufacturer: 'Samsung', appVersion: '1.0.0',
    monitoringConsent: true, lastSeen: NOW - 22 * 60_000,
    summary: {
      screenTimeMsToday: 6.8 * HOUR, unlocksToday: 142, notificationsToday: 388,
      messagesToday: 121, securityScore: 54, focusScore: 41, batteryLevel: 38, isCharging: true,
    },
  },
  {
    deviceId: 'demo-redmi-note',
    label: 'Redmi Note 12',
    model: '23021RAA2Y', manufacturer: 'Xiaomi', appVersion: '1.0.0',
    monitoringConsent: true, lastSeen: NOW - 3 * HOUR,
    summary: {
      screenTimeMsToday: 2.1 * HOUR, unlocksToday: 49, notificationsToday: 96,
      messagesToday: 18, securityScore: 91, focusScore: 82, batteryLevel: 95, isCharging: false,
    },
  },
]

const PLATFORMS = ['WHATSAPP', 'INSTAGRAM', 'MESSENGER', 'TELEGRAM', 'SNAPCHAT', 'SMS'] as const
const SENDERS = ['Mom', 'Riley', 'Study Group', 'Coach', 'Jamie', 'Unknown', 'Alex P.', 'Team']
const SAMPLES = [
  'See you at 6?', 'Did you finish the homework?', 'lol that’s wild',
  'Call me when you can', 'Sent a photo', 'Are you coming tonight?',
  'Limited offer — 50% off!', 'Meeting moved to 3pm', 'Happy birthday! 🎉',
]

export function mockMessages(deviceId: string): SocialMessage[] {
  const seed = deviceId.length
  return Array.from({ length: 40 }, (_, i) => ({
    id: `${deviceId}-m${i}`,
    platform: PLATFORMS[(i + seed) % PLATFORMS.length],
    appName: PLATFORMS[(i + seed) % PLATFORMS.length],
    sender: SENDERS[(i * 3 + seed) % SENDERS.length],
    conversation: i % 4 === 0 ? 'Family' : null,
    preview: SAMPLES[(i * 5 + seed) % SAMPLES.length],
    isGroup: i % 4 === 0,
    timestamp: NOW - i * 17 * 60_000,
  }))
}

const EVENTS = [
  ['SCREEN_ON', 'Screen on'], ['DEVICE_UNLOCKED', 'Device unlocked'],
  ['CHARGING_CONNECTED', 'Charger connected'], ['WIFI_ENABLED', 'Wi-Fi enabled'],
  ['SCREEN_OFF', 'Screen off'], ['HEADPHONES_CONNECTED', 'Headphones connected'],
  ['BLUETOOTH_CONNECTED', 'Bluetooth device connected'], ['BATTERY_LOW', 'Battery low'],
]

export function mockEvents(deviceId: string): DeviceEvent[] {
  return Array.from({ length: 30 }, (_, i) => {
    const [type, label] = EVENTS[i % EVENTS.length]
    return { id: `${deviceId}-e${i}`, type, label, detail: null, timestamp: NOW - i * 11 * 60_000 }
  })
}

export function mockSecurity(deviceId: string): SecurityReport {
  const score = mockDevices.find((d) => d.deviceId === deviceId)?.summary.securityScore ?? 70
  return {
    score, scannedAppCount: 64, updatedAt: NOW - HOUR,
    findings: [
      { id: 'cam', title: '4 apps with camera access', description: 'Camera is granted to 4 installed apps.', severity: 'MEDIUM', recommendation: 'Review camera access for apps you don’t recognize.' },
      { id: 'sms', title: '2 apps with SMS access', description: 'SMS access can expose 2FA codes.', severity: 'HIGH', recommendation: 'Revoke SMS from non-messaging apps.' },
      { id: 'a11y', title: '1 accessibility service enabled', description: 'Accessibility services can read screen content.', severity: 'MEDIUM', recommendation: 'Disable accessibility for apps you don’t rely on.' },
    ],
  }
}

export function mockUsageDays(deviceId: string): UsageDay[] {
  const seed = deviceId.length
  const apps = [
    ['com.instagram.android', 'Instagram'], ['com.whatsapp', 'WhatsApp'],
    ['com.google.android.youtube', 'YouTube'], ['com.android.chrome', 'Chrome'],
    ['com.spotify.music', 'Spotify'],
  ]
  const today = Math.floor(NOW / DAY)
  return Array.from({ length: 7 }, (_, i) => ({
    epochDay: today - (6 - i),
    totalForegroundMs: (2 + ((i * 7 + seed) % 5)) * HOUR,
    apps: apps.map(([pkg, name], j) => ({
      packageName: pkg, appName: name,
      totalForegroundMs: (5 - j) * 25 * 60_000 + ((i + seed) % 3) * 10 * 60_000,
      launchCount: 30 - j * 4 + i,
      lastTimeUsed: NOW - j * HOUR,
    })),
  }))
}
