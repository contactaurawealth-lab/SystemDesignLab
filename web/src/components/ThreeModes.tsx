"use client";

import React, { useState } from "react";
import { BookOpen, Box, Headphones, ArrowRight, CheckCircle2, ChevronRight } from "lucide-react";

export default function ThreeModes() {
  const [activeTab, setActiveTab] = useState<number>(0);

  const modes = [
    {
      num: "01",
      id: "learn",
      title: "LEARN",
      tagline: "Structured lessons built around the repository.",
      description:
        "Every concept is deconstructed into three progressive tiers: Level 1 (Simple Intuition), Level 2 (Engineering Mechanism), and Level 3 (Deep Dive & Trade-offs). Read diagrams, understand edge cases, and solve progressive interactive checkpoints.",
      icon: BookOpen,
      features: [
        "56 modular chapters covering DNS to Distributed Consensus",
        "43 original Excalidraw architectural diagrams",
        "Progressive disclosure: beginner intuition to production edge cases",
        "Direct bidirectional link to karanpratapsingh/system-design code",
      ],
      sample: {
        topic: "Consistent Hashing & Virtual Nodes",
        difficulty: "Intermediate",
        time: "8 min read",
        preview: "Why standard hash(key) % N breaks down during cluster rebalancing and how virtual tokens distribute keys evenly across ring topologies.",
      },
    },
    {
      num: "02",
      id: "simulate",
      title: "SIMULATE",
      tagline: "Watch systems behave under real conditions.",
      description:
        "Step inside simulated technical environments. Render server racks, network packets, load balancers, and cache tiers in real-time. Ramp up traffic from 100 to 10,000 req/s, kill worker nodes, and witness cascade failures before they happen in production.",
      icon: Box,
      features: [
        "Interactive 3D simulation engine built with hardware acceleration",
        "Dynamic traffic sliders: test QPS surges and thundering herds",
        "Fault injection: simulate server crashes, network partitions, and slow disks",
        "Real-time telemetry: watch latency curves and saturation spikes",
      ],
      sample: {
        topic: "Thundering Herd Cache Invalidation",
        difficulty: "Advanced",
        time: "Live Simulation",
        preview: "10,000 simultaneous queries hit the database when a popular cache key expires. Observe DB queue depth escalate to 100% until mutex locks are applied.",
      },
    },
    {
      num: "03",
      id: "listen",
      title: "LISTEN",
      tagline: "Learn through offline audiobook chapters.",
      description:
        "Master system architecture while walking, commuting, or relaxing away from screens. Every major module features studio-crafted narration explaining complex distributed systems concepts clearly without relying on visual crutches.",
      icon: Headphones,
      features: [
        "100% offline audio playback with zero network buffering",
        "Precise speed controls (1.0×, 1.25×, 1.5×, 2.0×) and chapter seeking",
        "Sleep timer and persistent position sync with written lessons",
        "Narrated architecture walk-throughs designed for auditory clarity",
      ],
      sample: {
        topic: "CAP Theorem in Practice: PACELC",
        difficulty: "Core Foundations",
        time: "14 min audio",
        preview: "An auditory breakdown of latency vs consistency trade-offs when networks are operating normally versus partitioned.",
      },
    },
  ];

  return (
    <section id="learn" className="py-24 border-t border-hairline bg-white">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        {/* Editorial Headline */}
        <div className="max-w-3xl mb-16">
          <div className="text-[12px] font-mono text-ink-muted uppercase tracking-wider mb-3">
            Integrated Learning System
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink leading-tight mb-5">
            Learn the system, <br />
            <span className="text-ink-secondary font-normal">not the slides.</span>
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Memorizing definitions for interviews fails when real production systems
            buckle under load. System Design Lab converts repository knowledge into
            an experiential loop of three unified modalities.
          </p>
        </div>

        {/* The Three Modalities Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {modes.map((mode, index) => {
            const isSelected = activeTab === index;
            const Icon = mode.icon;
            return (
              <div
                key={mode.id}
                onClick={() => setActiveTab(index)}
                className={`cursor-pointer group flex flex-col justify-between p-7 rounded-xl border transition-all duration-200 ${
                  isSelected
                    ? "border-ink bg-surface shadow-[0_2px_12px_rgba(0,0,0,0.03)]"
                    : "border-hairline bg-white hover:border-hairline-dark hover:bg-surface/50"
                }`}
              >
                <div>
                  {/* Top Bar: Number & Icon */}
                  <div className="flex items-center justify-between mb-8 pb-4 border-b border-hairline/80">
                    <span className="font-mono text-sm font-semibold tracking-wider text-ink-muted group-hover:text-ink transition-colors">
                      {mode.num}
                    </span>
                    <div
                      className={`w-9 h-9 rounded-lg flex items-center justify-center transition-colors ${
                        isSelected
                          ? "bg-ink text-white"
                          : "bg-surface border border-hairline text-ink-secondary group-hover:text-ink"
                      }`}
                    >
                      <Icon className="w-4 h-4" />
                    </div>
                  </div>

                  {/* Mode Title */}
                  <h3 className="text-xl font-semibold tracking-tight text-ink mb-2">
                    {mode.title}
                  </h3>
                  <p className="text-sm font-medium text-ink-secondary mb-4 leading-snug">
                    {mode.tagline}
                  </p>
                  <p className="text-xs text-ink-muted leading-relaxed mb-6">
                    {mode.description}
                  </p>

                  {/* Bullet features */}
                  <div className="space-y-2.5 mb-8">
                    {mode.features.map((feat, i) => (
                      <div key={i} className="flex items-start gap-2 text-xs text-ink-secondary">
                        <CheckCircle2 className="w-3.5 h-3.5 text-accent shrink-0 mt-0.5" />
                        <span>{feat}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Sample Lesson Card Box */}
                <div className="p-4 rounded-lg bg-white border border-hairline">
                  <div className="flex items-center justify-between text-[11px] font-mono text-ink-muted mb-1.5">
                    <span>{mode.sample.difficulty}</span>
                    <span>{mode.sample.time}</span>
                  </div>
                  <div className="text-xs font-semibold text-ink mb-1">
                    {mode.sample.topic}
                  </div>
                  <p className="text-[11px] text-ink-secondary line-clamp-2 leading-relaxed">
                    {mode.sample.preview}
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Unifying Progress Guarantee */}
        <div className="mt-12 p-5 rounded-xl border border-hairline bg-surface/50 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="w-2 h-2 rounded-full bg-accent" />
            <div className="text-xs text-ink-secondary">
              <span className="font-semibold text-ink">Unified Progress Engine:</span>{" "}
              Reading a lesson, running its 3D simulation, and finishing its audiobook all feed the same verified mastery score.
            </div>
          </div>
          <div className="flex items-center gap-3 text-xs font-mono text-ink-muted">
            <span>56 Lessons</span>
            <span>&middot;</span>
            <span>12 Simulations</span>
            <span>&middot;</span>
            <span>5 Audio Modules</span>
          </div>
        </div>
      </div>
    </section>
  );
}
