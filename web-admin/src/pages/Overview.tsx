import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts'
import PairingModal from '../components/PairingModal'
import Spinner from '../components/Spinner'
import StatCard from '../components/StatCard'
import { useAuth } from '../context/AuthContext'
import { listDevices } from '../lib/dataService'
import { formatDuration, relativeTime, scoreColor } from '../lib/format'
import type { Device } from '../lib/types'

export default function Overview() {
  const { user } = useAuth()
  const [devices, setDevices] = useState<Device[] | null>(null)
  const [pairing, setPairing] = useState(false)

  useEffect(() => {
    listDevices(user!.uid).then(setDevices).catch(() => setDevices([]))
  }, [user])

  if (!devices) return <Spinner label="Loading devices…" />

  const totalScreen = devices.reduce((a, d) => a + d.summary.screenTimeMsToday, 0)
  const totalMsgs = devices.reduce((a, d) => a + d.summary.messagesToday, 0)
  const avgSec = devices.length
    ? Math.round(devices.reduce((a, d) => a + d.summary.securityScore, 0) / devices.length)
    : 0
  const chartData = devices.map((d) => ({
    name: d.label.length > 14 ? d.label.slice(0, 13) + '…' : d.label,
    hours: +(d.summary.screenTimeMsToday / 3_600_000).toFixed(1),
  }))

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Overview</h1>
        <button
          onClick={() => setPairing(true)}
          className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-dark"
        >
          + Add device
        </button>
      </div>

      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <StatCard title="Devices" value={String(devices.length)} />
        <StatCard title="Screen time today" value={formatDuration(totalScreen)} subtitle="across all devices" />
        <StatCard title="Messages today" value={String(totalMsgs)} />
        <StatCard title="Avg security score" value={`${avgSec}/100`} accent={scoreColor(avgSec)} />
      </div>

      <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
        <h2 className="mb-4 text-sm font-semibold text-slate-700">Screen time today by device (hours)</h2>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={chartData}>
            <XAxis dataKey="name" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} />
            <Tooltip />
            <Bar dataKey="hours" fill="#1565C0" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="rounded-xl bg-white shadow-sm ring-1 ring-slate-200">
        <h2 className="border-b border-slate-100 px-5 py-3 text-sm font-semibold text-slate-700">Devices</h2>
        <ul className="divide-y divide-slate-100">
          {devices.map((d) => (
            <li key={d.deviceId}>
              <Link to={`/devices/${d.deviceId}`} className="flex items-center justify-between px-5 py-3 hover:bg-slate-50">
                <div>
                  <div className="font-medium text-slate-900">{d.label}</div>
                  <div className="text-xs text-slate-400">
                    Last seen {d.lastSeen ? relativeTime(d.lastSeen) : 'unknown'}
                  </div>
                </div>
                <div className="flex items-center gap-6 text-sm text-slate-600">
                  <span>{formatDuration(d.summary.screenTimeMsToday)}</span>
                  <span>{d.summary.messagesToday} msgs</span>
                  <span className="font-semibold" style={{ color: scoreColor(d.summary.securityScore) }}>
                    {d.summary.securityScore}
                  </span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      </div>

      {pairing && <PairingModal ownerUid={user!.uid} onClose={() => setPairing(false)} />}
    </div>
  )
}
