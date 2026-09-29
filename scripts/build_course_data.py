#!/usr/bin/env python3
import json
import os
import re
import shutil

REPO_DIR = '/root/system-design-repo'
README_PATH = os.path.join(REPO_DIR, 'README.md')
DIAGRAMS_DIR = os.path.join(REPO_DIR, 'diagrams')
ASSETS_DIR = '/root/SystemDesignLab/app/src/main/assets/course'

def slugify(text):
    text = text.lower().strip()
    text = re.sub(r'[^\w\s-]', '', text)
    text = re.sub(r'[\s_-]+', '-', text)
    text = re.sub(r'^-+|-+$', '', text)
    return text

def parse_readme():
    with open(README_PATH, 'r', encoding='utf-8') as f:
        content = f.read()

    # Split into sections based on "# "
    raw_sections = re.split(r'\n# ', content)
    
    # The first element is intro
    sections = {}
    for sec in raw_sections[1:]:
        lines = sec.strip().split('\n')
        title = lines[0].strip()
        body = '\n'.join(lines[1:]).strip()
        sections[title] = body

    return sections

def get_module_for_title(title):
    t = title.lower()
    if any(k in t for k in ['what is system design', 'ip', 'osi', 'tcp', 'domain name system']):
        return 'mod-foundations', 'Foundations'
    elif any(k in t for k in ['load balancing', 'clustering', 'caching', 'cdn', 'content delivery', 'proxy', 'availability', 'scalability', 'storage']):
        return 'mod-networking', 'Networking & Scalability'
    elif any(k in t for k in ['database', 'sql', 'nosql', 'replication', 'index', 'normalization', 'acid', 'cap', 'pacelc', 'transaction', 'sharding', 'consistent hashing', 'federation']):
        return 'mod-databases', 'Databases & Storage'
    elif any(k in t for k in ['n-tier', 'message broker', 'message queue', 'publish-subscribe', 'enterprise service bus', 'monolith', 'event-driven', 'event sourcing', 'cqrs', 'api gateway', 'rest, graphql', 'long polling']):
        return 'mod-distributed', 'Distributed Systems & Messaging'
    elif any(k in t for k in ['geohashing', 'circuit breaker', 'rate limiting', 'service discovery', 'sla', 'disaster recovery', 'virtual machines', 'oauth', 'single sign-on', 'ssl']):
        return 'mod-advanced', 'Advanced Concepts & Reliability'
    elif any(k in t for k in ['system design interview', 'url shortener', 'whatsapp', 'twitter', 'netflix', 'uber']):
        return 'mod-case-studies', 'System Design Interviews & Case Studies'
    return 'mod-foundations', 'Foundations'

def extract_diagram_id(title):
    slug = slugify(title)
    # Map to existing excalidraw file if possible
    mapping = {
        'load-balancing': 'load-balancing',
        'caching': 'caching',
        'database-replication': 'database-replication',
        'message-queues': 'message-queues',
        'sharding': 'sharding',
        'consistent-hashing': 'consistent-hashing',
        'content-delivery-network-cdn': 'cdn',
        'domain-name-system-dns': 'domain-name-system',
        'api-gateway': 'api-gateway',
        'rate-limiting': 'rate-limiting',
        'circuit-breaker': 'circuit-breaker',
        'url-shortener': 'url-shortener',
        'whatsapp': 'whatsapp',
        'twitter': 'twitter',
        'netflix': 'netflix',
        'uber': 'uber',
        'sql-vs-nosql-databases': 'db-dbms',
        'acid-and-base-consistency-models': 'transactions',
        'cap-theorem': 'cap',
        'pacelc-theorem': 'pacelc',
        'distributed-transactions': 'distributed-transactions',
        'publish-subscribe': 'pubsub',
        'message-brokers': 'message-brokers',
        'monoliths-and-microservices': 'monoliths-microservices',
        'event-driven-architecture-eda': 'event-driven-architecture',
        'event-sourcing': 'event-sourcing',
        'command-and-query-responsibility-segregation-cqrs': 'cqrs',
        'n-tier-architecture': 'n-tier-architecture',
        'proxy': 'proxy',
        'clustering': 'clustering',
        'geohashing-and-quadtrees': 'geohashing-quadtrees',
        'service-discovery': 'service-discovery',
        'disaster-recovery': 'disaster-recovery',
        'virtual-machines-vms-and-containers': 'virtualization-containerization',
        'oauth-20-and-openid-connect-oidc': 'oauth-openid-connect',
        'single-sign-on-sso': 'sso',
        'ssl-tls-mtls': 'tcp-udp',
        'tcp-and-udp': 'tcp-udp',
        'osi-model': 'osi',
        'ip': 'osi',
        'indexes': 'indexes',
        'database-federation': 'federated-database',
        'enterprise-service-bus-esb': 'enterprise-service-bus',
        'long-polling-websockets-server-sent-events-sse': 'lp-ws-sse',
        'scalability': 'scaling'
    }
    return mapping.get(slug, slug)

