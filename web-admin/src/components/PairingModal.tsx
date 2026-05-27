import { useEffect, useState } from 'react'
import Spinner from './Spinner'
import { createPairingCode, deletePairingCode, type PairingCode } from '../lib/pairing'

/** Generates a pairing code for [ownerUid] and shows it for the device user to enter. */
export default function PairingModal({ ownerUid, onClose }: { ownerUid: string; onClose: () => void }) {
  const [code, setCode] = useState<PairingCode | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(true)
  const [copied, setCopied] = useState(false)

  // Generate a fresh code; if `previous` is given, revoke it first.
  async function generate(previous?: PairingCode | null) {
    setBusy(true)
    setError(null)
    setCopied(false)
    try {
      if (previous) await deletePairingCode(previous.code).catch(() => {})
      setCode(await createPairingCode(ownerUid))
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create code')
    } finally {
      setBusy(false)
    }
  }

  useEffect(() => {
    void generate()
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
                disabled={busy}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50 disabled:opacity-50"
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

        <div className="mt-6 flex gap-3">
          <button
            onClick={() => generate(code)}
            disabled={busy}
            className="flex-1 rounded-lg border border-slate-300 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50"
          >
            {busy ? 'Regenerating…' : '↻ Regenerate'}
          </button>
          <button
            onClick={onClose}
            className="flex-1 rounded-lg bg-slate-900 py-2 text-sm font-semibold text-white hover:bg-slate-700"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  )
}
