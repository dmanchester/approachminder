import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from "vitest/config";
import { type Plugin } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';

import { type Target, viteStaticCopy } from 'vite-plugin-static-copy';
import { viteExternalsPlugin } from 'vite-plugin-externals';

// See also: https://vitejs.dev/config/.

function cesiumTargets(): Array<Target> {

  const targets: Array<Target> = [];

  ['Assets', 'ThirdParty', 'Widgets', 'Workers'].forEach(dir => {
    targets.push({
      src: `node_modules/cesium/Build/Cesium/${dir}/*`,
      dest: `libs/cesium/${dir}/`
    });
  });

  targets.push({
    src: 'node_modules/cesium/Build/Cesium/Cesium.js',
    dest: 'libs/cesium/'
  });

  return targets;
}

const scriptTagTransformer = () => [ {
  tag: 'script',
  attrs: { src: 'libs/cesium/Cesium.js' }
}];

// TODO Is scriptTagTransformerPlugin() needed? When it's uncommented in defineConfig(), it drives a line like the
// following into the top of index.html's <head> element (when running in dev mode):
//
//   <script src="libs/cesium/Cesium.js"></script>
//
// However, there seems to be no negative impact from having it commented out.
//
// If it isn't needed, remove it. And if it's removed, the above Target to copy Cesium.js can likely be removed, too.
//
// @ts-ignore
function scriptTagTransformerPlugin(): Plugin {
  return {
    name: 'scriptTagTransformerPlugin',
    transformIndexHtml: scriptTagTransformer
  }
}

export default defineConfig({
  base: './',  // see https://github.com/vitejs/vite/discussions/5081
  // TODO Do I need to add "build" and exclude "*.test.ts" files from bundle?
  define: {  // from https://community.cesium.com/t/is-there-a-good-way-to-use-cesium-with-vite/27545/18
    CESIUM_BASE_URL: JSON.stringify('./libs/cesium'),
  },
  plugins: [
    svelte(),
    viteStaticCopy({ targets: cesiumTargets() }),
    viteExternalsPlugin(
      { cesium: 'Cesium' },
      { disableInServe: true }
    ),
    // scriptTagTransformerPlugin(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  test: {}
})
