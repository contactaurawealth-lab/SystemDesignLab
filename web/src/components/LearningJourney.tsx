"use client";

import React, { useState } from "react";
import { Check, Lock, ChevronRight, BookOpen, Layers, Terminal, Sparkles } from "lucide-react";

interface JourneyStage {
  id: string;
  step: string;
  title: string;
  category: string;
  status: "completed" | "in_progress" | "locked";
  prerequisites: string[];
  concepts: string[];
  simulation: string;
  deliverable: string;
  description: string;
}

const stages: JourneyStage[] = [
  {
    id: "foundations",
    step: "01",
    title: "System Design Foundations",
    category: "FOUNDATIONS",
    status: "completed",
    prerequisites: ["None — Beginner Friendly"],
    concepts: ["Latency vs Throughput", "Availability vs Consistency", "SLA / SLO / SLI", "Redundancy"],
    simulation: "Single Server Bottleneck Test",
    deliverable: "Calculate system availability percentage for 99.9% vs 99.99%",
    description: "Understand the fundamental physics of computer systems: why disks are slow, why networks fail, and how SLAs dictate architecture.",
  },
  {
    id: "networking",
    step: "02",
    title: "Modern Computer Networking",
    category: "NETWORKING",
    status: "completed",
    prerequisites: ["Foundations"],
    concepts: ["DNS Resolution", "TCP vs UDP", "HTTP/1.1 vs HTTP/2 vs HTTP/3", "TLS Handshakes", "WebSockets"],
    simulation: "Packet Route & TLS Latency Waterfall",
    deliverable: "Inspect DNS query propagation and TCP 3-way handshake round trips",
    description: "Follow a packet from client keystroke through DNS resolution, CDN edge termination, and TLS handshakes.",
  },
  {
    id: "scalability",
    step: "03",
    title: "Scalability & Load Balancing",
    category: "SCALABILITY",
    status: "completed",
    prerequisites: ["Networking"],
    concepts: ["Horizontal vs Vertical Scaling", "L4 vs L7 Load Balancers", "Reverse Proxies", "Consistent Hashing"],
    simulation: "Ring Hash Key Distribution with Virtual Nodes",
    deliverable: "Distribute 100,000 keys across a 5-node cluster with zero hotspots",
    description: "Learn how modern load balancers distribute millions of connections without state bottlenecks.",
  },
  {
    id: "caching",
    step: "04",
    title: "High-Performance Caching",
    category: "CACHING",
    status: "completed",
    prerequisites: ["Scalability"],
    concepts: ["Cache Aside vs Write-Through", "Eviction Policies (LRU/LFU)", "Cache Stampede / Thundering Herd", "Redis Clusters"],
    simulation: "Thundering Herd Cache Invalidation Test",
    deliverable: "Protect database using probabilistic early expiration and mutex locks",
    description: "Master in-memory caching strategies, cache stampede mitigation, and multi-tier CDN caching.",
  },
  {
    id: "databases",
    step: "05",
    title: "Databases & Storage Engines",
    category: "DATABASES",
    status: "in_progress",
    prerequisites: ["Scalability", "Caching"],
    concepts: ["SQL vs NoSQL", "ACID vs BASE", "B-Trees vs LSM-Trees", "Indexes & Query Plans", "Replication & Sharding"],
    simulation: "Primary-Replica Failover & Write-Ahead Log Lag",
    deliverable: "Design range-based and hash-based sharding schema for 500M user records",
    description: "Explore underlying storage engines, index mechanics, replication topologies, and partitioned tables.",
  },
  {
    id: "distributed",
    step: "06",
    title: "Distributed Systems & Consensus",
    category: "DISTRIBUTED SYSTEMS",
    status: "locked",
    prerequisites: ["Databases", "Scalability"],
    concepts: ["CAP Theorem & PACELC", "Two-Phase Commit (2PC)", "Raft Consensus", "Vector Clocks & Eventual Consistency"],
    simulation: "Split-Brain Network Partition & Leader Election",
    deliverable: "Resolve concurrent conflicting writes across two datacenter partitions",
    description: "Understand how distributed nodes agree on state during network partitions and hardware faults.",
  },
  {
    id: "architecture",
    step: "07",
    title: "Asynchronous Architecture & Queues",
    category: "ARCHITECTURE",
    status: "locked",
    prerequisites: ["Distributed Systems"],
    concepts: ["Message Queues (Kafka / RabbitMQ)", "Event-Driven Architecture", "Publish-Subscribe", "Idempotency & Dead Letter Queues"],
    simulation: "Backpressure & Consumer Lag Surge",
    deliverable: "Build an idempotent payment event consumer with retry queues",
    description: "Decouple synchronous request-response bottlenecks using persistent event logs and distributed streaming.",
  },
  {
    id: "interview",
    step: "08",
    title: "Real-World System Design & Interviews",
    category: "INTERVIEW PRACTICE",
    status: "locked",
    prerequisites: ["All Modules"],
    concepts: ["URL Shortener (TinyURL)", "Global Chat (WhatsApp)", "Video Streaming (YouTube)", "Ride Sharing (Uber)"],
    simulation: "Full End-to-End System Evaluation Lab",
    deliverable: "Interactive mock interview session with real-time bottleneck interrogation",
    description: "Synthesize all knowledge into production systems capable of handling 100M+ active users.",
  },
];

