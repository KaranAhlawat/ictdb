import { spawnSync } from "child_process";
import path from "path";
import { cwd } from "process";
import { defineConfig } from "vite";

function alias(mode) {
  if (mode === "development") return runMillTask("ui.publicDev");
  if (mode === "production") return runMillTask("ui.publicProd");
  const prefix = "test:";
  if (mode.startsWith(prefix))
    return {
      "@public": mode.substring(prefix.length),
    };
}

export default defineConfig(({ mode }) => {
  return {
    root: path.join(cwd(), 'ui/public'),
    resolve: {
      alias: alias(mode),
    },
  };
});

function runMillTask(task) {
  const result = spawnSync("./mill", ["show", task], {
    stdio: [
      "pipe", // StdIn.
      "pipe", // StdOut.
      "inherit", // StdErr.
    ],
  });

  return JSON.parse(result.stdout);
}
