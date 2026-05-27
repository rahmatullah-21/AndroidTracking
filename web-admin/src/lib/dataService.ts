import {
  collection, deleteDoc, doc, getDoc, getDocs, limit, orderBy, query, where,
} from 'firebase/firestore'
import { USE_MOCK, getDb } from './firebase'
import * as mock from './mockData'
import type {
  Device, DeviceEvent, SecurityReport, SocialMessage, UsageDay,
} from './types'

/**
 * Unlinks a device the owner controls (removes the top-level document). Its subcollections then
 * become unreadable because the rules check the now-missing parent doc. For full storage cleanup,
 * run a recursive delete via the Firebase CLI / a scheduled job — omitted to stay Functions-free.
 */
export async function deleteDevice(deviceId: string): Promise<void> {
  if (USE_MOCK) return
  await deleteDoc(doc(getDb(), 'devices', deviceId))
}

// Firestore stores lastSeen as a Timestamp and writes summary lazily; normalize to the app's
// shape (numeric lastSeen, fully-populated summary) so the UI never shows undefined/garbage.
function toMillis(v: any): number | undefined {
  if (v == null) return undefined
  if (typeof v === 'number') return v
  if (typeof v.toMillis === 'function') return v.toMillis()
  if (typeof v.seconds === 'number') return v.seconds * 1000
  return undefined
}

function normalizeDevice(id: string, raw: any): Device {
  const s = raw?.summary ?? {}
  return {
    deviceId: id,
    label: raw?.label ?? id,
    model: raw?.model ?? '',
    manufacturer: raw?.manufacturer ?? '',
    appVersion: raw?.appVersion,
    ownerUid: raw?.ownerUid,
    monitoringConsent: raw?.monitoringConsent,
    lastSeen: toMillis(raw?.lastSeen),
    summary: {
      screenTimeMsToday: s.screenTimeMsToday ?? 0,
      unlocksToday: s.unlocksToday ?? 0,
      notificationsToday: s.notificationsToday ?? 0,
      messagesToday: s.messagesToday ?? 0,
      securityScore: s.securityScore ?? 0,
      focusScore: s.focusScore ?? 0,
      batteryLevel: s.batteryLevel ?? 0,
      isCharging: s.isCharging ?? false,
    },
  }
}

export async function listDevices(ownerUid: string): Promise<Device[]> {
  if (USE_MOCK) return mock.mockDevices
  const db = getDb()
  const snap = await getDocs(query(collection(db, 'devices'), where('ownerUid', '==', ownerUid)))
  return snap.docs.map((d) => normalizeDevice(d.id, d.data()))
}

export async function getDevice(deviceId: string): Promise<Device | null> {
  if (USE_MOCK) return mock.mockDevices.find((d) => d.deviceId === deviceId) ?? null
  const db = getDb()
  const snap = await getDoc(doc(db, 'devices', deviceId))
  return snap.exists() ? normalizeDevice(snap.id, snap.data()) : null
}

export async function getMessages(deviceId: string): Promise<SocialMessage[]> {
  if (USE_MOCK) return mock.mockMessages(deviceId)
  const db = getDb()
  const snap = await getDocs(query(
    collection(db, 'devices', deviceId, 'messages'),
    orderBy('timestamp', 'desc'), limit(200),
  ))
  return snap.docs.map((d) => ({ id: d.id, ...(d.data() as Omit<SocialMessage, 'id'>) }))
}

export async function getEvents(deviceId: string): Promise<DeviceEvent[]> {
  if (USE_MOCK) return mock.mockEvents(deviceId)
  const db = getDb()
  const snap = await getDocs(query(
    collection(db, 'devices', deviceId, 'events'),
    orderBy('timestamp', 'desc'), limit(200),
  ))
  return snap.docs.map((d) => ({ id: d.id, ...(d.data() as Omit<DeviceEvent, 'id'>) }))
}

export async function getSecurity(deviceId: string): Promise<SecurityReport | null> {
  if (USE_MOCK) return mock.mockSecurity(deviceId)
  const db = getDb()
  const snap = await getDoc(doc(db, 'devices', deviceId, 'security', 'latest'))
  return snap.exists() ? (snap.data() as SecurityReport) : null
}

export async function getUsageDays(deviceId: string): Promise<UsageDay[]> {
  if (USE_MOCK) return mock.mockUsageDays(deviceId)
  const db = getDb()
  const snap = await getDocs(query(
    collection(db, 'devices', deviceId, 'usage'),
    orderBy('epochDay', 'asc'), limit(30),
  ))
  return snap.docs.map((d) => d.data() as UsageDay)
}
