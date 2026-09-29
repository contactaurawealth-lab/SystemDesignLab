"use client";

import React, { useState, useEffect, useRef } from "react";
import { Plus, Minus, AlertTriangle, Play, RefreshCw, Cpu, Activity, Zap, Server, Database, ShieldAlert, Check } from "lucide-react";

export default function SimulationCenterpiece() {
  const [traffic, setTraffic] = useState<number>(1000); // 100 to 10000 req/s
  const [serverCount, setServerCount] = useState<number>(3); // 1 to 6
  const [cacheDegraded, setCacheDegraded] = useState<boolean>(false);
  const [activeTab, setActiveTab] = useState<"architecture" | "telemetry">("architecture");
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  // Calculate live engineering metrics based on traffic, server count, and cache health
  const capacityPerServer = 1800; // req/s per server before saturation
  const totalCapacity = serverCount * capacityPerServer;
  const loadRatio = traffic / totalCapacity;
  const cpuLoad = Math.min(100, Math.round(loadRatio * 85 + (cacheDegraded ? 25 : 0)));
  
  // Latency calculation (ms)
  let latency = 4.2;
  if (cacheDegraded) latency += 45;
  if (cpuLoad > 80) {
    latency += Math.pow((cpuLoad - 80) / 2, 2.2);
  }
  const formattedLatency = Math.min(999, Math.round(latency * 10) / 10);

  // Error rate calculation (%)
  let errorRate = 0.0;
  if (cpuLoad > 92) {
    errorRate = Math.min(15.4, Math.round((cpuLoad - 90) * 1.8 * 10) / 10);
  }

  // Cache hit rate
  const cacheHitRate = cacheDegraded ? 12 : 94;

  // Canvas packet animation
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animId: number;
    let width = (canvas.width = canvas.parentElement?.clientWidth || 800);
    let height = (canvas.height = 340);

    const onResize = () => {
      if (!canvas.parentElement) return;
      width = canvas.width = canvas.parentElement.clientWidth;
      height = canvas.height = 340;
    };
    window.addEventListener("resize", onResize);

    // Particle pool
    const particleCount = Math.min(50, Math.max(12, Math.floor(traffic / 200)));
    const particles = Array.from({ length: particleCount }, () => ({
      x: 0,
      y: 0,
      stage: Math.floor(Math.random() * 4),
      progress: Math.random(),
      speed: 0.008 + (traffic / 10000) * 0.015,
      targetServerIdx: Math.floor(Math.random() * serverCount),
      dropped: false,
    }));

    const render = () => {
      ctx.clearRect(0, 0, width, height);

      // Node X coordinates
      const xClient = 60;
      const xLB = width * 0.28;
      const xServers = width * 0.52;
      const xCache = width * 0.74;
      const xDB = width * 0.90;

      const centerY = height / 2;

      // Draw connection lines
      ctx.lineWidth = 1;
      ctx.setLineDash([3, 3]);
      ctx.strokeStyle = "#E2E8F0";

      // Client to LB
      ctx.beginPath();
      ctx.moveTo(xClient, centerY);
      ctx.lineTo(xLB, centerY);
      ctx.stroke();

      // LB to each server
      for (let i = 0; i < serverCount; i++) {
        const serverY = centerY + (i - (serverCount - 1) / 2) * 44;
        ctx.beginPath();
        ctx.moveTo(xLB, centerY);
        ctx.lineTo(xServers, serverY);
        ctx.stroke();

        // Server to Cache
        ctx.beginPath();
        ctx.moveTo(xServers, serverY);
        ctx.lineTo(xCache, centerY - 28);
        ctx.stroke();

        // Server to DB (direct if cache degraded or writes)
        ctx.beginPath();
        ctx.moveTo(xServers, serverY);
        ctx.lineTo(xDB, centerY + 28);
        ctx.stroke();
      }

      // Cache to DB
      ctx.beginPath();
      ctx.moveTo(xCache, centerY - 28);
      ctx.lineTo(xDB, centerY + 28);
      ctx.stroke();

      ctx.setLineDash([]);

      // Animate request particles
      particles.forEach((p) => {
        p.speed = 0.008 + (traffic / 10000) * 0.018;
        p.progress += p.speed;

        if (p.progress >= 1) {
          p.progress = 0;
          p.stage = (p.stage + 1) % 4;
          p.targetServerIdx = Math.floor(Math.random() * serverCount);
          p.dropped = errorRate > 0 && Math.random() < errorRate / 100;
        }

        const serverY = centerY + (p.targetServerIdx - (serverCount - 1) / 2) * 44;
        let startX = xClient,
          startY = centerY,
          endX = xLB,
          endY = centerY;

        if (p.stage === 0) {
          startX = xClient;
          startY = centerY;
          endX = xLB;
          endY = centerY;
        } else if (p.stage === 1) {
          startX = xLB;
          startY = centerY;
          endX = xServers;
          endY = serverY;
        } else if (p.stage === 2) {
          startX = xServers;
          startY = serverY;
          endX = xCache;
          endY = centerY - 28;
        } else if (p.stage === 3) {
          startX = xCache;
          startY = centerY - 28;
          endX = xDB;
          endY = centerY + 28;
        }

        const curX = startX + (endX - startX) * p.progress;
        const curY = startY + (endY - startY) * p.progress;

        ctx.beginPath();
        ctx.arc(curX, curY, p.dropped ? 3 : 2.5, 0, Math.PI * 2);
        if (p.dropped) {
          ctx.fillStyle = "#EF4444"; // Dropped packet
        } else if (cpuLoad > 85) {
          ctx.fillStyle = "#F59E0B"; // Stressed packet
        } else {
          ctx.fillStyle = "#2563EB"; // Healthy packet
        }
        ctx.fill();
      });

      // Render Static Component Blocks
      const drawBox = (x: number, y: number, label: string, sub: string, color: string = "#111111") => {
        const w = 72;
        const h = 40;
        ctx.fillStyle = "#FFFFFF";
        ctx.strokeStyle = color === "#EF4444" ? "#FCA5A5" : "#E2E8F0";
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.roundRect(x - w / 2, y - h / 2, w, h, 6);
        ctx.fill();
        ctx.stroke();

        ctx.fillStyle = color;
        ctx.font = "600 11px Inter, sans-serif";
        ctx.textAlign = "center";
        ctx.fillText(label, x, y - 2);

        ctx.fillStyle = "#64748B";
        ctx.font = "400 9px Inter, sans-serif";
        ctx.fillText(sub, x, y + 11);
      };

      // Client
      drawBox(xClient, centerY, "Clients", `${traffic} QPS`);

      // Load Balancer
      drawBox(xLB, centerY, "HAProxy", "L7 Layer");

      // App Servers
      for (let i = 0; i < serverCount; i++) {
        const serverY = centerY + (i - (serverCount - 1) / 2) * 44;
        const serverColor = cpuLoad > 90 ? "#EF4444" : cpuLoad > 75 ? "#D97706" : "#111111";
        drawBox(xServers, serverY, `Node 0${i + 1}`, `${Math.round(cpuLoad)}% CPU`, serverColor);
      }

      // Cache
      drawBox(
        xCache,
        centerY - 28,
        "Redis",
        cacheDegraded ? "Cache Miss!" : "94% Hit",
        cacheDegraded ? "#EF4444" : "#10B981"
      );

      // Database
      drawBox(xDB, centerY + 28, "PostgreSQL", "Primary ACID");

      animId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener("resize", onResize);
    };
  }, [traffic, serverCount, cacheDegraded, cpuLoad, errorRate]);

  return (
    <section id="simulate" className="py-24 border-t border-hairline bg-surface/40 relative">
      <div className="max-w-6xl mx-auto px-6 sm:px-8">
        {/* Section Header */}
        <div className="max-w-3xl mb-12">
          <div className="text-[12px] font-mono text-accent uppercase tracking-wider mb-3">
            Realtime 3D &middot; Architecture Simulator
          </div>
          <h2 className="text-3xl sm:text-5xl font-semibold tracking-tight text-ink mb-4">
            See what the system is doing.
          </h2>
          <p className="text-base sm:text-lg text-ink-secondary font-normal leading-relaxed">
            &ldquo;System design becomes intuitive when you can see the consequences.&rdquo;{" "}
            Test real production dynamics: ramp traffic, saturate memory, introduce
            server crashes, and watch latency and error rates react dynamically.
          </p>
        </div>

        {/* Live Simulation Control Dashboard */}
        <div className="rounded-xl border border-hairline bg-white shadow-sm overflow-hidden mb-8">
          {/* Top Control Bar */}
          <div className="p-4 sm:p-5 border-b border-hairline bg-surface/50 flex flex-wrap items-center justify-between gap-4">
            {/* Left: Traffic Presets */}
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono text-ink-secondary mr-2">TRAFFIC LOAD:</span>
              {[100, 1000, 5000, 10000].map((t) => (
                <button
                  key={t}
                  onClick={() => setTraffic(t)}
                  className={`text-xs font-mono px-2.5 py-1 rounded transition-all ${
                    traffic === t
                      ? "bg-ink text-white font-semibold shadow-sm"
                      : "bg-white border border-hairline text-ink-secondary hover:text-ink"
                  }`}
                >
                  {t.toLocaleString()} req/s
                </button>
              ))}
            </div>

            {/* Right: Server Pool Adjustments */}
            <div className="flex items-center gap-3">
              <span className="text-xs font-mono text-ink-secondary">NODES:</span>
              <div className="flex items-center gap-1 border border-hairline bg-white rounded-lg p-0.5">
                <button
                  onClick={() => setServerCount((c) => Math.max(1, c - 1))}
                  disabled={serverCount <= 1}
                  className="w-7 h-7 flex items-center justify-center rounded hover:bg-surface text-ink-secondary disabled:opacity-30"
                  aria-label="Remove server"
                >
                  <Minus className="w-3.5 h-3.5" />
                </button>
                <span className="w-8 text-center text-xs font-mono font-semibold text-ink">
                  {serverCount}
                </span>
                <button
                  onClick={() => setServerCount((c) => Math.min(6, c + 1))}
                  disabled={serverCount >= 6}
                  className="w-7 h-7 flex items-center justify-center rounded hover:bg-surface text-ink-secondary disabled:opacity-30"
                  aria-label="Add server"
                >
                  <Plus className="w-3.5 h-3.5" />
                </button>
              </div>

              {/* Degrade Cache toggle */}
              <button
                onClick={() => setCacheDegraded(!cacheDegraded)}
                className={`text-xs font-mono px-3 py-1.5 rounded-lg border transition-all flex items-center gap-1.5 ${
                  cacheDegraded
                    ? "bg-rose-50 border-rose-300 text-rose-700 font-semibold"
                    : "bg-white border-hairline text-ink-secondary hover:text-ink"
                }`}
              >
                <AlertTriangle className="w-3 h-3" />
                <span>{cacheDegraded ? "Cache Degraded" : "Simulate Cache Eviction"}</span>
              </button>
            </div>
          </div>

          {/* Interactive Canvas Rendering */}
          <div className="relative p-2 sm:p-6 bg-white min-h-[340px] flex items-center justify-center">
            <canvas ref={canvasRef} className="w-full h-[340px]" />
          </div>

          {/* Live Telemetry Footer */}
          <div className="grid grid-cols-2 sm:grid-cols-4 border-t border-hairline bg-surface/30 divide-x divide-hairline">
            <div className="p-4 text-center">
              <div className="text-[11px] font-mono text-ink-muted uppercase">Avg Latency (p99)</div>
              <div
                className={`text-xl font-mono font-semibold mt-1 ${
                  formattedLatency > 150
                    ? "text-rose-600"
                    : formattedLatency > 50
                    ? "text-amber-600"
                    : "text-ink"
                }`}
              >
                {formattedLatency} ms
              </div>
              <div className="text-[10px] text-ink-muted mt-0.5">SLA: &lt; 50 ms</div>
            </div>

            <div className="p-4 text-center">
              <div className="text-[11px] font-mono text-ink-muted uppercase">Cluster CPU</div>
              <div
                className={`text-xl font-mono font-semibold mt-1 ${
                  cpuLoad > 85 ? "text-rose-600" : cpuLoad > 70 ? "text-amber-600" : "text-ink"
                }`}
              >
                {cpuLoad}%
              </div>
              <div className="text-[10px] text-ink-muted mt-0.5">
                {serverCount} nodes &times; {capacityPerServer} QPS
              </div>
            </div>

            <div className="p-4 text-center">
              <div className="text-[11px] font-mono text-ink-muted uppercase">Error Rate (5xx)</div>
              <div
                className={`text-xl font-mono font-semibold mt-1 ${
                  errorRate > 0 ? "text-rose-600" : "text-emerald-600"
                }`}
              >
                {errorRate > 0 ? `${errorRate}%` : "0.00%"}
              </div>
              <div className="text-[10px] text-ink-muted mt-0.5">
                {errorRate > 0 ? "Dropping packets" : "All 200 OK"}
              </div>
            </div>

            <div className="p-4 text-center">
              <div className="text-[11px] font-mono text-ink-muted uppercase">Cache Hit Rate</div>
              <div
                className={`text-xl font-mono font-semibold mt-1 ${
                  cacheHitRate < 50 ? "text-rose-600" : "text-emerald-600"
                }`}
              >
                {cacheHitRate}%
              </div>
              <div className="text-[10px] text-ink-muted mt-0.5">
                {cacheDegraded ? "Thundering herd to DB" : "In-Memory LRU"}
              </div>
            </div>
          </div>
        </div>

        {/* Observation callout */}
        <div className="p-4 rounded-lg border border-hairline bg-white flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3">
            <div className="w-2 h-2 rounded-full bg-accent mt-1.5 shrink-0" />
            <div className="text-xs text-ink-secondary">
              <span className="font-semibold text-ink">Architectural Observation:</span>{" "}
              {cpuLoad > 85
                ? `At ${traffic.toLocaleString()} req/s with only ${serverCount} server(s), the worker tier is oversaturated. Click "Add Server" to scale horizontally and restore sub-10ms response times.`
                : cacheDegraded
                ? "With cache misses elevated, read traffic spills directly onto PostgreSQL disk I/O, driving up latency even under normal QPS."
                : "System is operating within optimal headroom. Requests distribute evenly across workers, and 94% of reads resolve directly from Redis."}
            </div>
          </div>

          <a
            href="/downloads/system-design-lab.apk"
            download="system-design-lab.apk"
            className="shrink-0 text-xs font-medium text-ink hover:text-black flex items-center gap-1.5 underline underline-offset-4"
          >
            <span>Run 3D Engine in App</span>
            <span className="font-mono text-ink-muted">&rarr;</span>
          </a>
        </div>
      </div>
    </section>
  );
}
