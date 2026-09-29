"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { ArrowDownToLine, Github, Menu, X } from "lucide-react";

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled
          ? "bg-white/85 backdrop-blur-md border-b border-hairline/80 py-3 shadow-[0_1px_2px_rgba(0,0,0,0.02)]"
          : "bg-transparent py-5"
      }`}
    >
      <div className="max-w-7xl mx-auto px-6 sm:px-8 flex items-center justify-between">
        {/* Left: Brand */}
        <Link href="/" className="flex items-center gap-2.5 group">
          <span className="w-2.5 h-2.5 rounded-full bg-ink group-hover:bg-accent transition-colors" />
          <span className="text-[15px] font-semibold tracking-tight text-ink">
            System Design Lab
          </span>
          <span className="hidden sm:inline-block text-[11px] font-mono uppercase tracking-wider text-ink-muted bg-surface px-1.5 py-0.5 rounded border border-hairline">
            Offline Edition
          </span>
        </Link>

        {/* Center: Navigation */}
        <nav className="hidden md:flex items-center gap-7 text-[13.5px] font-medium text-ink-secondary">
          <a
            href="#learn"
            className="hover:text-ink transition-colors tracking-tight"
          >
            Learn
          </a>
          <a
            href="#simulate"
            className="hover:text-ink transition-colors tracking-tight"
          >
            Simulate
          </a>
          <a
            href="#playground"
            className="hover:text-ink transition-colors tracking-tight"
          >
            Playground
          </a>
          <a
            href="#listen"
            className="hover:text-ink transition-colors tracking-tight"
          >
            Listen
          </a>
          <a
            href="#journey"
            className="hover:text-ink transition-colors tracking-tight"
          >
            Journey
          </a>
          <a
            href="#assistant"
            className="hover:text-ink transition-colors tracking-tight"
          >
            Assistant
          </a>
        </nav>

        {/* Right: Actions */}
        <div className="hidden sm:flex items-center gap-3">
          <a
            href="https://github.com/karanpratapsingh/system-design"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 text-[13px] text-ink-secondary hover:text-ink px-3 py-1.5 rounded-md hover:bg-surface transition-colors"
          >
            <Github className="w-3.5 h-3.5" />
            <span>GitHub</span>
          </a>
          <a
            href="/downloads/system-design-lab.apk"
            download="system-design-lab.apk"
            className="inline-flex items-center gap-1.5 text-[13px] font-medium text-white bg-ink hover:bg-black px-3.5 py-1.5 rounded-md transition-colors shadow-sm"
          >
            <ArrowDownToLine className="w-3.5 h-3.5" />
            <span>Download APK</span>
          </a>
        </div>

        {/* Mobile menu button */}
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="md:hidden text-ink-secondary hover:text-ink p-1"
          aria-label="Toggle menu"
        >
          {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
        </button>
      </div>

      {/* Mobile drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-white/95 backdrop-blur-md border-b border-hairline px-6 py-5 space-y-4">
          <nav className="flex flex-col space-y-3 text-[14px] text-ink-secondary font-medium">
            <a
              href="#learn"
              onClick={() => setMobileMenuOpen(false)}
              className="hover:text-ink py-1"
            >
              Learn
            </a>
            <a
              href="#simulate"
              onClick={() => setMobileMenuOpen(false)}
              className="hover:text-ink py-1"
            >
              Simulate
            </a>
            <a
              href="#playground"
              onClick={() => setMobileMenuOpen(false)}
              className="hover:text-ink py-1"
            >
              Playground
            </a>
            <a
              href="#listen"
              onClick={() => setMobileMenuOpen(false)}
              className="hover:text-ink py-1"
            >
              Listen
            </a>
            <a
              href="#journey"
              onClick={() => setMobileMenuOpen(false)}
              className="hover:text-ink py-1"
            >
              Journey
            </a>
            <a
              href="#assistant"
              onClick={() => setMobileMenuOpen(false)}
              className="hover:text-ink py-1"
            >
              Assistant
            </a>
          </nav>
          <div className="pt-3 border-t border-hairline flex flex-col gap-2.5">
            <a
              href="https://github.com/karanpratapsingh/system-design"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center justify-center gap-1.5 text-[13px] text-ink-secondary py-2 rounded border border-hairline hover:bg-surface"
            >
              <Github className="w-3.5 h-3.5" />
              <span>karanpratapsingh/system-design</span>
            </a>
            <a
              href="/downloads/system-design-lab.apk"
              download="system-design-lab.apk"
              className="flex items-center justify-center gap-2 text-[13.5px] font-medium text-white bg-ink py-2.5 rounded shadow-sm"
            >
              <ArrowDownToLine className="w-4 h-4" />
              <span>Download Android APK (v1.0)</span>
            </a>
          </div>
        </div>
      )}
    </header>
  );
}
