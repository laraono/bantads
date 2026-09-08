import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'

export default createVuetify({
    components,
    directives,

    icons: {
        defaultSet: 'mdi',
        aliases,
        sets: {
            mdi,
        },
    },
    
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