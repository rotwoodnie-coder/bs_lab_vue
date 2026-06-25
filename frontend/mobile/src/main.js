import { createApp } from 'vue'

import { createPinia } from 'pinia'

import './styles/fonts.css'

import './styles/tokens.css'

import './styles/base.css'

import './styles/components.css'

import './styles/layout.css'

import './styles/layout-pad.css'

import './styles/layout-pad-pages.css'

import './styles/layout-adaptive.css'

import './styles/home-elevated.css'

import './styles/bind-register-flow.css'

import './styles/legacy.css'

import App from './App.vue'

import router from './router'

import { nextTick } from 'vue'

import { initLucideIcons } from './utils/lucideIcons'

import { bootstrapAuthSession } from './bootstrap/auth'
import { useProfileStore } from './stores/profile'
import { isLoggedIn } from './utils/authStorage'



async function startApp() {

  const app = createApp(App)

  const pinia = createPinia()

  app.use(pinia)

  await bootstrapAuthSession()

  if (isLoggedIn()) {
    try {
      useProfileStore().loadProfile(true)
    } catch {
      // ignore preload errors
    }
  }

  app.use(router)

  router.afterEach(async () => {
    await nextTick()
    initLucideIcons()
  })

  app.mount('#app')

}



startApp()

