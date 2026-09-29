"use client";

import React from "react";
import { WifiOff, Smartphone, ShieldCheck, Check, Sparkles } from "lucide-react";

export default function OfflineSection() {
  const offlineItems = [
    "All 56 Structured Lessons & Notes",
    "Hardware-Accelerated 3D Simulation Engine",
    "Studio Master Audiobook Chapters",
    "Failure Labs & Outage Scenarios",
    "Architecture Sandbox & Stress Tester",
    "Instant Local Search & Bookmarks",
    "Progress Tracking & Spaced Repetition",
    "Mock System Design Interviews",
  ];

  return (
    <section className="py-24 border-t border-hairline bg-surface/30">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Offline &amp; Mobile Architecture
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            Learn without a connection.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Subway tunnels, airplane mode, or spotty mobile data should never disrupt your learning.
            The entire platform operates 100% locally on your device with zero cloud lag.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Offline Matrix */}
          <div className="p-7 rounded-xl border border-hairline bg-white shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-2 text-xs font-mono text-emerald-700 uppercase mb-4">
                <WifiOff className="w-4 h-4" />
                <span>100% Offline by Design</span>
              </div>
              <h3 className="text-xl font-semibold text-ink mb-3">
                Zero Cloud Dependence
              </h3>
              <p className="text-xs text-ink-secondary leading-relaxed mb-6">
                All SQLite database schemas, audio files, 3D simulation shaders, and lesson texts
                are pre-compiled inside the Android APK. You never see a spinner or connection error.
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-4 border-t border-hairline">
              {offlineItems.map((item) => (
                <div key={item} className="flex items-center gap-2 text-xs text-ink">
                  <Check className="w-3.5 h-3.5 text-accent shrink-0" />
                  <span>{item}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Mobile-First Ergonomics */}
          <div className="p-7 rounded-xl border border-hairline bg-white shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-2 text-xs font-mono text-accent uppercase mb-4">
                <Smartphone className="w-4 h-4" />
                <span>Native Android Architecture</span>
              </div>
              <h3 className="text-xl font-semibold text-ink mb-3">
                Crafted for One-Handed Use
              </h3>
              <p className="text-xs text-ink-secondary leading-relaxed mb-6">
                Built with Jetpack Compose, Material 3, and Kotlin coroutines. Swipe gestures,
                bottom sheets, and tactile haptic feedback make studying on the subway effortless.
              </p>
            </div>

            <div className="p-4 rounded-lg bg-surface border border-hairline space-y-2 text-xs text-ink-secondary font-mono">
              <div className="flex justify-between">
                <span>Memory Footprint:</span>
                <span className="text-ink font-semibold">&lt; 85 MB RAM</span>
              </div>
              <div className="flex justify-between">
                <span>Startup Time:</span>
                <span className="text-ink font-semibold">&lt; 350 ms cold start</span>
              </div>
              <div className="flex justify-between">
                <span>Storage Size:</span>
                <span className="text-ink font-semibold">35 MB standalone APK</span>
              </div>
              <div className="flex justify-between">
                <span>Key Security:</span>
                <span className="text-ink font-semibold">AES-256 Android Keystore</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
