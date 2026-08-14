import js from "@eslint/js";
import globals from "globals";
import tseslint from "typescript-eslint";
import json from "@eslint/json";
import markdown from "@eslint/markdown";
import css from "@eslint/css";
import svelte from "eslint-plugin-svelte";
import { defineConfig, globalIgnores } from "eslint/config";
import eslintConfigPrettier from "eslint-config-prettier/flat";

const DOT_JSON_FILES_WITH_COMMENTS = "tsconfig*.json";

export default defineConfig([
  globalIgnores(["dist/"]),
  { files: ["**/*.{js,mjs,cjs,ts,mts,cts}"], plugins: { js }, extends: ["js/recommended"], languageOptions: { globals: {...globals.browser, ...globals.node} } },
  tseslint.configs.recommended,
  {
    files: ["**/*.svelte"],
    extends: [svelte.configs.recommended],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
      },
      parserOptions: {
        // Lint TypeScript inside Svelte files.
        projectService: true,
        extraFileExtensions: [".svelte"],
        parser: tseslint.parser,
      },
    },
  },
  { files: ["**/*.json"], ignores: ["package-lock.json", DOT_JSON_FILES_WITH_COMMENTS], plugins: { json }, language: "json/json", extends: ["json/recommended"] },
  { files: ["**/*.jsonc", DOT_JSON_FILES_WITH_COMMENTS], plugins: { json }, language: "json/jsonc", extends: ["json/recommended"] },
  { files: ["**/*.json5"], plugins: { json }, language: "json/json5", extends: ["json/recommended"] },
  { files: ["**/*.md"], plugins: { markdown }, language: "markdown/gfm", extends: ["markdown/recommended"] },
  { files: ["**/*.css"], plugins: { css }, language: "css/css", extends: ["css/recommended"] },
  eslintConfigPrettier
]);
