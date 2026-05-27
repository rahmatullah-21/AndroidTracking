import {
  createContext, useContext, useEffect, useMemo, useState, type ReactNode,
} from 'react'
import {
  onAuthStateChanged, signInWithEmailAndPassword, signOut,
} from 'firebase/auth'
import { USE_MOCK, getAuthInstance } from '../lib/firebase'

interface AuthUser {
  uid: string
  email: string | null
}

interface AuthContextValue {
  user: AuthUser | null
  loading: boolean
  signIn: (email: string, password: string) => Promise<void>
  signOutUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const DEMO_USER: AuthUser = { uid: 'demo-owner', email: 'demo@deviceinsight.local' }

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(USE_MOCK ? DEMO_USER : null)
  const [loading, setLoading] = useState(!USE_MOCK)

  useEffect(() => {
    if (USE_MOCK) return
    try {
      const unsub = onAuthStateChanged(getAuthInstance(), (fbUser) => {
        setUser(fbUser ? { uid: fbUser.uid, email: fbUser.email } : null)
        setLoading(false)
      })
      return unsub
    } catch (err) {
      // Misconfigured Firebase — surface on the login screen instead of crashing.
      console.error('Firebase auth init failed:', err)
      setLoading(false)
    }
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    user,
    loading,
    signIn: async (email, password) => {
      if (USE_MOCK) { setUser(DEMO_USER); return }
      await signInWithEmailAndPassword(getAuthInstance(), email, password)
    },
    signOutUser: async () => {
      if (USE_MOCK) { setUser(null); return }
      await signOut(getAuthInstance())
    },
  }), [user, loading])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
