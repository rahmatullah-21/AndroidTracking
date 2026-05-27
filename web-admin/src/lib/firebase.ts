import { initializeApp, type FirebaseApp } from 'firebase/app'
import { getAuth, type Auth } from 'firebase/auth'
import { getFirestore, type Firestore } from 'firebase/firestore'

const apiKey = import.meta.env.VITE_FIREBASE_API_KEY

/**
 * When true, the app runs entirely on bundled demo data — no Firebase project required.
 * Demo mode is the default: it is only disabled when a real Firebase API key is provided
 * AND VITE_USE_MOCK is not explicitly "true". This makes `npm run dev` work out of the box.
 */
export const USE_MOCK =
  import.meta.env.VITE_USE_MOCK === 'true' || !apiKey

const firebaseConfig = {
  apiKey,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

let app: FirebaseApp | null = null

function ensureApp(): FirebaseApp {
  if (USE_MOCK) throw new Error('Firebase is disabled in mock mode')
  if (!app) app = initializeApp(firebaseConfig)
  return app
}

export const getAuthInstance = (): Auth => getAuth(ensureApp())
export const getDb = (): Firestore => getFirestore(ensureApp())
