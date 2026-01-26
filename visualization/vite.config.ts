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
  // TODO After upgrading Vite, try again to set CESIUM_BASE_URL via "define". (See below.)
  //
  // This approach to to setting CESIUM_BASE_URL has not seemed to work with Vite v4.5.9. "npm run build" gets an error
  // like this one:
  //
  // [commonjs--resolver] Unexpected token (66:97) in /home/dan/IdeaUltProjects/approachminder/visualization/node_modules/@cesium/engine/Source/Core/buildModuleUrl.js
  // file: /home/dan/IdeaUltProjects/approachminder/visualization/node_modules/@cesium/engine/Source/Core/buildModuleUrl.js:66:97
  // 64:   if (!defined(baseUrlString)) {
  // 65:     throw new DeveloperError(
  // 66:       "Unable to determine Cesium base URL automatically, try defining a global variable called "cesiumStatic".",
  //                                                                                                      ^
  // 67:     );
  // 68:   }
  //
  // Bizarrely, the source code quoted above ("try defining a global variable...") happens to reflect replacement of
  // "CESIUM_BASE_URL" with "cesiumStatic".
  //
  // define: {  // from https://community.cesium.com/t/is-there-a-good-way-to-use-cesium-with-vite/27545/18
  //   CESIUM_BASE_URL: JSON.stringify(cesiumBaseUrl),
  // },
  plugins: [
    svelte(),
    viteStaticCopy({ targets: cesiumTargets }),
  ],
  test: {}
})
