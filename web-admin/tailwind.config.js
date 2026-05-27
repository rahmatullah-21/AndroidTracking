/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#1565C0',
          light: '#42a5f5',
          dark: '#0d47a1',
        },
      },
    },
  },
  plugins: [],
}
