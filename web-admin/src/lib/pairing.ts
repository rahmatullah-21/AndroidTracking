import { Timestamp, doc, serverTimestamp, setDoc } from 'firebase/firestore'
import { USE_MOCK, getDb } from './firebase'

// Unambiguous alphabet (no 0/O/1/I) for codes the user has to type on a phone.
const ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
const CODE_LENGTH = 8
const TTL_MS = 15 * 60 * 1000

function generateCode(): string {
  const bytes = new Uint32Array(CODE_LENGTH)
  crypto.getRandomValues(bytes)
  let out = ''
  for (let i = 0; i < CODE_LENGTH; i++) out += ALPHABET[bytes[i] % ALPHABET.length]
  return out
}

export interface PairingCode {
  code: string
  expiresAt: Date
}

/**
 * Creates a short-lived pairing code owned by [ownerUid]. The device reads this code by its exact
 * id, then claims itself under that owner (validated by Firestore rules). In demo mode it just
 * returns a sample code without touching Firestore.
 */
export async function createPairingCode(ownerUid: string): Promise<PairingCode> {
  const code = generateCode()
  const expiresAt = new Date(Date.now() + TTL_MS)
  if (USE_MOCK) return { code: `DEMO${code.slice(0, 4)}`, expiresAt }
  await setDoc(doc(getDb(), 'pairingCodes', code), {
    ownerUid,
    createdAt: serverTimestamp(),
    expiresAt: Timestamp.fromDate(expiresAt),
  })
  return { code, expiresAt }
}
