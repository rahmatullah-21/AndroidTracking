import { useState, type FormEvent } from 'react'
import { useAuth } from '../context/AuthContext'
import { USE_MOCK } from '../lib/firebase'

export default function Login() {
  const { signIn } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await signIn(email, password)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Sign-in failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="grid min-h-screen place-items-center bg-slate-900 px-4">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-xl">
        <h1 className="text-xl font-bold text-slate-900">Device Insight Admin</h1>
        <p className="mt-1 text-sm text-slate-500">
          Sign in to monitor devices linked to your account.
        </p>

        <form onSubmit={onSubmit} className="mt-6 space-y-4">
          <input
            type="email" required placeholder="Email"
            value={email} onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-brand"
          />
          <input
            type="password" required placeholder="Password"
            value={password} onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-brand"
          />
          {error && <div className="text-sm text-red-600">{error}</div>}
          <button
            type="submit" disabled={busy}
            className="w-full rounded-lg bg-brand py-2 text-sm font-semibold text-white hover:bg-brand-dark disabled:opacity-60"
          >
            {busy ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        {USE_MOCK && (
          <button
            onClick={() => signIn('demo@deviceinsight.local', 'demo')}
            className="mt-4 w-full rounded-lg border border-brand py-2 text-sm font-semibold text-brand hover:bg-brand/5"
          >
            Enter demo (sample data)
          </button>
        )}
      </div>
    </div>
  )
}
