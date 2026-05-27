import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts'
import Spinner from '../components/Spinner'
import StatCard from '../components/StatCard'
import {
  CHART_PALETTE, clockTime, formatDuration, relativeTime, scoreColor, severityColor,
} from '../lib/format'
import {
  getDevice, getEvents, getMessages, getSecurity, getUsageDays,
} from '../lib/dataService'
import type {
  Device, DeviceEvent, SecurityReport, SocialMessage, UsageDay,
} from '../lib/types'

export default function DeviceDetail() {
  const { deviceId = '' } = useParams()
  const [device, setDevice] = useState<Device | null>(null)
  const [usage, setUsage] = useState<UsageDay[]>([])
  const [messages, setMessages] = useState<SocialMessage[]>([])
  const [events, setEvents] = useState<DeviceEvent[]>([])
  const [security, setSecurity] = useState<SecurityReport | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    setLoading(true)
    Promise.all([
      getDevice(deviceId), getUsageDays(deviceId), getMessages(deviceId),
      getEvents(deviceId), getSecurity(deviceId),
    ]).then(([d, u, m, e, s]) => {
      if (!active) return
      setDevice(d); setUsage(u); setMessages(m); setEvents(e); setSecurity(s)
      setLoading(false)
    })
    return () => { active = false }
  }, [deviceId])

  if (loading) return <Spinner label="Loading device…" />
  if (!device) return <div className="text-slate-500">Device not found.</div>

  const s = device.summary
  const usageChart = usage.map((d) => ({
    day: new Date(d.epochDay * 86_400_000).toLocaleDateString([], { weekday: 'short' }),
    hours: +(d.totalForegroundMs / 3_600_000).toFixed(1),
  }))
  const topApps = [...(usage[usage.length - 1]?.apps ?? [])]
    .sort((a, b) => b.totalForegroundMs - a.totalForegroundMs)
    .slice(0, 6)

  const platformCounts = Object.entries(
    messages.reduce<Record<string, number>>((acc, m) => {
      acc[m.platform] = (acc[m.platform] ?? 0) + 1
      return acc
    }, {}),
  ).map(([name, value]) => ({ name, value }))

  return (
    <div className="space-y-6">
      <div>
        <Link to="/devices" className="text-sm text-brand hover:underline">← All devices</Link>
        <h1 className="mt-1 text-2xl font-bold text-slate-900">{device.label}</h1>
        <p className="text-sm text-slate-400">
          {device.manufacturer} {device.model} • last seen{' '}
          {device.lastSeen ? relativeTime(device.lastSeen) : 'unknown'}
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <StatCard title="Screen time" value={formatDuration(s.screenTimeMsToday)} />
        <StatCard title="Unlocks" value={String(s.unlocksToday)} />
        <StatCard title="Messages today" value={String(s.messagesToday)} />
        <StatCard title="Security" value={`${s.securityScore}/100`} accent={scoreColor(s.securityScore)} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Panel title="Daily usage (hours)">
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={usageChart}>
              <XAxis dataKey="day" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="hours" fill="#1565C0" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Panel>

        <Panel title="Messages by platform">
          {platformCounts.length === 0 ? (
            <Empty>No messages captured.</Empty>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={platformCounts} dataKey="value" nameKey="name" outerRadius={80} label>
                  {platformCounts.map((_, i) => (
                    <Cell key={i} fill={CHART_PALETTE[i % CHART_PALETTE.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          )}
        </Panel>
      </div>

      <Panel title="Top apps">
        {topApps.length === 0 ? <Empty>No usage data.</Empty> : (
          <ul className="space-y-2">
            {topApps.map((a) => (
              <li key={a.packageName} className="flex items-center justify-between text-sm">
                <span className="text-slate-800">{a.appName}</span>
                <span className="text-slate-500">{formatDuration(a.totalForegroundMs)} • {a.launchCount} launches</span>
              </li>
            ))}
          </ul>
        )}
      </Panel>

      <Panel title={`Messages (${messages.length})`}>
        {messages.length === 0 ? <Empty>No messages captured.</Empty> : (
          <ul className="max-h-96 space-y-2 overflow-y-auto">
            {messages.map((m) => (
              <li key={m.id} className="rounded-lg bg-slate-50 px-3 py-2">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-slate-800">{m.sender}</span>
                  <span className="text-xs text-slate-400">{relativeTime(m.timestamp)}</span>
                </div>
                <div className="text-sm text-slate-600">{m.preview}</div>
                <div className="mt-1 text-xs text-brand">{m.platform}{m.isGroup ? ' • group' : ''}</div>
              </li>
            ))}
          </ul>
        )}
      </Panel>

      <div className="grid gap-6 lg:grid-cols-2">
        <Panel title="Activity timeline">
          {events.length === 0 ? <Empty>No events recorded.</Empty> : (
            <ul className="max-h-80 space-y-1 overflow-y-auto text-sm">
              {events.map((e) => (
                <li key={e.id} className="flex justify-between border-b border-slate-50 py-1">
                  <span className="text-slate-700">{e.label}</span>
                  <span className="text-xs text-slate-400">{clockTime(e.timestamp)}</span>
                </li>
              ))}
            </ul>
          )}
        </Panel>

        <Panel title={security ? `Security (score ${security.score})` : 'Security'}>
          {!security ? <Empty>No scan available.</Empty> : (
            <ul className="space-y-2">
              {security.findings.map((f) => (
                <li key={f.id} className="rounded-lg bg-slate-50 px-3 py-2">
                  <div className="flex items-center gap-2">
                    <span className="h-2 w-2 rounded-full" style={{ backgroundColor: severityColor(f.severity) }} />
                    <span className="font-medium text-slate-800">{f.title}</span>
                  </div>
                  <div className="text-xs text-slate-500">{f.recommendation}</div>
                </li>
              ))}
            </ul>
          )}
        </Panel>
      </div>
    </div>
  )
}

function Panel({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <h2 className="mb-4 text-sm font-semibold text-slate-700">{title}</h2>
      {children}
    </div>
  )
}

function Empty({ children }: { children: React.ReactNode }) {
  return <div className="py-6 text-center text-sm text-slate-400">{children}</div>
}
