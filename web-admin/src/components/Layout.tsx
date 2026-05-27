import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { USE_MOCK } from '../lib/firebase'

const linkBase =
  'block rounded-lg px-3 py-2 text-sm font-medium transition-colors'

export default function Layout() {
  const { user, signOutUser } = useAuth()

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 flex-col bg-slate-900 px-4 py-6 text-slate-200">
        <div className="mb-8 px-2">
          <div className="text-lg font-bold text-white">Device Insight</div>
          <div className="text-xs text-slate-400">Admin Console</div>
        </div>
        <nav className="flex-1 space-y-1">
          <NavLink to="/" end className={({ isActive }) =>
            `${linkBase} ${isActive ? 'bg-brand text-white' : 'text-slate-300 hover:bg-slate-800'}`}>
            Overview
          </NavLink>
          <NavLink to="/devices" className={({ isActive }) =>
            `${linkBase} ${isActive ? 'bg-brand text-white' : 'text-slate-300 hover:bg-slate-800'}`}>
            Devices
          </NavLink>
        </nav>
        <div className="mt-4 border-t border-slate-700 pt-4 text-xs text-slate-400">
          <div className="truncate">{user?.email}</div>
          <button onClick={signOutUser} className="mt-2 text-slate-300 hover:text-white">
            Sign out
          </button>
        </div>
      </aside>

      <main className="flex-1 overflow-y-auto">
        {USE_MOCK && (
          <div className="bg-amber-100 px-6 py-2 text-center text-sm text-amber-800">
            Demo mode — showing bundled sample data. Set <code>VITE_USE_MOCK=false</code> and
            configure Firebase to monitor real devices.
          </div>
        )}
        <div className="mx-auto max-w-6xl px-6 py-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
