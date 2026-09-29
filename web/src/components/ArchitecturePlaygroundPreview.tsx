"use client";

import React, { useState } from "react";
import { Plus, Play, RotateCcw, Check, Layers, Cpu, Database, Server, Globe, Radio } from "lucide-react";

interface ComponentNode {
  id: string;
  name: string;
  category: "edge" | "compute" | "cache" | "queue" | "storage";
  latency: number;
}

export default function ArchitecturePlaygroundPreview() {
  const [pipeline, setPipeline] = useState<string[]>([
    "Edge CDN",
    "L7 Load Balancer",
    "App Server Cluster",
    "Redis Cache",
    "PostgreSQL DB",
  ]);

  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [runStats, setRunStats] = useState<{
    throughput: string;
    p99: string;
    sla: string;
  } | null>(null);

  const availableCatalog = [
    { name: "Edge CDN", icon: Globe, category: "Edge Caching" },
    { name: "Kafka Message Queue", icon: Radio, category: "Async Buffering" },
    { name: "Redis Cache", icon: Cpu, category: "In-Memory Store" },
    { name: "Read Replicas", icon: Database, category: "Database Scaling" },
  ];

  const handleAdd = (name: string) => {
    if (!pipeline.includes(name)) {
      setPipeline([...pipeline, name]);
      setRunStats(null);
    }
  };

  const handleRemove = (name: string) => {
    setPipeline(pipeline.filter((item) => item !== name));
    setRunStats(null);
  };

  const handleRun = () => {
    setIsRunning(true);
    setTimeout(() => {
      setIsRunning(false);
      const hasCdn = pipeline.includes("Edge CDN");
      const hasCache = pipeline.includes("Redis Cache");
      const hasQueue = pipeline.includes("Kafka Message Queue");

      setRunStats({
        throughput: hasQueue ? "85,000 req/s" : hasCache ? "24,000 req/s" : "4,200 req/s",
        p99: hasCdn && hasCache ? "8.4 ms" : hasCache ? "26.1 ms" : "185.0 ms",
        sla: hasQueue ? "99.999%" : hasCache ? "99.95%" : "99.2%",
      });
    }, 700);
  };

  const handleReset = () => {
    setPipeline(["Edge CDN", "L7 Load Balancer", "App Server Cluster", "Redis Cache", "PostgreSQL DB"]);
    setRunStats(null);
  };

  return (
    <section id="playground" className="py-24 border-t border-hairline bg-white">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Sandbox &middot; Architecture Playground
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            Build your own system. <br />
            <span className="text-ink-secondary font-normal">
              Validate every decision before code.
            </span>
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Drag, connect, and configure technical components on an open canvas.
            Run simulation workloads to stress-test your design against traffic spikes.
          </p>
        </div>

        {/* Playground Interactive Frame */}
        <div className="rounded-xl border border-hairline bg-surface/40 p-6 sm:p-8">
          {/* Action Toolbar */}
          <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-hairline mb-6">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono text-ink-muted">ADD TO PIPELINE:</span>
              <div className="flex flex-wrap gap-2">
                {availableCatalog.map((item) => {
                  const alreadyIn = pipeline.includes(item.name);
                  return (
                    <button
                      key={item.name}
                      onClick={() => (alreadyIn ? handleRemove(item.name) : handleAdd(item.name))}
                      className={`text-xs px-2.5 py-1 rounded-md border transition-all flex items-center gap-1.5 ${
                        alreadyIn
                          ? "bg-slate-200 border-slate-300 text-ink font-medium"
                          : "bg-white border-hairline text-ink-secondary hover:text-ink hover:border-hairline-dark"
                      }`}
                    >
                      {alreadyIn ? <Check className="w-3 h-3 text-emerald-600" /> : <Plus className="w-3 h-3 text-ink-muted" />}
                      <span>{item.name}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={handleReset}
                className="p-2 rounded-lg border border-hairline bg-white hover:bg-surface text-ink-secondary"
                title="Reset Pipeline"
              >
                <RotateCcw className="w-3.5 h-3.5" />
              </button>
              <button
                onClick={handleRun}
                disabled={isRunning}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-ink text-white text-xs font-semibold hover:bg-black transition-colors disabled:opacity-50 shadow-sm"
              >
                <Play className="w-3.5 h-3.5 fill-current" />
                <span>{isRunning ? "Simulating Traffic..." : "Run System Load Test"}</span>
              </button>
            </div>
          </div>

          {/* Active Architecture Flow */}
          <div className="p-6 bg-white rounded-lg border border-hairline mb-6">
            <div className="text-xs font-mono text-ink-muted uppercase mb-4">
              Active Request Propagation Sequence:
            </div>
            <div className="flex items-center flex-wrap gap-3">
              {pipeline.map((item, idx) => (
                <React.Fragment key={item}>
                  <div className="flex items-center gap-2 px-3 py-2 rounded-md bg-surface border border-hairline text-xs font-mono text-ink">
                    <span className="w-1.5 h-1.5 rounded-full bg-accent" />
                    <span>{item}</span>
                  </div>
                  {idx < pipeline.length - 1 && (
                    <span className="text-hairline-dark font-mono text-xs">&rarr;</span>
                  )}
                </React.Fragment>
              ))}
            </div>
          </div>

          {/* Simulation Output Telemetry */}
          {runStats && (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 p-4 rounded-lg bg-emerald-50/50 border border-emerald-200 animate-fadeIn">
              <div>
                <div className="text-[11px] font-mono text-emerald-700 uppercase">Max Peak Throughput</div>
                <div className="text-xl font-mono font-semibold text-emerald-900 mt-0.5">{runStats.throughput}</div>
              </div>
              <div>
                <div className="text-[11px] font-mono text-emerald-700 uppercase">P99 End-to-End Latency</div>
                <div className="text-xl font-mono font-semibold text-emerald-900 mt-0.5">{runStats.p99}</div>
              </div>
              <div>
                <div className="text-[11px] font-mono text-emerald-700 uppercase">Estimated Availability SLA</div>
                <div className="text-xl font-mono font-semibold text-emerald-900 mt-0.5">{runStats.sla}</div>
              </div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
