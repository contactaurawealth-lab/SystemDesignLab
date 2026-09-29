"use client";

import React, { useState } from "react";
import { Folder, FileText, ChevronRight, Github, ExternalLink, Code } from "lucide-react";

export default function RepositoryExplorerPreview() {
  const [selectedFile, setSelectedFile] = useState<string>("consistent-hashing.md");

  const files = [
    {
      name: "system-design-foundations.md",
      folder: "01-foundations",
      topic: "Latency, Bandwidth, and Availability Math",
      lesson: "Foundations &middot; Lesson 01",
      simulation: "Throughput Bottleneck",
    },
    {
      name: "load-balancing.md",
      folder: "02-networking",
      topic: "L4 vs L7 Reverse Proxy Routing Algorithms",
      lesson: "Networking &middot; Lesson 04",
      simulation: "Round Robin vs Least Connections",
    },
    {
      name: "consistent-hashing.md",
      folder: "03-scalability",
      topic: "Virtual Node Allocation & Rebalancing Ring",
      lesson: "Scalability &middot; Lesson 07",
      simulation: "Hash Ring Token Visualizer",
    },
    {
      name: "caching-strategies.md",
      folder: "04-caching",
      topic: "Cache-Aside, Write-Through & Eviction Algorithms",
      lesson: "Caching &middot; Lesson 11",
      simulation: "Thundering Herd Outage",
    },
  ];

  const current = files.find((f) => f.name === selectedFile) || files[0];

  return (
    <section className="py-24 border-t border-hairline bg-white">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Open Source &middot; Ground Truth
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            Directly linked to the repository.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Based on <span className="font-mono text-ink">karanpratapsingh/system-design</span>.
            Seamless two-way navigation lets you hop from high-level lesson directly to original source Markdown and code.
          </p>
        </div>

        {/* Explorer Two-Pane Frame */}
        <div className="rounded-xl border border-hairline bg-surface/40 overflow-hidden grid grid-cols-1 md:grid-cols-12 shadow-sm">
          {/* File Tree (5 cols) */}
          <div className="md:col-span-5 p-4 border-r border-hairline bg-surface/70 space-y-1">
            <div className="flex items-center justify-between text-xs font-mono text-ink-muted px-2 py-2 mb-2">
              <span className="flex items-center gap-1.5">
                <Github className="w-3.5 h-3.5" />
                <span>karanpratapsingh/system-design</span>
              </span>
              <span>main branch</span>
            </div>

            {files.map((file) => (
              <div
                key={file.name}
                onClick={() => setSelectedFile(file.name)}
                className={`cursor-pointer p-2.5 rounded-lg text-xs font-mono transition-colors flex items-center justify-between ${
                  selectedFile === file.name
                    ? "bg-white text-ink shadow-sm border border-hairline font-semibold"
                    : "text-ink-secondary hover:bg-white/60 hover:text-ink"
                }`}
              >
                <div className="flex items-center gap-2 truncate">
                  <FileText className="w-3.5 h-3.5 text-ink-muted shrink-0" />
                  <span className="truncate">{file.name}</span>
                </div>
                <ChevronRight className="w-3.5 h-3.5 text-ink-muted shrink-0" />
              </div>
            ))}
          </div>

          {/* Inspector (7 cols) */}
          <div className="md:col-span-7 p-6 bg-white flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-3 border-b border-hairline mb-4">
                <div className="flex items-center gap-2 text-xs font-mono text-ink">
                  <Folder className="w-3.5 h-3.5 text-ink-muted" />
                  <span>{current.folder} / {current.name}</span>
                </div>
                <a
                  href={`https://github.com/karanpratapsingh/system-design`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-xs text-ink-secondary hover:text-ink flex items-center gap-1 font-mono"
                >
                  <span>View on GitHub</span>
                  <ExternalLink className="w-3 h-3" />
                </a>
              </div>

              <h4 className="text-base font-semibold text-ink mb-2">
                {current.topic}
              </h4>
              <p className="text-xs text-ink-secondary leading-relaxed mb-6">
                Integrated bidirectionally with System Design Lab. Every repository concept is paired with an interactive 3D simulation, audio chapter, and diagnostic failure lab.
              </p>

              <div className="grid grid-cols-2 gap-3 text-xs font-mono">
                <div className="p-3 rounded-lg bg-surface border border-hairline">
                  <div className="text-[10px] text-ink-muted uppercase">Interactive Lesson</div>
                  <div className="text-ink font-semibold mt-0.5">{current.lesson}</div>
                </div>
                <div className="p-3 rounded-lg bg-surface border border-hairline">
                  <div className="text-[10px] text-ink-muted uppercase">3D Simulation</div>
                  <div className="text-ink font-semibold mt-0.5">{current.simulation}</div>
                </div>
              </div>
            </div>

            <div className="pt-6 border-t border-hairline mt-6 flex items-center justify-between text-xs text-ink-secondary">
              <span>Sync Status: 100% Up to Date</span>
              <span className="font-mono text-[11px] text-ink-muted">56/56 Markdown Files Mapped</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
