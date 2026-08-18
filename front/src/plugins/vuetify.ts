import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

import { createVuetify } from 'vuetify'

export default createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#101B2D',
          secondary: '#F4C561',
          background: '#FAF8F4',
          success: '#2E7D32',
          error: '#C62828',
        },
      },
    },
  },
  defaults: {
    VTextField: { variant: 'outlined', density: 'comfortable' },
    VBtn: { style: 'text-transform: none;' },
  },
})
