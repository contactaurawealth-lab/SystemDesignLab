import React from "react";
import Link from "next/link";
import { Github, ArrowDownToLine } from "lucide-react";

export default function Footer() {
  return (
    <footer className="border-t border-hairline bg-white py-12 text-ink-secondary text-xs">
      <div className="max-w-6xl mx-auto px-6 sm:px-8 flex flex-col sm:flex-row items-center justify-between gap-6">
        {/* Left: Brand & Attribution */}
        <div className="space-y-1 text-center sm:text-left">
          <div className="font-semibold text-ink text-sm">System Design Lab</div>
          <div className="text-ink-muted">
            An intelligent offline laboratory for understanding software systems.
          </div>
          <div className="text-ink-muted font-mono text-[11px]">
            Based on{" "}
            <a
              href="https://github.com/karanpratapsingh/system-design"
              target="_blank"
              rel="noopener noreferrer"
              className="text-ink underline hover:text-accent"
            >
              karanpratapsingh/system-design
            </a>{" "}
            &middot; MIT License
          </div>
        </div>

        {/* Right: Quick Links */}
        <div className="flex items-center gap-6 font-mono text-ink-secondary">
          <a
            href="https://github.com/karanpratapsingh/system-design"
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-ink transition-colors flex items-center gap-1.5"
          >
            <Github className="w-3.5 h-3.5" />
            <span>GitHub</span>
          </a>
          <a
            href="/downloads/system-design-lab.apk"
            download="system-design-lab.apk"
            className="hover:text-ink transition-colors flex items-center gap-1.5 text-ink font-semibold"
          >
            <ArrowDownToLine className="w-3.5 h-3.5 text-accent" />
            <span>Download APK</span>
          </a>
        </div>
      </div>
    </footer>
  );
}
