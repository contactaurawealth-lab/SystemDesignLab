"use client";

import React, { useState } from "react";
import { Play, Pause, SkipBack, SkipForward, Volume2, Moon, Clock, WifiOff, Check } from "lucide-react";

export default function AudiobookPreview() {
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [speed, setSpeed] = useState<string>("1.0×");
  const [progress, setProgress] = useState<number>(38); // 38%

  const tracks = [
    { title: "Chapter 1: The Core Physics of Latency & Throughput", duration: "11:42", completed: true },
    { title: "Chapter 2: Consistent Hashing and Ring Topologies", duration: "14:15", completed: false, active: true },
    { title: "Chapter 3: Cache Invalidation & Thundering Herds", duration: "09:30", completed: false },
    { title: "Chapter 4: The PACELC Theorem in Distributed Databases", duration: "16:04", completed: false },
  ];

  const speeds = ["1.0×", "1.25×", "1.5×", "2.0×"];

  return (
    <section id="listen" className="py-24 border-t border-hairline bg-white">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Audio Learning &middot; 100% Offline
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            Learn while away from your desk.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Every core architecture chapter is narrated for deep auditory comprehension.
            Listen during commutes, walks, or workouts with background audio and offline caching.
          </p>
        </div>

        {/* Minimalist Player Widget */}
        <div className="rounded-xl border border-hairline bg-surface/50 p-6 sm:p-8 max-w-4xl mx-auto shadow-sm">
          {/* Header Status */}
          <div className="flex items-center justify-between pb-4 border-b border-hairline mb-6">
            <div className="flex items-center gap-2">
              <WifiOff className="w-4 h-4 text-emerald-600" />
              <span className="text-xs font-mono font-medium text-ink">
                CACHED LOCALLY ON DEVICE &middot; ZERO BUFFERING
              </span>
            </div>
            <div className="flex items-center gap-2 text-xs font-mono text-ink-muted">
              <Moon className="w-3.5 h-3.5" />
              <span>Sleep Timer: 30m</span>
            </div>
          </div>

          {/* Current Playing Details */}
          <div className="mb-6">
            <div className="text-[11px] font-mono uppercase text-accent font-semibold mb-1">
              NOW PLAYING &middot; CHAPTER 02
            </div>
            <h3 className="text-xl font-semibold text-ink">
              Consistent Hashing and Ring Topologies
            </h3>
            <div className="text-xs text-ink-muted mt-1 font-mono">
              System Design Lab &middot; Distributed Scalability
            </div>
          </div>

          {/* Scrubber Progress Bar */}
          <div className="space-y-2 mb-6">
            <div className="w-full bg-hairline rounded-full h-1.5 cursor-pointer relative overflow-hidden">
              <div
                className="bg-ink h-1.5 rounded-full transition-all"
                style={{ width: `${progress}%` }}
              />
            </div>
            <div className="flex items-center justify-between text-[11px] font-mono text-ink-muted">
              <span>05:24</span>
              <span>14:15</span>
            </div>
          </div>

          {/* Player Controls Bar */}
          <div className="flex flex-wrap items-center justify-between gap-4 pt-2">
            {/* Speed selector */}
            <div className="flex items-center gap-1 border border-hairline bg-white rounded-lg p-1">
              {speeds.map((s) => (
                <button
                  key={s}
                  onClick={() => setSpeed(s)}
                  className={`text-[11px] font-mono px-2 py-0.5 rounded transition-colors ${
                    speed === s ? "bg-ink text-white font-semibold" : "text-ink-secondary hover:text-ink"
                  }`}
                >
                  {s}
                </button>
              ))}
            </div>

            {/* Main playback buttons */}
            <div className="flex items-center gap-4">
              <button
                onClick={() => setProgress((p) => Math.max(0, p - 10))}
                className="p-2 text-ink-secondary hover:text-ink"
                title="Rewind 15 seconds"
              >
                <SkipBack className="w-4 h-4" />
              </button>
              <button
                onClick={() => setIsPlaying(!isPlaying)}
                className="w-11 h-11 rounded-full bg-ink text-white flex items-center justify-center hover:bg-black transition-transform active:scale-95 shadow-sm"
              >
                {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4 ml-0.5 fill-current" />}
              </button>
              <button
                onClick={() => setProgress((p) => Math.min(100, p + 10))}
                className="p-2 text-ink-secondary hover:text-ink"
                title="Forward 15 seconds"
              >
                <SkipForward className="w-4 h-4" />
              </button>
            </div>

            {/* Offline Chapter Badge */}
            <div className="text-xs font-mono text-ink-secondary flex items-center gap-1.5 bg-white px-3 py-1.5 rounded-lg border border-hairline">
              <Volume2 className="w-3.5 h-3.5 text-accent" />
              <span>Studio Master Narration</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
