import {
  collection, doc, getDoc, getDocs, limit, orderBy, query, where,
} from 'firebase/firestore'
import { USE_MOCK, getDb } from './firebase'
import * as mock from './mockData'
import type {
  Device, DeviceEvent, SecurityReport, SocialMessage, UsageDay,
} from './types'

export async function listDevices(ownerUid: string): Promise<Device[]> {
  if (USE_MOCK) return mock.mockDevices
  const db = getDb()
  const snap = await getDocs(query(collection(db, 'devices'), where('ownerUid', '==', ownerUid)))
  return snap.docs.map((d) => ({ deviceId: d.id, ...(d.data() as Omit<Device, 'deviceId'>) }))
}

export async function getDevice(deviceId: string): Promise<Device | null> {
  if (USE_MOCK) return mock.mockDevices.find((d) => d.deviceId === deviceId) ?? null
  const db = getDb()
  const snap = await getDoc(doc(db, 'devices', deviceId))
  return snap.exists() ? ({ deviceId: snap.id, ...(snap.data() as Omit<Device, 'deviceId'>) }) : null
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
