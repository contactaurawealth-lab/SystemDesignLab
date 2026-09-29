"use client";

import React from "react";
import { Flame, CheckCircle, Award, Target, BarChart2 } from "lucide-react";

export default function ProgressSection() {
  const dimensions = [
    { name: "Understanding", percentage: 82, desc: "Foundational theory & architectural patterns" },
    { name: "Application", percentage: 70, desc: "Hands-on failure diagnosis & stress testing" },
    { name: "Recall", percentage: 61, desc: "Spaced retrieval & trade-off retention" },
    { name: "Design", percentage: 54, desc: "End-to-end multi-region system synthesis" },
  ];

  return (
    <section className="py-24 border-t border-hairline bg-white">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Multi-Dimensional Mastery &middot; Real Progress
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            Progress that actually matters.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            We don&apos;t count empty &ldquo;pages read.&rdquo; System Design Lab measures
            genuine competence across 4 core engineering competencies.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          {/* Progress Dimensions (7 cols) */}
          <div className="lg:col-span-7 space-y-6">
            {dimensions.map((dim) => (
              <div key={dim.name} className="space-y-2">
                <div className="flex items-center justify-between text-xs font-mono">
                  <span className="font-semibold text-ink uppercase tracking-wide">
                    {dim.name}
                  </span>
                  <span className="text-ink-secondary font-medium">
                    {dim.percentage}%
                  </span>
                </div>
                {/* Progress bar */}
                <div className="w-full h-2 bg-surface rounded-full overflow-hidden border border-hairline">
                  <div
                    className="h-full bg-ink rounded-full transition-all duration-700"
                    style={{ width: `${dim.percentage}%` }}
                  />
                </div>
                <div className="text-[11px] text-ink-muted">{dim.desc}</div>
              </div>
            ))}
          </div>

          {/* Right Summary Badges & Streak (5 cols) */}
          <div className="lg:col-span-5 p-6 rounded-xl border border-hairline bg-surface/50 space-y-4">
            <div className="flex items-center justify-between p-3.5 rounded-lg bg-white border border-hairline">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center border border-amber-200">
                  <Flame className="w-4 h-4 fill-current" />
                </div>
                <div>
                  <div className="text-xs font-semibold text-ink">Active Streak</div>
                  <div className="text-[11px] text-ink-muted">7 days consistent</div>
                </div>
              </div>
              <span className="text-sm font-mono font-bold text-ink">7 DAYS</span>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-lg bg-white border border-hairline">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-blue-50 text-accent flex items-center justify-center border border-blue-200">
                  <Target className="w-4 h-4" />
                </div>
                <div>
                  <div className="text-xs font-semibold text-ink">Systems Designed</div>
                  <div className="text-[11px] text-ink-muted">URL Shortener, Chat, Feed</div>
                </div>
              </div>
              <span className="text-sm font-mono font-bold text-ink">10 Systems</span>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-lg bg-white border border-hairline">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-200">
                  <CheckCircle className="w-4 h-4" />
                </div>
                <div>
                  <div className="text-xs font-semibold text-ink">Simulations Solved</div>
                  <div className="text-[11px] text-ink-muted">Outages diagnosed &amp; patched</div>
                </div>
              </div>
              <span className="text-sm font-mono font-bold text-ink">18 Fixed</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
