import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

import { createHtmlPlugin } from 'vite-plugin-html'
import { viteStaticCopy } from 'vite-plugin-static-copy'
import { viteExternalsPlugin } from 'vite-plugin-externals'

// https://vitejs.dev/config/

const copyCesium = items =>
    viteStaticCopy({
      targets: [
        ...items.map(item => ({
          src: `node_modules/cesium/Build/Cesium/${item}/*`,
          dest: `libs/cesium/${item}/`
        })),
        {
          src: 'node_modules/cesium/Build/Cesium/Cesium.js',
          dest: 'libs/cesium/'
        }
      ]
    })

export default defineConfig({
  plugins: [
    svelte(),
    createHtmlPlugin({
      minify: true,
      inject: {
        data: {
          cesiumInjectScript: `<script src="libs/cesium/Cesium.js"></script>`
        }
      }
    }),
    copyCesium(['Assets', 'ThirdParty', 'Widgets', 'Workers']),
    viteExternalsPlugin(
        { cesium: 'Cesium' },
        {
          disableInServe: true
        }
    )
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})