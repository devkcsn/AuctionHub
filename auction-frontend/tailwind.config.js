/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        gray: {
          50:  '#fafafa',
          100: '#f4f4f5',
          200: '#e4e4e7',
          300: '#d4d4d8',
          400: '#a1a1aa',
          500: '#71717a',
          600: '#52525b',
          700: '#3f3f46',
          800: '#27272a',
          900: '#18181b',
          950: '#09090b',
        },
        primary: {
          50:  '#effefa',
          100: '#c7fef0',
          200: '#90fce2',
          300: '#73eedc',
          400: '#2ac5b0',
          500: '#12a594',
          600: '#098578',
          700: '#0b6c63',
          800: '#0d5650',
          900: '#0f4843',
          950: '#032c29',
        },
        accent: {
          50:  '#fef2f4',
          100: '#fde6eb',
          200: '#facfda',
          300: '#f6a8bc',
          400: '#f07798',
          500: '#e4496f',
          600: '#d02959',
          700: '#ae1c47',
          800: '#5f1a37',
          900: '#4a1230',
          950: '#2c0618',
        },
        brand: {
          turquoise: '#73eedc',
          pearl:     '#73c2be',
          lavender:  '#776885',
          crimson:   '#5f1a37',
          ink:       '#04030f',
        },
      },
      fontFamily: {
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
    },
  },
  plugins: [],
};