import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import os from 'node:os'
import path from 'node:path'

function resolveLanIp() {
  const nets = os.networkInterfaces()
  const candidates = []
  for (const ifaces of Object.values(nets)) {
    for (const iface of ifaces || []) {
      if (iface.family !== 'IPv4' || iface.internal) continue
      if (iface.address.startsWith('169.254.')) continue
      candidates.push(iface.address)
    }
  }
  return (
    candidates.find((ip) => ip.startsWith('10.')) ||
    candidates.find((ip) => ip.startsWith('192.168.')) ||
    candidates[0]
  )
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const lanIp = resolveLanIp()
  const port = 5174
  const base = env.VITE_BASE || '/'

  return {
    base,
    plugins: [
      vue(),
      {
        name: 'lan-access-hint',
        configureServer(server) {
          server.httpServer?.once('listening', () => {
            if (!lanIp) return
            console.log('\n  📱 局域网访问方式（单端口方案，推荐）：')
            console.log('     1. 执行 pnpm build:lan')
            console.log('     2. 重启后端')
            console.log(`     3. 手机打开 http://${lanIp}:8010/m/#/login\n`)
            console.log('  📱 Vite 开发模式（需 5174 端口可达）：')
            console.log(`     http://${lanIp}:${port}/#/login\n`)
          })
        }
      }
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src')
      }
    },
    server: {
      host: '0.0.0.0',
      port,
      strictPort: true,
      allowedHosts: true,
      cors: true,
      // 关闭 HMR，避免手机连 localhost WebSocket 导致白屏
      hmr: false,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8010',
          changeOrigin: true
        },
        '/v1': {
          target: 'http://127.0.0.1:5001',
          changeOrigin: true
        }
      }
    },
    preview: {
      host: '0.0.0.0',
      port,
      allowedHosts: true,
      cors: true
    }
  }
})
