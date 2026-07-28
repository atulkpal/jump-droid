import { appendFile, existsSync, unlinkSync } from "fs";
import { join } from "path";

let logFilePath: string | null = null;
let initialized = false;

function getLogPath(): string {
  if (logFilePath) return logFilePath;
  // On Vercel, /tmp is the only writable dir
  const dir = process.env.VERCEL ? "/tmp" : process.cwd();
  logFilePath = join(dir, "campaign-run.log");
  return logFilePath;
}

function timestamp(): string {
  const d = new Date();
  return d.toISOString().replace("T", " ").replace("Z", "");
}

function logLine(level: string, message: string): string {
  return `[${timestamp()}] [${level}] ${message}`;
}

function writeFile(msg: string): void {
  try {
    appendFile(getLogPath(), msg + "\n", (err) => {
      if (err) console.error("campaignLogger: write failed", err.message);
    });
  } catch {
    // non-critical
  }
}

export function campaignLog(message: string): void {
  const line = logLine("INFO", message);
  console.log(line);
  writeFile(line);
}

export function campaignWarn(message: string): void {
  const line = logLine("WARN", message);
  console.warn(line);
  writeFile(line);
}

export function campaignError(message: string): void {
  const line = logLine("ERROR", message);
  console.error(line);
  writeFile(line);
}

export function campaignSeparator(): void {
  const d = new Date().toISOString().replace("T", " ").replace("Z", "");
  const line = `\n===== Campaign Run ${d} =====`;
  console.log(line);
  writeFile(line);
}

export function clearCampaignLogs(): void {
  try {
    if (existsSync(getLogPath())) {
      unlinkSync(getLogPath());
      logFilePath = null;
      initialized = false;
    }
  } catch {
    // non-critical
  }
}

export function readCampaignLogs(): string {
  try {
    const { readFileSync } = require("fs");
    if (existsSync(getLogPath())) {
      return readFileSync(getLogPath(), "utf-8");
    }
  } catch {
    // non-critical
  }
  return "";
}
