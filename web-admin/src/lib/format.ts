import type { Severity } from './types'

export function formatDuration(ms: number): string {
  if (!ms || ms <= 0) return '0m'
  const totalMin = Math.floor(ms / 60000)
  const h = Math.floor(totalMin / 60)
  const m = totalMin % 60
  if (h > 0 && m > 0) return `${h}h ${m}m`
  if (h > 0) return `${h}h`
  return `${m}m`
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  const units = ['KB', 'MB', 'GB', 'TB']
  let size = bytes / 1024
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(1)} ${units[i]}`
}

export function relativeTime(ts: number): string {
  const delta = Date.now() - ts
  const min = Math.floor(delta / 60000)
  if (min < 1) return 'just now'
  if (min < 60) return `${min}m ago`
  if (min < 1440) return `${Math.floor(min / 60)}h ago`
  return `${Math.floor(min / 1440)}d ago`
}

export function clockTime(ts: number): string {
  return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export function severityColor(s: Severity): string {
  switch (s) {
    case 'CRITICAL': return '#d32f2f'
    case 'HIGH': return '#f57c00'
    case 'MEDIUM': return '#fbc02d'
    case 'LOW': return '#7cb342'
    default: return '#42a5f5'
  }
}

export function scoreColor(score: number): string {
  if (score >= 80) return '#7cb342'
  if (score >= 60) return '#fbc02d'
  if (score >= 40) return '#f57c00'
  return '#d32f2f'
}

export const CHART_PALETTE = [
  '#1565C0', '#00897B', '#6A1B9A', '#EF6C00',
  '#C2185B', '#43A047', '#5E35B1', '#00ACC1',
]
