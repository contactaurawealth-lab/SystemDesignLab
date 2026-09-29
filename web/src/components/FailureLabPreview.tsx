"use client";

import React, { useState } from "react";
import { AlertCircle, CheckCircle, HelpCircle, ArrowRight, Wrench, ShieldAlert } from "lucide-react";

export default function FailureLabPreview() {
  const [selectedIncident, setSelectedIncident] = useState<number>(0);
  const [diagnosed, setDiagnosed] = useState<boolean>(false);
  const [fixed, setFixed] = useState<boolean>(false);

  const incidents = [
    {
      title: "Incident #402: 8-Second P99 Latency on Checkout",
      symptom: "During flash sale, checkout latency surged from 45ms to 8,200ms. CPU on App Servers is only 18%, but worker threads are blocking.",
      components: [
        { name: "L7 Load Balancer", status: "Healthy", detail: "Passing health checks, 0 queued conns" },
        { name: "App Servers (x4)", status: "Idle/Blocked", detail: "Thread pool exhausted waiting on I/O" },
        { name: "Redis Cache", status: "Healthy", detail: "Session store response time 1.1ms" },
        { name: "PostgreSQL DB", status: "Critical Bottleneck", detail: "Sequential scan on unindexed orders table; lock contention" },
      ],
      rootCause: "A sequential table scan on 42M rows inside a serializable transaction held exclusive row locks.",
      fixAction: "Add composite B-Tree index on (user_id, created_at) and enable connection pooling (PgBouncer).",
    },
    {
      title: "Incident #519: Thundering Herd Cache Collapse",
      symptom: "Celebrity user posts update. At 14:00:00 UTC, the cache key expired. Database CPU spiked instantly to 100%, causing cascading 504 timeouts.",
      components: [
        { name: "CDN Edge", status: "Bypassed", detail: "Cache-Control: max-age=0 forced edge miss" },
        { name: "App Servers", status: "Stressed", detail: "All 12 instances issuing duplicate query" },
        { name: "Redis Cache", status: "Cache Miss", detail: "Key 'user:99182:feed' expired at 14:00:00" },
        { name: "Database", status: "Crashed", detail: "18,000 identical SQL queries executed simultaneously" },
      ],
      rootCause: "Cache stampede: thousands of threads attempted to repopulate the identical missing key concurrently.",
      fixAction: "Implement distributed mutex (SingleFlight pattern) and probabilistic early expiration (XFetch algorithm).",
    },
  ];

  const current = incidents[selectedIncident];

  const handleDiagnose = () => {
    setDiagnosed(true);
  };

  const handleFix = () => {
    setFixed(true);
  };

  const handleReset = (idx: number) => {
    setSelectedIncident(idx);
    setDiagnosed(false);
    setFixed(false);
  };

  return (
    <section className="py-24 border-t border-hairline bg-surface/30">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Failure Lab &middot; Break It &amp; Fix It
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            Break the system. <br />
            <span className="text-ink-secondary font-normal">
              Then learn how to save it.
            </span>
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            Real engineering confidence isn&apos;t built on clean green tests. It is forged
            by diagnosing broken distributed architectures under simulated production fires.
          </p>
        </div>

        {/* Failure Lab Interactive Card */}
        <div className="rounded-xl border border-hairline bg-white shadow-sm overflow-hidden">
          {/* Incident Selector Tabs */}
          <div className="flex border-b border-hairline bg-surface/50 overflow-x-auto">
            {incidents.map((inc, i) => (
              <button
                key={i}
                onClick={() => handleReset(i)}
                className={`px-5 py-3 text-xs font-mono border-r border-hairline text-left whitespace-nowrap transition-colors ${
                  selectedIncident === i
                    ? "bg-white text-ink font-semibold border-b-2 border-b-ink"
                    : "text-ink-muted hover:text-ink hover:bg-surface"
                }`}
              >
                {inc.title.split(":")[0]}
              </button>
            ))}
          </div>

          <div className="p-6 sm:p-8">
            {/* Title & Symptom */}
            <div className="mb-6">
              <div className="flex items-center gap-2 text-rose-600 text-xs font-mono font-semibold uppercase mb-2">
                <AlertCircle className="w-4 h-4" />
                <span>Simulated Production Outage</span>
              </div>
              <h3 className="text-xl font-semibold text-ink mb-2">{current.title}</h3>
              <p className="text-xs sm:text-sm text-ink-secondary bg-surface p-3.5 rounded-lg border border-hairline leading-relaxed">
                <strong className="text-ink font-medium">Symptom Telemetry:</strong> {current.symptom}
              </p>
            </div>

            {/* Architecture Component Inspection */}
            <div className="mb-8">
              <div className="text-xs font-mono uppercase text-ink-muted mb-3">
                Investigate Components (Tap to inspect telemetry):
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
                {current.components.map((comp, idx) => {
                  const isCulprit = comp.status.includes("Bottleneck") || comp.status.includes("Crashed") || comp.status.includes("Cache Miss");
                  return (
                    <div
                      key={idx}
                      className={`p-3.5 rounded-lg border text-left transition-all ${
                        diagnosed && isCulprit
                          ? "border-rose-300 bg-rose-50/60"
                          : "border-hairline bg-surface/40 hover:bg-white"
                      }`}
                    >
                      <div className="text-xs font-semibold text-ink mb-1">{comp.name}</div>
                      <div
                        className={`text-[11px] font-mono mb-2 ${
                          isCulprit ? "text-rose-600 font-semibold" : "text-emerald-600"
                        }`}
                      >
                        {comp.status}
                      </div>
                      <div className="text-[11px] text-ink-secondary leading-snug">
                        {comp.detail}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Interactive Diagnose & Fix Actions */}
            <div className="pt-6 border-t border-hairline flex flex-col sm:flex-row items-center justify-between gap-4">
              {!diagnosed ? (
                <button
                  onClick={handleDiagnose}
                  className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-lg bg-ink text-white text-xs font-semibold hover:bg-black transition-colors"
                >
                  <HelpCircle className="w-3.5 h-3.5" />
                  <span>Diagnose Bottleneck</span>
                </button>
              ) : !fixed ? (
                <div className="w-full flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                  <div className="text-xs text-ink-secondary">
                    <span className="font-semibold text-rose-700">Root Cause Identified:</span>{" "}
                    {current.rootCause}
                  </div>
                  <button
                    onClick={handleFix}
                    className="shrink-0 inline-flex items-center gap-2 px-5 py-2.5 rounded-lg bg-accent text-white text-xs font-semibold hover:bg-accent-hover transition-colors"
                  >
                    <Wrench className="w-3.5 h-3.5" />
                    <span>Apply Architectural Fix</span>
                  </button>
                </div>
              ) : (
                <div className="w-full flex items-center justify-between p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs">
                  <div className="flex items-center gap-2">
                    <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>
                      <strong>System Restored:</strong> {current.fixAction} P99 Latency dropped back to 14ms!
                    </span>
                  </div>
                  <button
                    onClick={() => handleReset((selectedIncident + 1) % incidents.length)}
                    className="text-xs font-mono underline font-medium text-emerald-900 ml-3"
                  >
                    Next Outage &rarr;
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
