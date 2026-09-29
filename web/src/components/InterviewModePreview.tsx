"use client";

import React, { useState } from "react";
import { MessageSquare, ArrowRight, CheckCircle2, User, Bot, Sparkles } from "lucide-react";

export default function InterviewModePreview() {
  const [activeStep, setActiveStep] = useState<number>(0);

  const interviewScript = [
    {
      interviewer: "Let's design a global URL shortener like TinyURL. What are our core scale and storage requirements?",
      userResponse: "Assuming 100M new URLs created per month with a 100:1 read-to-write ratio, that's ~40 writes/sec and ~4,000 reads/sec. Over 5 years, we need to persist ~6 billion records, requiring around 3 TB of database storage.",
      feedback: "Strong estimation. Clear separation of write throughput vs read traffic and long-term capacity calculation.",
    },
    {
      interviewer: "Traffic just increased 10× during a viral campaign. Reads jump to 40,000 req/s. Where is your immediate bottleneck and how do you protect it?",
      userResponse: "The primary database disk I/O will choke under 40k queries/s. We introduce a distributed Redis cluster using consistent hashing. Since URL access follows an 80/20 Pareto distribution, caching the top 20% of hot URLs absorbs 80% of read traffic, keeping DB query volume well below saturation.",
      feedback: "Excellent intuition applying the 80/20 rule to in-memory caching to shield disk persistence.",
    },
    {
      interviewer: "How do you generate unique 7-character short keys without collisions in a multi-region distributed system?",
      userResponse: "Instead of MD5 hash truncation which suffers collisions, we use Base62 encoding over a distributed unique ID generator like Twitter Snowflake (timestamp + datacenter ID + sequence number) or a pre-allocated range counter service (e.g. Zookeeper allocating key blocks to workers).",
      feedback: "Outstanding. Avoided naive random hashes and addressed distributed consensus and coordinate-free generation.",
    },
  ];

  return (
    <section className="py-24 border-t border-hairline bg-surface/30">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Mock Interview Simulator &middot; Natural Engineering Dialogue
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            Practice like a staff engineer.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            No multiple-choice trivia. System Design Lab engages you in progressive,
            probing architectural interviews that test trade-offs, edge cases, and scale.
          </p>
        </div>

        {/* Interview Simulation Box */}
        <div className="rounded-xl border border-hairline bg-white shadow-sm overflow-hidden max-w-4xl mx-auto">
          {/* Interviewer Header */}
          <div className="p-4 border-b border-hairline bg-surface/50 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
              <span className="text-xs font-mono font-semibold text-ink">
                SYSTEM DESIGN INTERVIEW &middot; TINYURL
              </span>
            </div>
            <div className="text-xs font-mono text-ink-muted">
              Question {activeStep + 1} of {interviewScript.length}
            </div>
          </div>

          {/* Conversation Exchange */}
          <div className="p-6 sm:p-8 space-y-6">
            {/* Interviewer Question */}
            <div className="flex items-start gap-3.5">
              <div className="w-8 h-8 rounded-lg bg-surface border border-hairline flex items-center justify-center shrink-0">
                <Bot className="w-4 h-4 text-ink-secondary" />
              </div>
              <div className="flex-1">
                <div className="text-[11px] font-mono text-ink-muted mb-1">INTERVIEWER</div>
                <div className="text-sm font-medium text-ink bg-surface/70 p-4 rounded-xl border border-hairline leading-relaxed">
                  &ldquo;{interviewScript[activeStep].interviewer}&rdquo;
                </div>
              </div>
            </div>

            {/* Candidate Response */}
            <div className="flex items-start gap-3.5">
              <div className="w-8 h-8 rounded-lg bg-ink flex items-center justify-center text-white shrink-0">
                <User className="w-4 h-4" />
              </div>
              <div className="flex-1">
                <div className="text-[11px] font-mono text-ink-muted mb-1">YOUR ARCHITECTURAL PROPOSAL</div>
                <div className="text-sm text-ink-secondary bg-white p-4 rounded-xl border border-hairline leading-relaxed">
                  {interviewScript[activeStep].userResponse}
                </div>
              </div>
            </div>

            {/* AI Staff Reviewer Feedback */}
            <div className="p-3.5 rounded-lg bg-emerald-50/70 border border-emerald-200 text-xs text-emerald-900 flex items-start gap-2.5">
              <Sparkles className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
              <div>
                <strong className="font-semibold text-emerald-950">Interviewer Assessment:</strong>{" "}
                {interviewScript[activeStep].feedback}
              </div>
            </div>
          </div>

          {/* Stepper Controls */}
          <div className="p-4 border-t border-hairline bg-surface/30 flex items-center justify-between">
            <button
              onClick={() => setActiveStep((s) => Math.max(0, s - 1))}
              disabled={activeStep === 0}
              className="text-xs font-mono text-ink-secondary hover:text-ink disabled:opacity-30"
            >
              &larr; Previous Question
            </button>
            <div className="flex gap-1.5">
              {interviewScript.map((_, i) => (
                <span
                  key={i}
                  className={`w-2 h-2 rounded-full transition-all ${
                    activeStep === i ? "bg-ink w-5" : "bg-hairline-dark"
                  }`}
                />
              ))}
            </div>
            <button
              onClick={() => setActiveStep((s) => Math.min(interviewScript.length - 1, s + 1))}
              disabled={activeStep === interviewScript.length - 1}
              className="text-xs font-mono font-medium text-ink hover:text-accent disabled:opacity-30 flex items-center gap-1"
            >
              <span>Next Follow-up</span>
              <span>&rarr;</span>
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
