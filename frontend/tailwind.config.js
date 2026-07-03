/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        grotesk: ['"Schibsted Grotesk"', 'sans-serif'],
        inter: ['Inter', 'sans-serif'],
        noto: ['"Noto Sans"', 'sans-serif'],
        fustat: ['Fustat', 'sans-serif'],
      },
    },
  },
  plugins: [],
}

