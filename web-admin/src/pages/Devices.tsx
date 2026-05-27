import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Spinner from '../components/Spinner'
import { useAuth } from '../context/AuthContext'
import { listDevices } from '../lib/dataService'
import { createPairingCode, type PairingCode } from '../lib/pairing'
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

function PairingModal({ ownerUid, onClose }: { ownerUid: string; onClose: () => void }) {
  const [code, setCode] = useState<PairingCode | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    createPairingCode(ownerUid)
      .then(setCode)
      .catch((e) => setError(e instanceof Error ? e.message : 'Failed to create code'))
  }, [ownerUid])

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/50 px-4" onClick={onClose}>
      <div
        className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-lg font-bold text-slate-900">Link a new device</h2>
        <p className="mt-1 text-sm text-slate-500">
          On the device, open <b>Device Insight Pro → Settings → Cloud sync → Link device</b>,
          confirm consent, and enter this code:
        </p>

        {error ? (
          <div className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</div>
        ) : !code ? (
          <Spinner label="Generating code…" />
        ) : (
          <>
            <div className="mt-5 flex items-center justify-center gap-3">
              <span className="select-all rounded-lg bg-slate-100 px-5 py-3 font-mono text-2xl font-bold tracking-widest text-slate-900">
                {code.code}
              </span>
              <button
                onClick={() => {
                  navigator.clipboard?.writeText(code.code)
                  setCopied(true)
                  setTimeout(() => setCopied(false), 1500)
                }}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
              >
                {copied ? 'Copied' : 'Copy'}
              </button>
            </div>
            <p className="mt-3 text-center text-xs text-slate-400">
              Expires at {code.expiresAt.toLocaleTimeString()}. The device must consent to monitoring
              before it appears here.
            </p>
          </>
        )}

        <button
          onClick={onClose}
          className="mt-6 w-full rounded-lg bg-slate-900 py-2 text-sm font-semibold text-white hover:bg-slate-700"
        >
          Done
        </button>
      </div>
    </div>
  )
}
