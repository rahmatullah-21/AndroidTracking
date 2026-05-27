export interface DeviceSummary {
  screenTimeMsToday: number
  unlocksToday: number
  notificationsToday: number
  messagesToday: number
  securityScore: number
  focusScore: number
  batteryLevel: number
  isCharging: boolean
}

export interface Device {
  deviceId: string
  label: string
  model?: string
  manufacturer?: string
  appVersion?: string
  ownerUid?: string
  monitoringConsent?: boolean
  lastSeen?: number
  summary: DeviceSummary
}

export interface AppUsage {
  packageName: string
  appName: string
  totalForegroundMs: number
  launchCount: number
  lastTimeUsed: number
}

export interface UsageDay {
  epochDay: number
  totalForegroundMs: number
  apps: AppUsage[]
}

export type SocialPlatform =
  | 'WHATSAPP' | 'MESSENGER' | 'INSTAGRAM' | 'TELEGRAM' | 'SNAPCHAT'
  | 'SIGNAL' | 'DISCORD' | 'WECHAT' | 'TWITTER' | 'SMS' | 'OTHER'

export interface SocialMessage {
  id: string
  platform: SocialPlatform
  appName: string
  sender: string
  conversation?: string | null
  preview: string
  isGroup: boolean
  timestamp: number
}

export interface DeviceEvent {
  id: string
  type: string
  label: string
  detail?: string | null
  timestamp: number
}

export type Severity = 'INFO' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface SecurityFinding {
  id: string
  title: string
  description: string
  severity: Severity
  recommendation: string
}

export interface SecurityReport {
  score: number
  scannedAppCount: number
  findings: SecurityFinding[]
  updatedAt?: number
}