export default function LearningJourney() {
  const [selectedStage, setSelectedStage] = useState<JourneyStage>(stages[4]); // default to in_progress stage

  return (
    <section id="journey" className="py-24 border-t border-hairline bg-white">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        {/* Section Header */}
        <div className="max-w-3xl mb-16">
          <div className="text-[12px] font-mono text-ink-muted uppercase tracking-wider mb-3">
            Knowledge Map &middot; Structured Progression
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            A map of what you know. <br />
            <span className="text-ink-secondary font-normal">
              And exactly what to build next.
            </span>
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Never wonder what to study next. The curriculum adapts dynamically to
            your completed concepts, proven simulation scores, and weak points.
          </p>
        </div>

        {/* Master Journey Layout: Left Roadmap + Right Inspector */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Left: Interactive Roadmap Nodes (7 cols) */}
          <div className="lg:col-span-7 space-y-3">
            {stages.map((stage, idx) => {
              const isSelected = selectedStage.id === stage.id;
              const isCompleted = stage.status === "completed";
              const isInProgress = stage.status === "in_progress";
              const isLocked = stage.status === "locked";

              return (
                <div
                  key={stage.id}
                  onClick={() => setSelectedStage(stage)}
                  className={`cursor-pointer p-4 rounded-xl border transition-all duration-200 flex items-center justify-between ${
                    isSelected
                      ? "border-ink bg-surface shadow-sm ring-1 ring-ink/10"
                      : "border-hairline bg-white hover:border-hairline-dark hover:bg-surface/40"
                  }`}
                >
                  <div className="flex items-center gap-3.5">
                    {/* Status Icon */}
                    <div
                      className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-mono font-medium ${
                        isCompleted
                          ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                          : isInProgress
                          ? "bg-blue-50 text-accent border border-blue-200 animate-pulse"
                          : "bg-surface text-ink-muted border border-hairline"
                      }`}
                    >
                      {isCompleted ? (
                        <Check className="w-3.5 h-3.5 stroke-[2.5]" />
                      ) : isLocked ? (
                        <Lock className="w-3 h-3 text-ink-muted" />
                      ) : (
                        <span>{stage.step}</span>
                      )}
                    </div>

                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-mono uppercase tracking-wider text-ink-muted">
                          {stage.category}
                        </span>
                        {isInProgress && (
                          <span className="text-[9px] font-mono px-1.5 py-0.2 rounded bg-accent text-white uppercase">
                            Current
                          </span>
                        )}
                      </div>
                      <div className="text-sm font-semibold text-ink">{stage.title}</div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 text-xs font-mono text-ink-muted">
                    <span className="hidden sm:inline">
                      {isCompleted ? "Mastered" : isInProgress ? "In Progress" : "Prerequisites Req"}
                    </span>
                    <ChevronRight
                      className={`w-4 h-4 transition-transform ${
                        isSelected ? "rotate-90 text-ink" : "text-ink-muted"
                      }`}
                    />
                  </div>
                </div>
              );
            })}
          </div>

          {/* Right: Stage Inspector (5 cols) */}
          <div className="lg:col-span-5 p-6 rounded-xl border border-hairline bg-surface/50 sticky top-24">
            <div className="flex items-center justify-between pb-3 border-b border-hairline mb-4">
              <span className="text-xs font-mono uppercase text-ink-muted">
                STAGE {selectedStage.step} INSPECTOR
              </span>
              <span
                className={`text-[11px] font-mono px-2 py-0.5 rounded border ${
                  selectedStage.status === "completed"
                    ? "bg-emerald-50 text-emerald-700 border-emerald-200"
                    : selectedStage.status === "in_progress"
                    ? "bg-blue-50 text-accent border-blue-200 font-semibold"
                    : "bg-white text-ink-muted border-hairline"
                }`}
              >
                {selectedStage.status === "completed"
                  ? "✓ Verified Mastered"
                  : selectedStage.status === "in_progress"
                  ? "● Active Exploration"
                  : "🔒 Locked"}
              </span>
            </div>

            <h3 className="text-xl font-semibold text-ink mb-2">
              {selectedStage.title}
            </h3>
            <p className="text-xs text-ink-secondary leading-relaxed mb-6">
              {selectedStage.description}
            </p>

            {/* Prerequisites */}
            <div className="mb-4">
              <div className="text-[11px] font-mono uppercase text-ink-muted mb-1.5">
                Prerequisites
              </div>
              <div className="flex flex-wrap gap-1.5">
                {selectedStage.prerequisites.map((p, i) => (
                  <span
                    key={i}
                    className="text-xs px-2 py-1 rounded bg-white border border-hairline text-ink-secondary font-mono"
                  >
                    {p}
                  </span>
                ))}
              </div>
            </div>

            {/* Core Concepts */}
            <div className="mb-4">
              <div className="text-[11px] font-mono uppercase text-ink-muted mb-1.5">
                Core Architectural Concepts
              </div>
              <div className="space-y-1.5">
                {selectedStage.concepts.map((c, i) => (
                  <div
                    key={i}
                    className="text-xs text-ink flex items-center gap-2 bg-white px-2.5 py-1.5 rounded border border-hairline"
                  >
                    <span className="w-1.5 h-1.5 rounded-full bg-accent" />
                    <span>{c}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* 3D Simulation Tied to this Stage */}
            <div className="p-3 rounded-lg bg-white border border-hairline mb-4">
              <div className="text-[10px] font-mono uppercase text-accent font-semibold mb-1">
                Integrated 3D Simulation
              </div>
              <div className="text-xs font-medium text-ink">
                {selectedStage.simulation}
              </div>
            </div>

            {/* Milestone Challenge */}
            <div className="p-3 rounded-lg bg-white border border-hairline">
              <div className="text-[10px] font-mono uppercase text-ink-muted mb-1">
                Practical Build Deliverable
              </div>
              <div className="text-xs text-ink-secondary">
                {selectedStage.deliverable}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