def generate_analogy_and_problem(title, body):
    t = title.lower()
    if 'load balancing' in t:
        return (
            "Traffic arrives unevenly; a single server crashes under sudden traffic spikes, leaving users stranded.",
            "Think of a bank with 5 teller windows and a queue manager at the entrance directing each customer to the next open window, preventing any single teller from drowning in work.",
            "Distributing traffic across multiple server instances avoids single points of failure and prevents server overload.",
            "Sticky sessions can cause traffic imbalance; health check timeouts must be tuned carefully.",
            "Adding a load balancer introduces network hops and a potential single point of failure if the LB itself isn't redundant (active-passive)."
        )
    elif 'caching' in t:
        return (
            "Disks and databases are orders of magnitude slower than memory. Repeated identical queries choke the database.",
            "Like keeping the books you are actively studying on your desk instead of walking to the university library archive across town for every single page.",
            "Keep hot data in fast volatile RAM (Redis/Memcached) with appropriate eviction policies (LRU/LFU).",
            "Not setting TTLs leading to stale data or out-of-memory crashes; cache stampede during cold restarts.",
            "High read throughput and low latency at the cost of eventual consistency, cache invalidation complexity, and RAM cost."
        )
    elif 'database replication' in t:
        return (
            "If your primary database disk fails or catches fire, your entire company loses all data and goes offline.",
            "Like a master accountant writing transactions in the main ledger, and two assistants continuously copying each entry into backup ledgers in separate vaults.",
            "Primary handles writes and syncs to replicas; replicas serve read traffic and stand ready for automated failover.",
            "Replication lag causing users to read stale data immediately after a write (read-your-own-writes hazard); split-brain on network partitions.",
            "High read scalability and fault tolerance at the cost of replication lag and consistency trade-offs (sync vs async)."
        )
    elif 'message queue' in t:
        return (
            "Synchronous HTTP requests block the caller and cause domino failures when downstream services are slow or temporarily down.",
            "Like an order queue in a busy coffee shop: the cashier hands you a receipt number and immediately takes the next customer, while 3 baristas process drinks asynchronously.",
            "Decouple producers from consumers to buffer traffic surges and protect downstream services with backpressure.",
            "Lack of dead-letter queues causing poison messages to crash consumer workers in an endless loop.",
            "Guaranteed message delivery and spike buffering at the cost of asynchronous complexity, ordering challenges, and duplicate handling."
        )
    elif 'sharding' in t:
        return (
            "A single database server reaches vertical scaling limits (RAM, CPU, disk IOPS) and can no longer store or index petabytes of data.",
            "Like splitting an enormous phonebook into 26 individual volumes (A to Z) stored on separate shelves across a library.",
            "Partition database tables horizontally across distinct physical nodes based on a partition key.",
            "Choosing a poor shard key (like timestamp) leading to hot shards where 90% of writes hit one machine.",
            "Infinite horizontal data capacity at the cost of cross-shard joins, distributed transactions, and complex resharding."
        )
    elif 'consistent hashing' in t:
        return (
            "Standard modulo hashing (hash(key) % N) invalidates almost all cached keys whenever a server is added or removed, causing a cache stampede.",
            "Like arranging servers around a 360-degree circular roulette wheel and mapping keys to the next server clockwise.",
            "Consistent hashing minimizes key redistribution to k/N when scaling cache nodes from N to N+1.",
            "Non-uniform key distribution without virtual nodes; cascading failures if a node fails without replica virtual points.",
            "Minimal key migration during topology changes at the cost of virtual node management and ring lookup complexity."
        )
    elif 'url shortener' in t:
        return (
            "Long, unwieldy URLs consume characters in SMS/tweets and expose internal routing paths.",
            "Like an airport baggage claim tag: a short 7-character barcode identifier maps to your entire heavy luggage record.",
            "Use Base62 encoding on unique 64-bit IDs generated by a Key Generation Service (KGS) or snowflake algorithm, backed by distributed cache.",
            "Generating short IDs using random strings causing collision checks against database on every write.",
            "Blazing 5ms redirect latency via aggressive caching at the cost of write-time key pre-generation."
        )
    else:
        return (
            f"Solving the scalability and reliability bottlenecks inherent to {title.lower()}.",
            f"Think of {title.lower()} as an organized assembly line where specialized stations collaborate efficiently.",
            f"Understand the architectural trade-offs, access patterns, and failure modes of {title.lower()}.",
            f"Misconfiguring timeouts, omitting telemetry, or coupling components tightly.",
            "Balancing throughput, latency, consistency, and operational complexity."
        )

