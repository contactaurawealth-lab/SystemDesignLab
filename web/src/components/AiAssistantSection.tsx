"use client";

import React, { useState } from "react";
import { Key, Shield, Sparkles, Send, Bot, CheckCircle } from "lucide-react";

export default function AiAssistantSection() {
  const [selectedPrompt, setSelectedPrompt] = useState<string>("Review my architecture");

  const prompts = [
    {
      action: "Review my architecture",
      query: "Review my 3-tier setup with 2 Redis shards and single Postgres primary. Where will it break first?",
      response:
        "Your single Postgres primary is the immediate Single Point of Failure (SPOF). At 15,000 writes/sec, replication lag will spike and disk IOPS will saturate. Recommend: Add an async WAL streaming read replica and configure connection pooling (PgBouncer) before sharding tables.",
    },
    {
      action: "Explain with real-world analogy",
      query: "Give me an intuitive analogy for Two-Phase Commit (2PC).",
      response:
        "Think of a group booking an international flight with a hotel: The coordinator asks everyone 'Can you guarantee this reservation?' (Prepare Phase). Only when every service confirms inventory does the coordinator shout 'Charge all cards now' (Commit Phase). If one hotel is booked out, all reservations abort.",
    },
    {
      action: "Find my mistake",
      query: "Why did my cache-aside logic cause stale data during user profile updates?",
      response:
        "You updated the database and then wrote the updated object directly to cache. If another concurrent worker read the old DB state during the update window, it overwrites your cache with stale data. Fix: Invalidate (delete) the cache key instead of updating it, forcing the next read to fetch fresh DB data.",
    },
  ];

  const current = prompts.find((p) => p.action === selectedPrompt) || prompts[0];

  return (
    <section id="assistant" className="py-24 border-t border-hairline bg-surface/30">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            BYOK AI Tutor &middot; Bring Your Own Key
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            A staff architect in your pocket.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Zero subscription lock-in. Provide your own OpenAI, Anthropic, or Gemini API key.
            Your keys are encrypted via Android Keystore and never touch our servers.
          </p>
        </div>

        {/* AI Assistant Interactive Preview */}
        <div className="rounded-xl border border-hairline bg-white shadow-sm overflow-hidden max-w-4xl mx-auto">
          {/* Top Bar with Privacy & BYOK guarantee */}
          <div className="p-4 border-b border-hairline bg-surface/50 flex flex-wrap items-center justify-between gap-3 text-xs font-mono">
            <div className="flex items-center gap-2 text-ink">
              <Key className="w-3.5 h-3.5 text-accent" />
              <span>BYOK: OpenAI / Anthropic / Gemini</span>
            </div>
            <div className="flex items-center gap-2 text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded border border-emerald-200">
              <Shield className="w-3.5 h-3.5" />
              <span>Hardware-Encrypted via Android Keystore</span>
            </div>
          </div>

          {/* Prompt Selector Pills */}
          <div className="p-4 border-b border-hairline flex flex-wrap gap-2 bg-surface/20">
            {prompts.map((p) => (
              <button
                key={p.action}
                onClick={() => setSelectedPrompt(p.action)}
                className={`text-xs px-3 py-1.5 rounded-lg border transition-all ${
                  selectedPrompt === p.action
                    ? "bg-ink text-white font-medium shadow-sm"
                    : "bg-white border-hairline text-ink-secondary hover:text-ink"
                }`}
              >
                {p.action}
              </button>
            ))}
          </div>

          {/* Simulated Chat Dialogue */}
          <div className="p-6 sm:p-8 space-y-4">
            {/* User message */}
            <div className="flex justify-end">
              <div className="max-w-xl bg-surface border border-hairline text-ink text-xs sm:text-sm p-4 rounded-xl rounded-tr-sm leading-relaxed">
                {current.query}
              </div>
            </div>

            {/* AI Assistant response */}
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-lg bg-accent/10 border border-accent/20 flex items-center justify-center shrink-0">
                <Sparkles className="w-4 h-4 text-accent" />
              </div>
              <div className="max-w-2xl bg-white border border-hairline text-ink-secondary text-xs sm:text-sm p-4 rounded-xl rounded-tl-sm leading-relaxed shadow-sm">
                <div className="font-semibold text-ink text-xs mb-1.5 font-mono">SYSTEM DESIGN TUTOR:</div>
                {current.response}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
