# Developer Notes

## One-Time Setup

1. Create a [Sketchfab](https://sketchfab.com/) account (unless you already have one).

2. Navigate to [B737-800 Model](https://sketchfab.com/3d-models/b737-800-model-6cbe380405794ea69d6b0a3d144dd1af).

3. Download the model in glTF format.

4. Create a [Cesium ion](https://ion.cesium.com/) account (unless you already have one).

5. In Cesium ion:

   1. Under Access Tokens, obtain a [token](https://cesium.com/learn/ion/cesium-ion-access-tokens/).

   2. Under My Assets:
      1. Click "Add data" > "Add files...".
      2. Upload the file downloaded from Sketchfab in step 3.
      3. On the Add Data screen, for "What kind of data is this?", choose "3D Model (convert to glTF)".
      4. Click Upload.
      5. Note the ID of the asset.

6. Under `visualization/`, create a file named `approachminder-config.json`.

7. Populate the file with the following JSON, _substituting the values from steps 5.i and 5.ii:_

   ```json
   {
     "cesiumIon": {
       "accessToken": "ABCDEFGH",
       "assetIdAirplane": 12345678
     }
   }
   ```

   _**Important:** The access token is a sensitive value and should **not** be committed to version control. `approachminder-config.json` is listed in `.gitignore`, which should help prevent accidentally committing it._

## Starting in Development Mode

### Accept Local Connections Only

```bash
npm run dev
```

### Accept Local and Remote Connections

```bash
npm run dev -- --host
```

## Creating a Distribution

```bash
npm run build
```

The distribution is created in `dist/`.

The above command runs with a larger-than-default heap, setting `--max-old-space-size` to 4096 MB. This is to avoid `JavaScript heap out of memory` errors like the following:

```
FATAL ERROR: Ineffective mark-compacts near heap limit Allocation failed - JavaScript heap out of memory
```

For background information, please see [this Vite issue](https://github.com/vitejs/vite/issues/2433) and [this documentation from Rollup](https://rollupjs.org/troubleshooting/#error-javascript-heap-out-of-memory) (used by Vite).