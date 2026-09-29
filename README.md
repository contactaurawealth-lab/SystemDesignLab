# System Design Lab

A premium, completely offline-first native Android learning academy that transforms Karan Pratap Singh's renowned [system-design](https://github.com/karanpratapsingh/system-design.git) repository into an interactive, visual, and multi-modal engineering platform.

---

## 🌟 Architecture & Core Technology Stack

- **Platform:** Native Android (Min SDK 26 / Android 8.0+, Target SDK 34)
- **Language:** Kotlin 2.0.20
- **UI Framework:** Jetpack Compose with Material 3 Design System
- **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
- **Concurrency & State:** Kotlin Coroutines, StateFlow, Flow
- **Local Persistence:** Room Database (2.6.1) with DAOs & Entity Migrations
- **Preferences:** Jetpack DataStore Preferences
- **3D Simulation Layer:** libGDX (1.12.1) + Real-time Simulation Engine
- **Audiobook Engine:** AndroidX Media3 (1.4.1) ExoPlayer + MediaSessionService (Background Playback)
- **2D Architecture Diagrams:** Jetpack Compose Canvas (Dynamic animated data flows & node inspection)
- **Security:** Hardware-backed Android Keystore with AES-256-GCM encryption for BYOK credentials
- **Networking:** OkHttp 4.12.0 for direct client-to-provider BYOK AI queries

---

## 🚀 Three Primary Learning Modes

1. **📖 LEARN Mode (Interactive Deep-Dive Lessons):**
   - 56 structured lessons extracted and enriched directly from the repository.
   - Organized into 6 modules:
     1. *System Design Foundations*
     2. *Networking & Scalability*
     3. *Databases & Storage*
     4. *Distributed Systems & Messaging*
     5. *Advanced Concepts & Reliability*
     6. *System Design Interviews & Real-World Projects*
   - Every lesson includes the 14 required pedagogical points:
     - Core Problem Statement
     - Simple Intuitive Explanation
     - Technical Deep Dive (progressive disclosure via expandable card)
     - Visual Data-Flow Explanation
     - Real-World Analogy
     - Interactive Architecture Flow Diagram (Canvas)
     - Syntax-Highlighted Code/Config Viewer
     - Repository Reference Links (to README sections & Excalidraw files)
     - Common Mistakes & Traps
     - Architectural Trade-Offs (Latency vs Consistency, etc.)
     - Check-for-Understanding Question (instant feedback)
     - Practical Engineering Exercise (+20 XP)
     - Key Architectural Takeaway
     - Related Concepts & Next Lesson Navigation

2. **🌐 SIMULATE Mode (3D Interactive Architecture Environments):**
   - Interactive simulation scenarios:
     - **Load Balancing:** Overload a single server under 15,000 QPS, witness CPU spike to 100% and latency surge, introduce a Load Balancer, scale up server instances, and watch workload balance across nodes.
     - **Caching (Redis/Memcached):** Compare sub-millisecond RAM cache hits with disk database reads, adjust TTL and cache size.
     - **Database Replication & Failover:** Observe sync vs async replication streams, simulate primary leader crash, resolve split-brain hazard, and promote replicas.
     - **Message Queue & Backpressure:** Buffer bursty producers, scale worker consumer pools, monitor queue depth lag.
     - **Database Sharding:** Horizontally partition petabyte datasets across 4 shards using consistent hash rings.
     - **Chaos & Failure Injection:** Inject latency, kill random nodes, and observe circuit breaker trip states.
     - **Experiment Mode:** Live sliders for Traffic (100 to 100k req/s), Server Count, Failure Rate, Latency, and Cache Size.
     - **Build Mode / Architecture Playground:** Canvas for placing and connecting components with automated architecture viability evaluation.

3. **🎧 LISTEN Mode (Offline Audiobook):**
   - 5 bundled offline audiobook chapters with natural educational narration.
   - Built on Media3 ExoPlayer with `MediaSessionService` for background playback.
   - Speed controls: 0.75x, 1.0x, 1.25x, 1.5x, 1.75x, 2.0x.
   - Sleep timer: 5m, 15m, 30m, End of Chapter.
   - **Audio + Visual Sync:** Highlights the exact concept being discussed in the narration in real-time, with one-tap jump to the corresponding Lesson or Simulation without losing audio playback!

---

## 🧠 Optional BYOK AI Tutor ("Lab Assistant")

- **Completely Optional:** The application functions 100% offline without requiring an account or API key.
- **Provider Architecture:** Modular `AiProvider` interface supporting:
  - Google Gemini API (`gemini-1.5-flash`, `gemini-1.5-pro`)
  - OpenAI-compatible APIs (`gpt-4o-mini`, etc.)
  - OpenRouter (`meta-llama/llama-3.1-8b-instruct`, etc.)
  - Custom Endpoints
  - Extensible `LocalAiProvider` (ready for future on-device local LLM inference)
- **Hardware-Backed Encryption:** Keys are encrypted using Android Keystore AES-256-GCM and stored only in private internal app storage. Keys are never stored in plain text, DataStore, Room, or printed to Logcat.
- **Compact Context Retrieval:** Automatically packages the active lesson, problem statement, trade-offs, and simulation metrics into a compact, token-efficient prompt.
- **Quick Actions:** "Explain simply", "Explain technically", "Real-world analogy", "Give me a hint (no spoilers)", "Quiz me", "Analyze bottlenecks".

---

## 🎮 Gamification & Spaced Repetition

- **🔥 Streaks & Daily Goals:** Daily targets (1 lesson, 1 simulation, 2 exercises -> +50 XP reward) with flame micro-animation.
- **🏆 8 Achievement Badges:** First Lesson, First Simulation, First Architecture, 7-Day Streak, Bug Hunter, Scaling Thinker, Architecture Builder, Interview Ready.
- **📅 Spaced Repetition (SuperMemo SM-2):** Automated review scheduling (Learn today, Review tomorrow, Apply in 3 days, Challenge in 7 days).
- **💾 Data Portability:** Export and import progress backup via portable JSON.

---

## 🛠️ Build and Test

```bash
# Compile and run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Output APK:
# app/build/outputs/apk/debug/app-debug.apk (36.5 MB)
```
