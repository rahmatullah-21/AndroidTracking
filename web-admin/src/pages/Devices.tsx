import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import PairingModal from '../components/PairingModal'
import Spinner from '../components/Spinner'
import { useAuth } from '../context/AuthContext'
import { listDevices } from '../lib/dataService'
import { formatDuration, relativeTime, scoreColor } from '../lib/format'
import type { Device } from '../lib/types'

export default function Devices() {
  const { user } = useAuth()
  const [devices, setDevices] = useState<Device[] | null>(null)
  const [pairing, setPairing] = useState(false)

  useEffect(() => {
    listDevices(user!.uid).then(setDevices).catch(() => setDevices([]))
  }, [user])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Devices</h1>
        <button
          onClick={() => setPairing(true)}
          className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-dark"
        >
          + Add device
        </button>
      </div>

      {!devices ? (
        <Spinner label="Loading devices…" />
      ) : devices.length === 0 ? (
        <div className="rounded-xl bg-white p-8 text-center text-slate-500 ring-1 ring-slate-200">
          No devices linked yet. Click <b>Add device</b> to generate a pairing code, then enter it on
          the device (Settings → Cloud sync → Link device).
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {devices.map((d) => (
            <Link
              key={d.deviceId}
              to={`/devices/${d.deviceId}`}
              className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-slate-200 transition hover:shadow-md"
            >
              <div className="flex items-center justify-between">
                <div className="font-semibold text-slate-900">{d.label}</div>
                <span
                  className="rounded-full px-2 py-0.5 text-xs font-semibold text-white"
                  style={{ backgroundColor: scoreColor(d.summary.securityScore) }}
                >
                  {d.summary.securityScore}
                </span>
              </div>
              <div className="mt-1 text-xs text-slate-400">
                {d.manufacturer} {d.model} • {d.lastSeen ? relativeTime(d.lastSeen) : 'unknown'}
              </div>
              <div className="mt-4 grid grid-cols-2 gap-2 text-sm">
                <Metric label="Screen time" value={formatDuration(d.summary.screenTimeMsToday)} />
                <Metric label="Unlocks" value={String(d.summary.unlocksToday)} />
                <Metric label="Messages" value={String(d.summary.messagesToday)} />
                <Metric label="Battery" value={`${d.summary.batteryLevel}%`} />
              </div>
            </Link>
          ))}
        </div>
      )}

      {pairing && <PairingModal ownerUid={user!.uid} onClose={() => setPairing(false)} />}
    </div>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <div className="text-xs text-slate-400">{label}</div>
      <div className="font-medium text-slate-800">{value}</div>
    </div>
  )
}
