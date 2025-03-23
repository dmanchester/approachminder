import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

import { viteStaticCopy } from 'vite-plugin-static-copy'
import { viteExternalsPlugin } from 'vite-plugin-externals'

// See also: https://vitejs.dev/config/.

// FIXME The configuration below of "base: './'" notwithstanding, some references to "libs"-based assets are still using
// absolute URLs, as opposed to relative ones (i.e., "/libs/..." as opposed to "./libs/...").
//
// When a production build is deployed to a non-root directory on dmanchester.com--for example, to
// https://www.dmanchester.com/am/2022-12-d/--this use of absolute URLs leads to 404 errors on various assets, including:
//
//     "InfoBoxDescription.css"
//     various "tycho*.jpg" files
//     "IAU2006_XYS_17.json"
//
// This culminates in a UI error:
//
//     "An error occurred while rendering. Rendering has stopped."
//
// The 404s are due to the assets being requested from:
//
//     https://www.dmanchester.com/libs/...
//
// As opposed to:
//
//     https://www.dmanchester.com/am/2022-12-d/libs/...
//
// WORKAROUND (from Bash on dmanchester.com):
//
//     cd /var/www/html
//     ln -s am/2022-12-d/libs/
//
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