def main():
    sections = parse_readme()
    print(f"Parsed {len(sections)} sections from README.md")

    # Define modules
    modules = [
        {
            "id": "mod-foundations",
            "title": "System Design Foundations",
            "description": "Master core networking primitives, protocol layers, and the architectural mindset.",
            "icon": "foundation",
            "order": 1,
            "color": "#2563eb"
        },
        {
            "id": "mod-networking",
            "title": "Networking & Scalability",
            "description": "Load balancers, caching hierarchies, CDNs, reverse proxies, and scaling tiers.",
            "icon": "network",
            "order": 2,
            "color": "#059669"
        },
        {
            "id": "mod-databases",
            "title": "Databases & Storage",
            "description": "SQL vs NoSQL, replication, sharding, indexes, CAP theorem, and distributed consistency.",
            "icon": "database",
            "order": 3,
            "color": "#7c3aed"
        },
        {
            "id": "mod-distributed",
            "title": "Distributed Systems & Messaging",
            "description": "Message queues, pub/sub brokers, microservices, event sourcing, CQRS, and API gateways.",
            "icon": "hub",
            "order": 4,
            "color": "#d97706"
        },
        {
            "id": "mod-advanced",
            "title": "Advanced Concepts & Reliability",
            "description": "Circuit breakers, rate limiting, service discovery, disaster recovery, quadtrees, and auth.",
            "icon": "security",
            "order": 5,
            "color": "#dc2626"
        },
        {
            "id": "mod-case-studies",
            "title": "Interviews & Real-World Systems",
            "description": "Step-by-step interview framework and deep dives into URL Shortener, WhatsApp, Twitter, Netflix, and Uber.",
            "icon": "architecture",
            "order": 6,
            "color": "#0284c7"
        }
    ]

    lessons = []
    concepts = []
    learning_nodes = []
    learning_edges = []
    repo_files = []

    # Copy excalidraw files
    diagram_assets_dir = os.path.join(ASSETS_DIR, 'diagrams')
    os.makedirs(diagram_assets_dir, exist_ok=True)
    for f in os.listdir(DIAGRAMS_DIR):
        if f.endswith('.excalidraw'):
            shutil.copy2(os.path.join(DIAGRAMS_DIR, f), os.path.join(diagram_assets_dir, f))
    print(f"Copied {len(os.listdir(diagram_assets_dir))} diagrams to assets")

    ignored_titles = ['Table of contents', 'Next Steps', 'References']
    
    previous_lesson_id = None
    all_titles = [t for t in sections.keys() if t not in ignored_titles]
    
    for idx, title in enumerate(all_titles):
        body = sections[title]
        slug = slugify(title)
        mod_id, mod_name = get_module_for_title(title)
        diagram_id = extract_diagram_id(title)
        prob, analogy, takeaway, mistakes, tradeoffs = generate_analogy_and_problem(title, body)

        # Split body into simple intro vs deep technical
        subsections = body.split('\n## ')
        simple_intro = subsections[0].strip()
        deep_tech = '\n\n## '.join(subsections[1:]).strip() if len(subsections) > 1 else body

        # Difficulty & Minutes
        length = len(body.split())
        est_min = max(5, min(15, length // 120 + 4))
        if idx < 6:
            diff = "Beginner"
        elif idx < 30:
            diff = "Intermediate"
        else:
            diff = "Advanced"

        # Interactive Question
        question = {
            "id": f"q-{slug}",
            "question": f"In production system design, what is the primary benefit of {title}?",
            "options": [
                f"It eliminates the need for any database indexing or caching.",
                f"{takeaway}",
                f"It guarantees 100% zero-latency execution across all WAN clients.",
                f"It allows replacing all microservices with a single SQLite file."
            ],
            "correctOptionIndex": 1,
            "explanation": f"{takeaway} Common pitfall to avoid: {mistakes}"
        }

        # Practical Exercise
        exercise = {
            "id": f"ex-{slug}",
            "title": f"Production Troubleshooting: {title}",
            "type": "scenario",
            "scenario": f"You are a Staff Infrastructure Engineer. Your production dashboard reports severe degradation related to {title.lower()}. {prob}",
            "question": f"Which architectural change directly mitigates this bottleneck?",
            "options": [
                f"Apply {title} best practices: {takeaway}, taking into account {tradeoffs}.",
                f"Temporarily reboot all servers and disable TLS encryption.",
                f"Remove the component entirely and route all traffic to disk.",
                f"Increase client retry frequency to 10 milliseconds without backoff."
            ],
            "correctOptionIndex": 0,
            "explanation": f"Correct! {takeaway}. Remember: {tradeoffs}"
        }

        next_id = slugify(all_titles[idx + 1]) if idx + 1 < len(all_titles) else None

        lesson_obj = {
            "id": slug,
            "title": title,
            "moduleId": mod_id,
            "moduleName": mod_name,
            "order": idx + 1,
            "difficulty": diff,
            "estimatedMinutes": est_min,
            "problem": prob,
            "simpleExplanation": simple_intro,
            "technicalExplanation": deep_tech if deep_tech else simple_intro,
            "visualExplanation": f"Requests flow through the {title} layer. Observe how input is evaluated, transformed, and routed without overloading downstream resources.",
            "realWorldAnalogy": analogy,
            "architectureDiagramId": diagram_id,
            "repoReference": {
                "filePath": "README.md",
                "sectionHeading": title,
                "diagramFile": f"{diagram_id}.excalidraw" if os.path.exists(os.path.join(DIAGRAMS_DIR, f"{diagram_id}.excalidraw")) else None
            },
            "commonMistakes": mistakes,
            "tradeOffs": tradeoffs,
            "keyTakeaway": takeaway,
            "relatedConcepts": [slugify(all_titles[max(0, idx - 1)]), slugify(all_titles[min(len(all_titles) - 1, idx + 1)])],
            "prerequisites": [slugify(all_titles[idx - 1])] if idx > 0 else [],
            "nextLessonId": next_id,
            "interactiveQuestion": question,
            "exercise": exercise
        }

        lessons.append(lesson_obj)

        # Write individual lesson file
        lesson_file = os.path.join(ASSETS_DIR, 'lessons', f"{slug}.json")
        with open(lesson_file, 'w', encoding='utf-8') as lf:
            json.dump(lesson_obj, lf, indent=2)

        # Concept
        concepts.append({
            "id": slug,
            "name": title,
            "moduleId": mod_id,
            "summary": simple_intro[:200] + ('...' if len(simple_intro) > 200 else ''),
            "keyTakeaway": takeaway,
            "difficulty": diff,
            "diagramId": diagram_id
        })

        # Learning map node
        learning_nodes.append({
            "id": slug,
            "label": title,
            "moduleId": mod_id,
            "order": idx + 1,
            "difficulty": diff,
            "prerequisites": [slugify(all_titles[idx - 1])] if idx > 0 else []
        })
        if idx > 0:
            learning_edges.append({
                "from": slugify(all_titles[idx - 1]),
                "to": slug,
                "relationship": "leads_to"
            })

        # Repository Explorer mapping
        repo_files.append({
            "id": f"repo-{slug}",
            "title": title,
            "slug": slug,
            "category": mod_name,
            "markdownFile": "README.md",
            "section": title,
            "diagramFile": f"{diagram_id}.excalidraw" if os.path.exists(os.path.join(DIAGRAMS_DIR, f"{diagram_id}.excalidraw")) else None,
            "whatItTeaches": takeaway,
            "relatedLessonId": slug,
            "relatedExerciseId": f"ex-{slug}"
        })

        previous_lesson_id = slug

    # Save modules.json
    with open(os.path.join(ASSETS_DIR, 'modules.json'), 'w', encoding='utf-8') as f:
        json.dump(modules, f, indent=2)

    # Save lessons index
    with open(os.path.join(ASSETS_DIR, 'lessons.json'), 'w', encoding='utf-8') as f:
        json.dump(lessons, f, indent=2)

    # Save concepts.json
    with open(os.path.join(ASSETS_DIR, 'concepts.json'), 'w', encoding='utf-8') as f:
        json.dump(concepts, f, indent=2)

    # Save learning_map.json
    with open(os.path.join(ASSETS_DIR, 'learning_map.json'), 'w', encoding='utf-8') as f:
        json.dump({
            "nodes": learning_nodes,
            "edges": learning_edges
        }, f, indent=2)

    # Save repository_explorer.json
    with open(os.path.join(ASSETS_DIR, 'repository_explorer.json'), 'w', encoding='utf-8') as f:
        json.dump({
            "repository": "karanpratapsingh/system-design",
            "branch": "main",
            "sourceUrl": "https://github.com/karanpratapsingh/system-design",
            "files": repo_files,
            "diagramCount": len(os.listdir(diagram_assets_dir))
        }, f, indent=2)

    # Build Exercises Catalog
    exercises = []
    for l in lessons:
        exercises.append(l["exercise"])
    with open(os.path.join(ASSETS_DIR, 'exercises.json'), 'w', encoding='utf-8') as f:
        json.dump(exercises, f, indent=2)

    # Build Simulations Catalog
    simulations = [
        {
            "id": "sim-load-balancer",
            "title": "3D Load Balancing Simulation",
            "category": "Traffic & Networking",
            "description": "Observe a single server collapse under traffic, introduce an active Load Balancer, scale worker instances, and monitor live latency and CPU distribution.",
            "initialTraffic": 500,
            "maxTraffic": 50000,
            "initialServers": 1,
            "minServers": 1,
            "maxServers": 8,
            "learningPoints": [
                "Single points of failure cause system outages under spikes.",
                "Round-robin and Least-Connection distribution balance workload.",
                "Adding nodes linearly expands concurrent request capacity."
            ]
        },
        {
            "id": "sim-caching",
            "title": "3D Cache & Database Simulation",
            "category": "Storage & Latency",
            "description": "Simulate client queries traversing memory cache (Redis) vs disk database. Tweak cache size, TTL, and witness cache hits reduce DB load by 95%.",
            "initialTraffic": 1000,
            "maxTraffic": 100000,
            "initialCacheSize": 100,
            "initialTtl": 60,
            "learningPoints": [
                "Cache hits return data in sub-millisecond RAM latency.",
                "Cache misses force expensive disk queries and risk cache stampedes.",
                "Eviction policies (LRU) protect cache capacity limits."
            ]
        },
        {
            "id": "sim-db-replication",
            "title": "3D Database Replication & Failover",
            "category": "Reliability & HA",
            "description": "Animate primary-replica replication logs. Trigger primary hardware failure, handle the split-brain hazard, and promote a replica to primary.",
            "initialReplicas": 2,
            "replicationMode": "asynchronous",
            "learningPoints": [
                "Replication provides read-scaling and high availability.",
                "Asynchronous replication introduces replication lag and stale reads.",
                "Automated failover must prevent split-brain using quorum."
            ]
        },
        {
            "id": "sim-message-queue",
            "title": "3D Message Queue & Backpressure",
            "category": "Distributed Messaging",
            "description": "Buffer bursty producer traffic in a distributed message queue. Scale worker pools to eliminate queue depth lag and enforce backpressure.",
            "initialTraffic": 2000,
            "initialWorkers": 2,
            "maxWorkers": 12,
            "learningPoints": [
                "Queues decouple throughput mismatch between producers and consumers.",
                "Backpressure protects downstream databases from collapsing.",
                "Dead-letter queues safely quarantine poison pill payloads."
            ]
        },
        {
            "id": "sim-sharding",
            "title": "3D Database Sharding",
            "category": "Data Partitioning",
            "description": "Horizontally partition a multi-terabyte dataset across 4 physical database shards. Observe hash distribution and hot shard mitigation.",
            "shardCount": 4,
            "partitioningAlgorithm": "consistent-hash",
            "learningPoints": [
                "Sharding breaks vertical hardware ceiling for petabyte datasets.",
                "Good shard keys evenly distribute read and write throughput.",
                "Cross-shard joins are costly and should be avoided in schema design."
            ]
        },
        {
            "id": "sim-failure-injection",
            "title": "3D Chaos & Failure Simulator",
            "category": "Resilience",
            "description": "Simulate real-world cloud failures: kill random servers, inject 500ms packet loss, partition a network, and verify circuit breaker fail-safes.",
            "learningPoints": [
                "Design for failure: assume any server or network link will die.",
                "Circuit breakers prevent cascading domino failures across microservices.",
                "Graceful degradation maintains core user functionality during outages."
            ]
        },
        {
            "id": "sim-architecture-playground",
            "title": "Build Mode: Architecture Playground",
            "category": "Interactive Sandbox",
            "description": "Drag, connect, and configure Client, Load Balancer, API Gateway, Cache, Database, and Queue. Run real-time traffic to test latency, throughput, and bottlenecks.",
            "learningPoints": [
                "Every architectural decision involves latency, cost, and complexity trade-offs.",
                "Identify bottlenecks before deploying code to production."
            ]
        }
    ]
    with open(os.path.join(ASSETS_DIR, 'simulations.json'), 'w', encoding='utf-8') as f:
        json.dump(simulations, f, indent=2)

    # Build Interview Simulator Catalog
    interviews = [
        {
            "id": "interview-url-shortener",
            "title": "Design a URL Shortener (TinyURL)",
            "difficulty": "Medium",
            "traffic": "500 Million new URLs per month (100:1 read/write ratio)",
            "storage": "500M * 500 bytes = 250 GB/month, 15 TB over 5 years",
            "steps": [
                {
                    "stepNumber": 1,
                    "title": "Requirements Clarification",
                    "prompt": "Clarify functional and non-functional requirements. What must the system do, and what are its performance SLAs?",
                    "expectedDecisions": ["Given URL generates short hash", "Redirect with 301/302", "High availability (99.99%)", "Sub-10ms redirect latency"]
                },
                {
                    "stepNumber": 2,
                    "title": "Traffic & Capacity Estimations",
                    "prompt": "Calculate write QPS, read QPS, bandwidth, and cache memory required for 20% hot URLs (80-20 rule).",
                    "expectedDecisions": ["Writes: ~200 QPS", "Reads: ~20,000 QPS", "Cache memory: ~34 GB RAM per day"]
                },
                {
                    "stepNumber": 3,
                    "title": "Key Generation & Data Storage",
                    "prompt": "How do you generate unique short keys without write collisions or DB race conditions?",
                    "expectedDecisions": ["Pre-generate unique 7-char Base62 keys with Key Generation Service (KGS)", "Store mapping in NoSQL Key-Value or partitioned SQL"]
                },
                {
                    "stepNumber": 4,
                    "title": "Caching & Latency Optimization",
                    "prompt": "Where do you place caches, and what eviction policy do you choose?",
                    "expectedDecisions": ["Distributed Redis cache in front of DB", "LRU (Least Recently Used) eviction", "HTTP 302 vs 301 caching trade-off"]
                },
                {
                    "stepNumber": 5,
                    "title": "Scaling & Fault Tolerance",
                    "prompt": "How do you handle regional outages and database shard failures?",
                    "expectedDecisions": ["Consistent hashing across DB shards", "Multi-region read replicas with GeoDNS"]
                }
            ]
        },
        {
            "id": "interview-whatsapp",
            "title": "Design a Scalable Chat Service (WhatsApp)",
            "difficulty": "Hard",
            "traffic": "2 Billion users, 100 Billion messages daily",
            "storage": "10 PB message archive per year",
            "steps": [
                {
                    "stepNumber": 1,
                    "title": "Requirements & Scope",
                    "prompt": "Define core chat requirements: 1-on-1 messaging, group chats, delivery status (sent, delivered, read), offline messaging.",
                    "expectedDecisions": ["Real-time low latency delivery", "End-to-end encryption", "Push notifications for offline users"]
                },
                {
                    "stepNumber": 2,
                    "title": "Connection & Protocol Selection",
                    "prompt": "Which network protocol will you choose for maintaining millions of concurrent bi-directional connections?",
                    "expectedDecisions": ["Persistent WebSockets or TCP connections", "Connection gateway service with Epoll/Kqueue"]
                },
                {
                    "stepNumber": 3,
                    "title": "Message Routing & Presence",
                    "prompt": "How do you track which server holds the active connection for User B, and route User A's message to User B?",
                    "expectedDecisions": ["Session Service backed by Redis cluster", "Distributed message queue (Kafka/Pulsar) for temporary buffering"]
                },
                {
                    "stepNumber": 4,
                    "title": "Storage Architecture",
                    "prompt": "How do you store transient unread messages vs historical chat logs?",
                    "expectedDecisions": ["Wide-column store (Apache Cassandra/ScyllaDB) partitioned by (chat_id, message_id)", "Ephemeral queue for offline delivery"]
                }
            ]
        },
        {
            "id": "interview-twitter",
            "title": "Design Twitter / X (News Feed System)",
            "difficulty": "Hard",
            "traffic": "300 Million DAU, 500 Million tweets/day, massive celebrity fanout",
            "storage": "80 TB media + tweets per day",
            "steps": [
                {
                    "stepNumber": 1,
                    "title": "Feed Generation Model: Fanout on Write vs Fanout on Read",
                    "prompt": "How do you handle the celebrity problem (e.g. user with 100M followers tweets)?",
                    "expectedDecisions": ["Hybrid approach: Fanout-on-write (push) for normal users, Fanout-on-read (pull) for high-follower celebrities"]
                },
                {
                    "stepNumber": 2,
                    "title": "Timeline Caching",
                    "prompt": "How is a user's home timeline cached and served in <50ms?",
                    "expectedDecisions": ["In-memory Redis timeline ring buffer (e.g. 800 recent tweet IDs per active user)", "Hydrate tweet entities via multi-get cache"]
                }
            ]
        },
        {
            "id": "interview-netflix",
            "title": "Design a Video Streaming Platform (Netflix)",
            "difficulty": "Hard",
            "traffic": "250 Million subscribers, 100 Million concurrent streams",
            "storage": "Petabytes of multi-bitrate encoded video chunks",
            "steps": [
                {
                    "stepNumber": 1,
                    "title": "Video Ingestion & Transcoding",
                    "prompt": "How do you process source video files into streamable formats across varying client bandwidths?",
                    "expectedDecisions": ["Chunking video into 2-5 second segments", "Transcoding DAG into HLS/DASH at multiple bitrates (Adaptive Bitrate Streaming)"]
                },
                {
                    "stepNumber": 2,
                    "title": "CDN Edge Delivery",
                    "prompt": "How do you ensure zero buffering and high throughput for global video playback?",
                    "expectedDecisions": ["Custom Open Connect CDN appliances deployed at ISP internet exchange points (IXPs)", "GeoDNS / BGP Anycast routing"]
                }
            ]
        },
        {
            "id": "interview-uber",
            "title": "Design a Ride-Sharing Service (Uber)",
            "difficulty": "Hard",
            "traffic": "50 Million daily trips, 5 Million active drivers reporting GPS every 4 seconds",
            "storage": "High-write geospatial location stream",
            "steps": [
                {
                    "stepNumber": 1,
                    "title": "Real-time Geospatial Indexing",
                    "prompt": "How do you index millions of continuously moving driver coordinates to quickly find nearest drivers?",
                    "expectedDecisions": ["Geohash or Uber H3 hexagonal spatial index", "In-memory geospatial cluster (Redis GEO or Google S2 / Quadtrees)"]
                },
                {
                    "stepNumber": 2,
                    "title": "Ride Matching & Dispatch",
                    "prompt": "How do you match a rider request to the optimal driver without race conditions?",
                    "expectedDecisions": ["Ring buffer / Dispatch lock per driver", "ETA computation via routing engine", "WebSocket connection for driver accept/reject modal"]
                }
            ]
        }
    ]
    with open(os.path.join(ASSETS_DIR, 'interviews.json'), 'w', encoding='utf-8') as f:
        json.dump(interviews, f, indent=2)

    # Build Audiobook Catalog
    audiobook_chapters = [
        {
            "id": "audio-ch-01",
            "chapterNumber": 1,
            "title": "Foundations & The Network Layer",
            "subtitle": "IP, OSI 7-Layer Model, TCP vs UDP, and Domain Name System",
            "durationSeconds": 480,
            "audioFileName": "chapter_01.wav",
            "audioAssetPath": "course/audio/chapter_01.wav",
            "visualSyncPoints": [
                {"timestampSeconds": 0, "conceptId": "what-is-system-design", "caption": "The core philosophy of system design"},
                {"timestampSeconds": 90, "conceptId": "ip", "caption": "Understanding IP routing and address spaces"},
                {"timestampSeconds": 210, "conceptId": "osi-model", "caption": "Traversing the OSI 7-layer architecture"},
                {"timestampSeconds": 330, "conceptId": "tcp-and-udp", "caption": "TCP reliability vs UDP throughput"},
                {"timestampSeconds": 410, "conceptId": "domain-name-system-dns", "caption": "DNS resolution and hierarchical lookups"}
            ],
            "narration": "Welcome to System Design Lab. Today we begin our journey at the bedrock of modern distributed computing: the network layer. Every distributed system begins with a packet moving across physical boundaries. In this chapter, we explore IP addressing, the classic OSI seven-layer model, the critical trade-offs between TCP handshakes and UDP datagrams, and how the Domain Name System directs internet traffic with authoritative name servers and recursive resolvers."
        },
        {
            "id": "audio-ch-02",
            "chapterNumber": 2,
            "title": "Scaling Traffic: Load Balancing & Caching",
            "subtitle": "Reverse Proxies, Consistent Hashing, and Multi-Tier Caching",
            "durationSeconds": 540,
            "audioFileName": "chapter_02.wav",
            "audioAssetPath": "course/audio/chapter_02.wav",
            "visualSyncPoints": [
                {"timestampSeconds": 0, "conceptId": "load-balancing", "caption": "Layer 4 vs Layer 7 load balancing algorithms"},
                {"timestampSeconds": 130, "conceptId": "caching", "caption": "Cache-aside, write-through, and LRU eviction"},
                {"timestampSeconds": 270, "conceptId": "content-delivery-network-cdn", "caption": "CDN edge caching and static asset distribution"},
                {"timestampSeconds": 400, "conceptId": "consistent-hashing", "caption": "Preventing cache stampedes with consistent hash rings"}
            ],
            "narration": "In chapter two, we tackle the first hurdle of high-growth applications: surviving massive traffic surges. When a single server exhausts its CPU and socket limits, how do we scale? We introduce Layer 4 and Layer 7 load balancers, evaluate Round Robin, Least Connections, and IP Hash algorithms. We then dive into caching architectures, contrasting Cache-Aside with Write-Through strategies, and examine how consistent hashing protects distributed caches from devastating stampedes when cluster nodes fluctuate."
        },
        {
            "id": "audio-ch-03",
            "chapterNumber": 3,
            "title": "Databases, Consistency & Storage",
            "subtitle": "SQL vs NoSQL, Replication, Sharding, CAP and PACELC",
            "durationSeconds": 600,
            "audioFileName": "chapter_03.wav",
            "audioAssetPath": "course/audio/chapter_03.wav",
            "visualSyncPoints": [
                {"timestampSeconds": 0, "conceptId": "sql-vs-nosql-databases", "caption": "Relational constraints vs distributed key-value agility"},
                {"timestampSeconds": 140, "conceptId": "database-replication", "caption": "Sync vs async replication and failover consensus"},
                {"timestampSeconds": 280, "conceptId": "cap-theorem", "caption": "The fundamental physics of CAP and PACELC"},
                {"timestampSeconds": 440, "conceptId": "sharding", "caption": "Horizontal partitioning and cross-shard trade-offs"}
            ],
            "narration": "Data is the lifeblood and heaviest burden of any architecture. In chapter three, we dissect databases and distributed storage. We compare the strict ACID guarantees of relational databases with the horizontal flexibility of NoSQL document, wide-column, and graph engines. We uncover the realities of database replication, replication lag, and the immutable trade-offs codified by Eric Brewer's CAP theorem and Daniel Abadi's PACELC theorem."
        },
        {
            "id": "audio-ch-04",
            "chapterNumber": 4,
            "title": "Asynchronous Systems & Message Queues",
            "subtitle": "Decoupling Microservices with Queues, Pub/Sub, and Event Sourcing",
            "durationSeconds": 520,
            "audioFileName": "chapter_04.wav",
            "audioAssetPath": "course/audio/chapter_04.wav",
            "visualSyncPoints": [
                {"timestampSeconds": 0, "conceptId": "message-queues", "caption": "Point-to-point queues and backpressure management"},
                {"timestampSeconds": 150, "conceptId": "publish-subscribe", "caption": "Fanout messaging and consumer group partitioning"},
                {"timestampSeconds": 310, "conceptId": "event-driven-architecture-eda", "caption": "Loose coupling through asynchronous domain events"},
                {"timestampSeconds": 420, "conceptId": "circuit-breaker", "caption": "Protecting against cascading failure with circuit breakers"}
            ],
            "narration": "Synchronous HTTP calls create fragile, tightly coupled architectures. In this chapter, we discover the power of asynchronous message passing. We explore point-to-point message queues for work distribution and publish-subscribe topics for one-to-many event notification. Learn how consumer groups scale processing, how backpressure prevents downstream database meltdown, and how circuit breakers isolate faults to stop cascading system outages."
        },
        {
            "id": "audio-ch-05",
            "chapterNumber": 5,
            "title": "Architecting Real-World Systems",
            "subtitle": "Case Studies: TinyURL, WhatsApp, Twitter, and Netflix",
            "durationSeconds": 620,
            "audioFileName": "chapter_05.wav",
            "audioAssetPath": "course/audio/chapter_05.wav",
            "visualSyncPoints": [
                {"timestampSeconds": 0, "conceptId": "system-design-interviews", "caption": "The 6-step system design interview blueprint"},
                {"timestampSeconds": 120, "conceptId": "url-shortener", "caption": "Key generation services and sub-10ms redirect latency"},
                {"timestampSeconds": 260, "conceptId": "whatsapp", "caption": "Real-time socket persistence and wide-column message storage"},
                {"timestampSeconds": 410, "conceptId": "twitter", "caption": "Solving the celebrity fanout problem in social feeds"},
                {"timestampSeconds": 520, "conceptId": "netflix", "caption": "Transcoding pipelines and global CDN edge delivery"}
            ],
            "narration": "In our final chapter, we synthesize all concepts into production architectures. We walk through the exact six-step framework required in top-tier system design interviews. Then we deconstruct four iconic architectures: the pre-generation hash ring of TinyURL, the persistent connection gateway of WhatsApp, the hybrid fanout engine of Twitter, and the global edge distribution mesh of Netflix."
        }
    ]
    with open(os.path.join(ASSETS_DIR, 'audiobook.json'), 'w', encoding='utf-8') as f:
        json.dump(audiobook_chapters, f, indent=2)

    # Build Achievements
    achievements = [
        {"id": "ach-first-lesson", "title": "First Lesson", "description": "Complete your first interactive system design lesson.", "xpReward": 25, "icon": "school"},
        {"id": "ach-first-sim", "title": "First Simulation", "description": "Run and manipulate your first 3D architecture simulation.", "xpReward": 50, "icon": "view_in_ar"},
        {"id": "ach-first-architecture", "title": "First Architecture", "description": "Successfully build a working system in Build Mode.", "xpReward": 75, "icon": "account_tree"},
        {"id": "ach-7-day-streak", "title": "7-Day Streak", "description": "Study system design for 7 consecutive days.", "xpReward": 100, "icon": "local_fire_department"},
        {"id": "ach-bug-hunter", "title": "Bug Hunter", "description": "Identify the bottleneck and fix 5 architectural exercise problems.", "xpReward": 60, "icon": "bug_report"},
        {"id": "ach-scaling-thinker", "title": "Scaling Thinker", "description": "Scale a simulated system past 50,000 requests per second.", "xpReward": 80, "icon": "trending_up"},
        {"id": "ach-architecture-builder", "title": "Architecture Builder", "description": "Assemble a resilient 4-tier microservice architecture.", "xpReward": 120, "icon": "domain"},
        {"id": "ach-interview-ready", "title": "Interview Ready", "description": "Complete all step-by-step System Design Interview scenarios.", "xpReward": 200, "icon": "verified"}
    ]
    with open(os.path.join(ASSETS_DIR, 'achievements.json'), 'w', encoding='utf-8') as f:
        json.dump(achievements, f, indent=2)

    print(f"Generated {len(lessons)} lessons, {len(concepts)} concepts, {len(simulations)} simulations, {len(interviews)} interviews, {len(audiobook_chapters)} audiobook chapters!")

if __name__ == '__main__':
    main()
