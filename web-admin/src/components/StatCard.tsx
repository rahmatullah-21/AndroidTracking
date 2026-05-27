interface Props {
  title: string
  value: string
  subtitle?: string
  accent?: string
}

export default function StatCard({ title, value, subtitle, accent }: Props) {
  return (
    <div className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
      <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{title}</div>
      <div className="mt-1 text-2xl font-bold" style={accent ? { color: accent } : undefined}>
        {value}
      </div>
      {subtitle && <div className="text-xs text-slate-400">{subtitle}</div>}
    </div>
  )
}
