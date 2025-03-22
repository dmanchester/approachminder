import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

import { viteStaticCopy } from 'vite-plugin-static-copy'
import { viteExternalsPlugin } from 'vite-plugin-externals'

// Contents of this file based largely on https://github.com/s3xysteak/simply-cesium-vite-vue.
//
// See also: https://vitejs.dev/config/.

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
  base: './',  // see https://github.com/vitejs/vite/discussions/5081
  plugins: [
    svelte(),
    copyCesium(['Assets', 'ThirdParty', 'Widgets', 'Workers']),
    viteExternalsPlugin(
        { cesium: 'Cesium' },
        {
          disableInServe: true
        }
    ),
    {
      transformIndexHtml: () => [
        {
          tag: 'script',
          attrs: { src: `libs/cesium/Cesium.js` }
        }
      ]
    }
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
