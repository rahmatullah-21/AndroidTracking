# Device Insight — Web Admin Panel

A React + TypeScript + TailwindCSS + Recharts dashboard to monitor devices running the
**Device Insight Pro** Android app. It reads the per-device analytics that each device syncs to
**Cloud Firestore** (opt-in) and lets an authorized owner review usage, messages, the activity
timeline, and security posture across all of their devices.

## Quick start (demo mode — no backend)

```bash
cd web-admin
npm install
cp .env.example .env.local        # VITE_USE_MOCK=true is already set
npm run dev                       # http://localhost:5173 → "Enter demo"
```

Demo mode runs entirely on bundled sample data so you can explore the UI without Firebase.

## Connecting to real devices (Firebase mode)

1. In `.env.local` set `VITE_USE_MOCK=false` and fill in your Firebase web config.
2. Enable **Email/Password** auth and **Cloud Firestore** in the Firebase console.
3. Apply the Firestore security rules from [`../docs/firebase/FIRESTORE_SCHEMA.md`](../docs/firebase/FIRESTORE_SCHEMA.md).
4. On the Android side, enable the cloud-sync module (see [`../docs/firebase/`](../docs/firebase/))
   and sign each device into the **same owner account**. The device must have cloud sync turned on.
5. `npm run build` → deploy `dist/` to Firebase Hosting / Vercel / Netlify.

The panel only ever queries `devices` where `ownerUid == yourUid`, so you can only see devices
linked to your own account.

## Structure

```
src/
├── lib/         firebase init, types, format helpers, mock data, dataService (mock|firestore)
├── context/     AuthContext (Firebase Auth, demo-aware)
├── components/  Layout, StatCard, Spinner
└── pages/       Login, Overview, Devices, DeviceDetail
```

## Lawful use

This console is for monitoring **devices you own or are explicitly authorized to manage** (a child's
device, or a company device under a disclosed policy). The monitored device always shows that
monitoring is active (a persistent notification, visible app, and user-granted permissions). Do not
use it to monitor anyone without their knowledge and consent — see the project privacy policy and
your local laws.
