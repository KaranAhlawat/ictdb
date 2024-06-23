import { spawnSync } from "node:child_process"
import { defineConfig } from "vite"

function runMillTask(task) {
  const result = spawnSync("./mill", ["show", task], {
    stdio: [
      "pipe",
      "pipe",
      "inherit"
    ]
  });

  return JSON.parse(result.stdout);
}

function alias(mode) {
  if (mode === "development") return runMillTask("ui.publicDev")
  if (mode === "development") return runMillTask("ui.publicProd")
}

export default defineConfig(({ mode }) => {
  return {
    resolve: {
      alias: alias(mode)
    }
  }
})
