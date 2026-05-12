import { defineConfig } from "vitest/config";
import { svelte } from '@sveltejs/vite-plugin-svelte';

import { viteStaticCopy } from 'vite-plugin-static-copy';

const cesiumSource = "node_modules/cesium/Build/Cesium";
const cesiumBaseUrl = "cesiumStatic";

const cesiumTargets = ['Assets', 'ThirdParty', 'Widgets', 'Workers'].map(dir => ({
  src: `${cesiumSource}/${dir}`,
  dest: cesiumBaseUrl
}));

// https://vite.dev/config/
export default defineConfig({
  // TODO Do I need to exclude "*.test.ts" files from bundle via a "build" property?
  base: './',  // see https://github.com/vitejs/vite/discussions/5081
  define: {  // from https://community.cesium.com/t/is-there-a-good-way-to-use-cesium-with-vite/27545/18
    CESIUM_BASE_URL: JSON.stringify(cesiumBaseUrl),
  },
  plugins: [
    svelte(),
    viteStaticCopy({ targets: cesiumTargets }),
  ],
  test: {
    dir: './test/',
    reporters: ['tree'],
  }
})
