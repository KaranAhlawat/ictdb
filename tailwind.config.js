import daisyui from "daisyui";

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./ui/public/*.{html,css,js}",
    "./ui/src/io/karan/ictdb/**/*.scala"
  ],
  theme: {
    extend: {},
  },
  plugins: [
    daisyui
  ],
}

