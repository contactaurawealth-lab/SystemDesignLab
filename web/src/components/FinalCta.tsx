"use client";

import React from "react";
import { ArrowDownToLine, Github, ArrowRight, ShieldCheck } from "lucide-react";

export default function FinalCta() {
  return (
    <section className="py-28 border-t border-hairline bg-radial-atmosphere relative overflow-hidden text-center">
      <div className="max-w-4xl mx-auto px-6 sm:px-8 relative z-10">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-hairline bg-surface text-xs text-ink-secondary mb-6">
          <span className="w-1.5 h-1.5 rounded-full bg-accent" />
          <span>Complete Offline Operating System</span>
        </div>

        <h2 className="text-4xl sm:text-6xl font-semibold tracking-tightest text-ink mb-6 leading-tight">
          Stop memorizing system design. <br />
          <span className="text-ink-secondary font-normal">
            Start understanding it.
          </span>
        </h2>

        <p className="text-base sm:text-lg text-ink-secondary max-w-xl mx-auto font-normal leading-relaxed mb-10">
          Download the standalone Android APK. No subscription, no paywalls,
          no required cloud login. 100% offline-ready immediately.
        </p>

        {/* Action CTAs */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5 mb-8">
          <a
            href="/downloads/system-design-lab.apk"
            download="system-design-lab.apk"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 text-[14.5px] font-medium text-white bg-ink hover:bg-black px-7 py-3.5 rounded-lg transition-all shadow-sm hover:shadow group"
          >
            <ArrowDownToLine className="w-4 h-4 group-hover:translate-y-0.5 transition-transform" />
            <span>Download Android App (APK)</span>
            <span className="text-xs text-white/70 font-mono">(35 MB)</span>
          </a>

          <a
            href="https://github.com/karanpratapsingh/system-design"
            target="_blank"
            rel="noopener noreferrer"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 text-[14.5px] font-medium text-ink bg-white hover:bg-surface border border-hairline px-6 py-3.5 rounded-lg transition-all"
          >
            <Github className="w-4 h-4" />
            <span>View Source Repository</span>
          </a>
        </div>

        <div className="flex items-center justify-center gap-6 text-xs text-ink-muted font-mono">
          <span>Android 8.0+</span>
          <span>&middot;</span>
          <span>ARM64 &amp; x86_64</span>
          <span>&middot;</span>
          <span>Offline Room DB</span>
        </div>
      </div>
    </section>
  );
}
