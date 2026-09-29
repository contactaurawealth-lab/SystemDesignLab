"use client";

import React, { useRef, useEffect, useState } from "react";
import { ArrowRight, ArrowDownToLine, Terminal, Activity, Database, Server, Cpu, Globe, Layers } from "lucide-react";

interface NodeItem {
  id: string;
  name: string;
  role: string;
  protocol: string;
  metric: string;
  x: number;
  y: number;
  active: boolean;
}

export default function HeroArchitecture() {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [activeNode, setActiveNode] = useState<string | null>(null);
  const [qps, setQps] = useState(140);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId: number;
    let width = (canvas.width = canvas.parentElement?.clientWidth || 700);
    let height = (canvas.height = 360);

    const handleResize = () => {
      if (!canvas || !canvas.parentElement) return;
      width = canvas.width = canvas.parentElement.clientWidth;
      height = canvas.height = 360;
    };
    window.addEventListener("resize", handleResize);

    // Architectural nodes positioned horizontally across the canvas
    const getNodes = (): NodeItem[] => {
      const padding = 60;
      const availableWidth = width - padding * 2;
      const step = availableWidth / 4;
      const centerY = height / 2;

      return [
        {
          id: "client",
          name: "Client",
          role: "Web / Mobile",
          protocol: "HTTP/3",
          metric: `${qps} req/s`,
          x: padding,
          y: centerY,
          active: false,
        },
        {
          id: "lb",
          name: "Load Balancer",
          role: "L4 / L7 Proxy",
          protocol: "TCP / TLS",
          metric: "Round Robin",
          x: padding + step,
          y: centerY,
          active: false,
        },
        {
          id: "server",
          name: "App Server",
          role: "Stateless Worker",
          protocol: "gRPC",
          metric: "4.2 ms avg",
          x: padding + step * 2,
          y: centerY,
          active: false,
        },
        {
          id: "cache",
          name: "Cache",
          role: "In-Memory Store",
          protocol: "RESP / Redis",
          metric: "94% hit rate",
          x: padding + step * 3,
          y: centerY,
          active: false,
        },
        {
          id: "db",
          name: "Database",
          role: "ACID Primary",
          protocol: "PostgreSQL",
          metric: "Write-Ahead Log",
          x: padding + step * 4,
          y: centerY,
          active: false,
        },
      ];
    };

    // Request particles
    const particles: Array<{
      segment: number;
      progress: number;
      speed: number;
      isCacheHit: boolean;
      yOffset: number;
    }> = [];

    for (let i = 0; i < 22; i++) {
      particles.push({
        segment: Math.floor(Math.random() * 4),
        progress: Math.random(),
        speed: 0.006 + Math.random() * 0.005,
        isCacheHit: Math.random() > 0.35,
        yOffset: (Math.random() - 0.5) * 6,
      });
    }

    let mouseX = -1000;
    let mouseY = -1000;

    const onMouseMove = (e: MouseEvent) => {
      const rect = canvas.getBoundingClientRect();
      mouseX = e.clientX - rect.left;
      mouseY = e.clientY - rect.top;
    };

    const onMouseLeave = () => {
      mouseX = -1000;
      mouseY = -1000;
      setActiveNode(null);
    };

    canvas.addEventListener("mousemove", onMouseMove);
    canvas.addEventListener("mouseleave", onMouseLeave);

    const render = () => {
      ctx.clearRect(0, 0, width, height);
      const nodes = getNodes();

      // Check hovered node
      let hovered: string | null = null;
      nodes.forEach((node) => {
        const dx = mouseX - node.x;
        const dy = mouseY - node.y;
        if (Math.sqrt(dx * dx + dy * dy) < 40) {
          hovered = node.id;
        }
      });
      setActiveNode(hovered);

      // Draw connection lines
      ctx.lineWidth = 1.5;
      for (let i = 0; i < nodes.length - 1; i++) {
        const a = nodes[i];
        const b = nodes[i + 1];

        // Base hairline wire
        ctx.beginPath();
        ctx.strokeStyle = "#E5E7EB";
        ctx.setLineDash([4, 4]);
        ctx.moveTo(a.x, a.y);
        ctx.lineTo(b.x, b.y);
        ctx.stroke();

        // Active connection highlight when mouse is nearby
        const midX = (a.x + b.x) / 2;
        const dist = Math.abs(mouseX - midX);
        if (dist < 100) {
          ctx.beginPath();
          ctx.strokeStyle = "rgba(37, 99, 235, 0.4)";
          ctx.setLineDash([]);
          ctx.moveTo(a.x, a.y);
          ctx.lineTo(b.x, b.y);
          ctx.stroke();
        }
      }
      ctx.setLineDash([]);

      // Draw animated request particles
      particles.forEach((p) => {
        p.progress += p.speed;
        if (p.progress >= 1) {
          p.progress = 0;
          p.segment = (p.segment + 1) % 4;
          // If server -> cache segment ends and it's a hit, return back or skip DB
          if (p.segment === 3 && p.isCacheHit) {
            p.segment = 0; // return to client loop
          }
        }

        const startNode = nodes[p.segment];
        const endNode = nodes[p.segment + 1];
        if (!startNode || !endNode) return;

        const currentX = startNode.x + (endNode.x - startNode.x) * p.progress;
        const currentY = startNode.y + (endNode.y - startNode.y) * p.progress + p.yOffset;

        ctx.beginPath();
        ctx.arc(currentX, currentY, 2.5, 0, Math.PI * 2);
        ctx.fillStyle = p.segment === 2 && p.isCacheHit ? "#10B981" : "#2563EB";
        ctx.fill();

        // Subtle glow on particle
        ctx.beginPath();
        ctx.arc(currentX, currentY, 5, 0, Math.PI * 2);
        ctx.fillStyle = p.segment === 2 && p.isCacheHit ? "rgba(16, 185, 129, 0.15)" : "rgba(37, 99, 235, 0.15)";
        ctx.fill();
      });

      // Draw nodes
      nodes.forEach((node) => {
        const isHovered = hovered === node.id;
        const radius = isHovered ? 26 : 22;

        // Outer halo
        ctx.beginPath();
        ctx.arc(node.x, node.y, radius + 8, 0, Math.PI * 2);
        ctx.fillStyle = isHovered ? "rgba(37, 99, 235, 0.08)" : "rgba(248, 250, 252, 0.5)";
        ctx.fill();

        // Node circle
        ctx.beginPath();
        ctx.arc(node.x, node.y, radius, 0, Math.PI * 2);
        ctx.fillStyle = isHovered ? "#FFFFFF" : "#FFFFFF";
        ctx.fill();
        ctx.lineWidth = isHovered ? 2 : 1.2;
        ctx.strokeStyle = isHovered ? "#2563EB" : "#D1D5DB";
        ctx.stroke();

        // Inner core
        ctx.beginPath();
        ctx.arc(node.x, node.y, 4, 0, Math.PI * 2);
        ctx.fillStyle = isHovered ? "#2563EB" : "#475569";
        ctx.fill();

        // Node Title
        ctx.font = "600 12.5px Inter, -apple-system, sans-serif";
        ctx.fillStyle = "#111111";
        ctx.textAlign = "center";
        ctx.fillText(node.name, node.x, node.y - 36);

        // Subtitle / Role
        ctx.font = "400 10.5px Inter, -apple-system, sans-serif";
        ctx.fillStyle = "#64748B";
        ctx.fillText(node.role, node.x, node.y - 22);

        // Protocol badge below
        ctx.font = "500 10px JetBrains Mono, monospace";
        ctx.fillStyle = isHovered ? "#2563EB" : "#94A3B8";
        ctx.fillText(node.protocol, node.x, node.y + 40);

        // Live Metric below badge
        ctx.font = "400 9.5px Inter, -apple-system, sans-serif";
        ctx.fillStyle = "#475569";
        ctx.fillText(node.metric, node.x, node.y + 54);
      });

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener("resize", handleResize);
      canvas.removeEventListener("mousemove", onMouseMove);
      canvas.removeEventListener("mouseleave", onMouseLeave);
    };
  }, [qps]);

  return (
    <section className="relative pt-32 pb-20 md:pt-40 md:pb-28 overflow-hidden bg-radial-atmosphere">
      {/* Background architectural grid */}
      <div className="absolute inset-0 bg-grid-pattern opacity-60 pointer-events-none" />

      <div className="max-w-6xl mx-auto px-6 sm:px-8 relative z-10 text-center">
        {/* Subtle pill */}
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-hairline bg-surface/80 text-[12px] text-ink-secondary mb-8">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
          <span className="font-mono">karanpratapsingh/system-design</span>
          <span className="text-hairline-dark">|</span>
          <span>56 Lessons &middot; Offline 3D Engine &middot; Realtime Lab</span>
        </div>

        {/* Hero headline - editorial, confident */}
        <h1 className="text-4xl sm:text-6xl md:text-7xl font-semibold tracking-tightest text-ink max-w-4xl mx-auto leading-[1.08] mb-6">
          Understand systems. <br />
          <span className="text-ink-secondary font-normal">
            Don&apos;t just memorize them.
          </span>
        </h1>

        {/* Hero subtext */}
        <p className="text-base sm:text-lg md:text-xl text-ink-secondary max-w-2xl mx-auto font-normal leading-relaxed mb-10">
          An offline-first interactive learning environment for mastering system
          design through lessons, 3D simulations, failure labs, and practice.
        </p>

        {/* Hero CTAs */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5 mb-16">
          <a
            href="/prototype.html"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 text-[14.5px] font-medium text-white bg-blue-600 hover:bg-blue-700 px-6 py-3 rounded-lg transition-all shadow-sm hover:shadow-md group"
          >
            <span>Launch Live Prototype</span>
            <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
          </a>
          <a
            href="/downloads/system-design-lab.apk"
            download="system-design-lab.apk"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 text-[14.5px] font-medium text-ink bg-white hover:bg-surface border border-hairline px-6 py-3 rounded-lg transition-all"
          >
            <ArrowDownToLine className="w-4 h-4 text-ink-secondary" />
            <span>Download Android App</span>
            <span className="text-xs text-ink-muted font-mono">(36.6 MB APK)</span>
          </a>
          <a
            href="#simulate"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 text-[14.5px] font-medium text-ink-secondary hover:text-ink px-4 py-3 rounded-lg transition-all"
          >
            <span>Explore Tour</span>
          </a>
        </div>

        {/* Interactive Living Architecture Canvas */}
        <div
          ref={containerRef}
          className="relative max-w-4xl mx-auto rounded-xl border border-hairline bg-white/70 backdrop-blur-sm p-4 sm:p-6 shadow-[0_4px_24px_rgba(0,0,0,0.02)]"
        >
          {/* Canvas header status */}
          <div className="flex items-center justify-between border-b border-hairline/80 pb-3 mb-4 text-xs font-mono text-ink-secondary">
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-500" />
              <span className="text-ink font-medium">LIVE TOPOLOGY</span>
              <span className="text-ink-muted hidden sm:inline">&middot; Move cursor to inspect nodes &amp; trace flows</span>
            </div>
            <div className="flex items-center gap-3">
              <span className="hidden sm:inline text-ink-muted">Throughput:</span>
              <span className="bg-surface px-2 py-0.5 rounded border border-hairline font-semibold text-ink">
                {qps} req/s
              </span>
            </div>
          </div>

          {/* Desktop Canvas (Interactive living system) */}
          <div className="hidden sm:block w-full overflow-hidden">
            <canvas
              ref={canvasRef}
              className="w-full h-[360px] cursor-crosshair"
            />
          </div>

          {/* Mobile Simplified Architecture (Vertical stream) */}
          <div className="sm:hidden space-y-2 py-2">
            {[
              { title: "Client", role: "Mobile / Browser", icon: Globe, metric: "140 req/s", tech: "HTTP/3" },
              { title: "Load Balancer", role: "Traffic Distribution", icon: Layers, metric: "Round Robin", tech: "L7 Reverse Proxy" },
              { title: "App Server", role: "Stateless Cluster", icon: Server, metric: "4.2 ms avg", tech: "gRPC" },
              { title: "Cache", role: "In-Memory Buffer", icon: Cpu, metric: "94% hit rate", tech: "Redis LRU" },
              { title: "Database", role: "Primary Datastore", icon: Database, metric: "ACID Write-Ahead", tech: "PostgreSQL" },
            ].map((node, i) => (
              <div key={node.title} className="flex flex-col items-center">
                <div className="w-full flex items-center justify-between p-3 rounded-lg border border-hairline bg-surface/50 text-left">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-md bg-white border border-hairline flex items-center justify-center text-ink-secondary">
                      <node.icon className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-[13px] font-semibold text-ink">{node.title}</div>
                      <div className="text-[11px] text-ink-secondary">{node.role}</div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-[11px] font-mono text-accent font-medium">{node.tech}</div>
                    <div className="text-[10px] text-ink-muted">{node.metric}</div>
                  </div>
                </div>
                {i < 4 && (
                  <div className="h-4 w-[1px] bg-hairline my-0.5 flex items-center justify-center">
                    <span className="w-1.5 h-1.5 rounded-full bg-accent/60" />
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Footer note inside canvas box */}
          <div className="mt-4 pt-3 border-t border-hairline flex flex-col sm:flex-row items-center justify-between text-[11.5px] text-ink-muted font-mono">
            <span>DISCOVER &rarr; LEARN &rarr; SEE &rarr; SIMULATE &rarr; BREAK &rarr; FIX</span>
            <span className="mt-1 sm:mt-0 text-ink-secondary">Zero cloud dependency &middot; 100% offline</span>
          </div>
        </div>
      </div>
    </section>
  );
}
