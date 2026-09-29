"use client";

import React, { useState } from "react";
import { Server, Database, Cpu, ArrowRight, ShieldCheck, RefreshCw } from "lucide-react";

export default function ScrollExpansion() {
  const [isDistributed, setIsDistributed] = useState(true);

  return (
    <section className="py-24 border-t border-hairline bg-surface/30 relative">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        {/* Section Header */}
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-2">
            Dynamic Scaling &middot; From Single Node to Cluster
          </div>
          <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight text-ink mb-4">
            This isn&apos;t just a course. <br />
            <span className="text-ink-secondary font-normal">
              It&apos;s a living system you can expand.
            </span>
          </h2>
          <p className="text-base text-ink-secondary leading-relaxed">
            Most tutorials stop at static diagrams. In System Design Lab, every
            architecture evolves as load increases. Switch the topology below to
            see how a toy system scales into high-availability production infrastructure.
          </p>
        </div>

        {/* Interactive Topology Switcher */}
        <div className="flex items-center justify-between flex-wrap gap-4 mb-8">
          <div className="inline-flex p-1 bg-surface rounded-lg border border-hairline">
            <button
              onClick={() => setIsDistributed(false)}
              className={`px-4 py-2 text-xs font-medium rounded-md transition-all ${
                !isDistributed
                  ? "bg-white text-ink shadow-sm border border-hairline"
                  : "text-ink-muted hover:text-ink"
              }`}
            >
              Single Instance (Monolith)
            </button>
            <button
              onClick={() => setIsDistributed(true)}
              className={`px-4 py-2 text-xs font-medium rounded-md transition-all flex items-center gap-1.5 ${
                isDistributed
                  ? "bg-white text-ink shadow-sm border border-hairline font-semibold text-accent"
                  : "text-ink-muted hover:text-ink"
              }`}
            >
              <RefreshCw className="w-3 h-3 text-accent" />
              <span>Distributed Cluster (High Availability)</span>
            </button>
          </div>

          <div className="text-xs font-mono text-ink-secondary flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-accent" />
            <span>
              {isDistributed
                ? "Resilient Topology: 99.99% SLA &middot; 0 SPOF"
                : "Single Point of Failure: 1 Server &middot; 1 DB"}
            </span>
          </div>
        </div>

        {/* Architecture Comparison Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* 1. Compute Tier */}
          <div className="p-6 rounded-xl border border-hairline bg-white shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <span className="text-xs font-mono uppercase text-ink-muted">01 / Compute Tier</span>
                <Server className="w-4 h-4 text-ink-secondary" />
              </div>
              <h3 className="text-lg font-semibold text-ink mb-1">
                {isDistributed ? "Stateless Server Pool" : "Single Web Server"}
              </h3>
              <p className="text-xs text-ink-secondary mb-6 leading-relaxed">
                {isDistributed
                  ? "Horizontal auto-scaling group behind an L7 reverse proxy. Any worker node can crash without user interruption."
                  : "Monolithic server handling business logic, session storage, and cron jobs. A memory leak crashes the entire service."}
              </p>
            </div>

            {/* Visual Representation */}
            <div className="p-4 rounded-lg bg-surface border border-hairline">
              {isDistributed ? (
                <div className="space-y-2">
                  {["Worker-01 (us-east-1a)", "Worker-02 (us-east-1b)", "Worker-03 (us-east-1c)"].map((s) => (
                    <div
                      key={s}
                      className="flex items-center justify-between text-[11px] font-mono bg-white p-2 rounded border border-hairline"
                    >
                      <div className="flex items-center gap-2">
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                        <span className="text-ink font-medium">{s}</span>
                      </div>
                      <span className="text-ink-muted">33% traffic</span>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="flex items-center justify-between text-[11px] font-mono bg-white p-3 rounded border border-amber-200 bg-amber-50/40">
                  <div className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
                    <span className="text-ink font-medium">Server-01 (Single Node)</span>
                  </div>
                  <span className="text-amber-700 font-semibold">100% Load</span>
                </div>
              )}
            </div>
          </div>

          {/* 2. Caching Tier */}
          <div className="p-6 rounded-xl border border-hairline bg-white shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <span className="text-xs font-mono uppercase text-ink-muted">02 / In-Memory Tier</span>
                <Cpu className="w-4 h-4 text-ink-secondary" />
              </div>
              <h3 className="text-lg font-semibold text-ink mb-1">
                {isDistributed ? "Redis Cluster with Sharding" : "Local In-Process Cache"}
              </h3>
              <p className="text-xs text-ink-secondary mb-6 leading-relaxed">
                {isDistributed
                  ? "Consistent hashing spreads memory load across shards. Sentinel automatically promotes replica on failure."
                  : "Process-bound dictionary memory. Cache vanishes upon restart, sending a crushing thundering herd to the DB."}
              </p>
            </div>

            {/* Visual Representation */}
            <div className="p-4 rounded-lg bg-surface border border-hairline">
              {isDistributed ? (
                <div className="grid grid-cols-2 gap-2 text-[11px] font-mono">
                  <div className="bg-white p-2 rounded border border-hairline">
                    <div className="text-ink font-medium">Shard A &middot; Keys 0-8k</div>
                    <div className="text-[10px] text-ink-muted">Master + Replica</div>
                  </div>
                  <div className="bg-white p-2 rounded border border-hairline">
                    <div className="text-ink font-medium">Shard B &middot; Keys 8k-16k</div>
                    <div className="text-[10px] text-ink-muted">Master + Replica</div>
                  </div>
                </div>
              ) : (
                <div className="text-[11px] font-mono bg-white p-3 rounded border border-hairline text-center">
                  <div className="text-ink font-medium">Process Memory Map</div>
                  <div className="text-ink-muted text-[10px]">Max 2 GB &middot; No eviction replication</div>
                </div>
              )}
            </div>
          </div>

          {/* 3. Storage Tier */}
          <div className="p-6 rounded-xl border border-hairline bg-white shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <span className="text-xs font-mono uppercase text-ink-muted">03 / Persistence Tier</span>
                <Database className="w-4 h-4 text-ink-secondary" />
              </div>
              <h3 className="text-lg font-semibold text-ink mb-1">
                {isDistributed ? "Primary + Read Replicas" : "Single PostgreSQL DB"}
              </h3>
              <p className="text-xs text-ink-secondary mb-6 leading-relaxed">
                {isDistributed
                  ? "Dedicated write master with asynchronous WAL streaming to multi-region read replicas for 90% read offloading."
                  : "Reads and writes compete for the same disk I/O and table locks, causing latency degradation during spikes."}
              </p>
            </div>

            {/* Visual Representation */}
            <div className="p-4 rounded-lg bg-surface border border-hairline">
              {isDistributed ? (
                <div className="space-y-1.5 text-[11px] font-mono">
                  <div className="bg-white p-2 rounded border border-accent/40 flex justify-between items-center">
                    <span className="text-ink font-semibold">Primary (Writes)</span>
                    <span className="text-[10px] text-accent">WAL Master</span>
                  </div>
                  <div className="grid grid-cols-2 gap-1.5">
                    <div className="bg-white p-1.5 rounded border border-hairline text-[10px]">
                      Replica 1 (Reads)
                    </div>
                    <div className="bg-white p-1.5 rounded border border-hairline text-[10px]">
                      Replica 2 (Reads)
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-[11px] font-mono bg-white p-3 rounded border border-hairline text-center">
                  <div className="text-ink font-medium">Postgres Standalone</div>
                  <div className="text-ink-muted text-[10px]">All Read &amp; Write I/O Contention</div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
