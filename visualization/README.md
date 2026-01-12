# Developer Notes

## Start in Development Mode

### Accept Local Connections Only

```bash
npm run dev
```

### Accept Local and Remote Connections

```bash
npm run dev -- --host
```

## Create a Distribution

```bash
npm run build
```

The distribution is created in `dist/`.

The above command runs with a larger-than-default heap, setting `--max-old-space-size` to 4096 MB. This is to avoid `JavaScript heap out of memory` errors like the following:

```
FATAL ERROR: Ineffective mark-compacts near heap limit Allocation failed - JavaScript heap out of memory
```

For background information, please see [this Vite issue](https://github.com/vitejs/vite/issues/2433) and [this documentation from Rollup](https://rollupjs.org/troubleshooting/#error-javascript-heap-out-of-memory) (used by Vite).