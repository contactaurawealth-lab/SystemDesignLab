package com.systemdesignlab.app.simulation.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class SimNodeType {
    CLIENT,
    LOAD_BALANCER,
    API_GATEWAY,
    SERVER,
    CACHE,
    DATABASE,
    QUEUE,
    WORKER,
    REPLICA
}

enum class SimNodeStatus {
    HEALTHY,
    DEGRADED,
    OVERLOADED,
    FAILED
}

data class SimNode(
    val id: String,
    val name: String,
    val type: SimNodeType,
    var x: Float,
    var y: Float,
    var z: Float = 0f,
    var status: SimNodeStatus = SimNodeStatus.HEALTHY,
    var cpuLoad: Float = 0.2f, // 0.0 to 1.0
    var memoryLoad: Float = 0.3f,
    var queueDepth: Int = 0,
    var isFailed: Boolean = false
)

data class SimPacket(
    val id: Long,
    val fromNodeId: String,
    val toNodeId: String,
    var progress: Float = 0f, // 0.0 to 1.0
    val speed: Float = 1.8f,
    val isCacheHit: Boolean = false,
    val isError: Boolean = false,
    val packetType: String = "HTTP"
)

data class SimConnection(
    val fromId: String,
    val toId: String
)

data class SimLiveMetrics(
    val qps: Int = 500,
    val latencyMs: Float = 12.4f,
    val errorRate: Float = 0.0f,
    val avgCpuLoad: Float = 0.35f,
    val activeServers: Int = 2,
    val queueDepth: Int = 0,
    val cacheHitRatio: Float = 0.85f,
    val systemAvailability: Float = 99.99f
)

class SystemSimulationEngine {

    var scenarioId: String = "sim-load-balancer"

    // Configuration parameters
    var trafficQps: Int = 500
    var serverCount: Int = 2
    var cacheSizeMb: Int = 256
    var cacheTtlSec: Int = 60
    var workerCount: Int = 2
    var failureRatePercent: Float = 0f
    var isCircuitBreakerOpen: Boolean = false

    val nodes = mutableListOf<SimNode>()
    val connections = mutableListOf<SimConnection>()
    val packets = mutableListOf<SimPacket>()

    private var packetIdCounter = 0L
    private var lastSpawnTime = 0L

    var liveMetrics by mutableStateOf(SimLiveMetrics())
        private set

    init {
        setupScenario(scenarioId)
    }

    fun setupScenario(scenario: String) {
        scenarioId = scenario
        nodes.clear()
        connections.clear()
        packets.clear()

        when (scenario) {
            "sim-load-balancer" -> setupLoadBalancerScenario()
            "sim-caching" -> setupCachingScenario()
            "sim-db-replication" -> setupReplicationScenario()
            "sim-message-queue" -> setupQueueScenario()
            "sim-sharding" -> setupShardingScenario()
            "sim-failure-injection" -> setupFailureScenario()
            else -> setupPlaygroundScenario()
        }
        update(0.016f)
    }

    private fun setupLoadBalancerScenario() {
        nodes.add(SimNode("client", "Clients (Global)", SimNodeType.CLIENT, -2.5f, 0f, 0f))
        nodes.add(SimNode("lb", "Load Balancer (Nginx)", SimNodeType.LOAD_BALANCER, -0.8f, 0f, 0f))
        for (i in 1..serverCount) {
            val yOffset = (i - (serverCount + 1) / 2f) * 1.2f
            nodes.add(SimNode("server-$i", "Server #$i", SimNodeType.SERVER, 1.2f, yOffset, 0f))
            connections.add(SimConnection("lb", "server-$i"))
        }
        connections.add(SimConnection("client", "lb"))
    }

    private fun setupCachingScenario() {
        nodes.add(SimNode("client", "Clients", SimNodeType.CLIENT, -2.5f, 0f, 0f))
        nodes.add(SimNode("app", "App Service", SimNodeType.SERVER, -0.8f, 0f, 0f))
        nodes.add(SimNode("cache", "Redis Cache", SimNodeType.CACHE, 1.0f, 1.0f, 0f))
        nodes.add(SimNode("db", "Postgres DB", SimNodeType.DATABASE, 1.0f, -1.0f, 0f))

        connections.add(SimConnection("client", "app"))
        connections.add(SimConnection("app", "cache"))
        connections.add(SimConnection("app", "db"))
    }

    private fun setupReplicationScenario() {
        nodes.add(SimNode("app", "App Service", SimNodeType.SERVER, -2.0f, 0f, 0f))
        nodes.add(SimNode("primary-db", "Primary DB (Leader)", SimNodeType.DATABASE, 0f, 0f, 0f))
        nodes.add(SimNode("replica-1", "Replica #1 (Follower)", SimNodeType.REPLICA, 2.0f, 1.0f, 0f))
        nodes.add(SimNode("replica-2", "Replica #2 (Follower)", SimNodeType.REPLICA, 2.0f, -1.0f, 0f))

        connections.add(SimConnection("app", "primary-db"))
        connections.add(SimConnection("primary-db", "replica-1"))
        connections.add(SimConnection("primary-db", "replica-2"))
    }

    private fun setupQueueScenario() {
        nodes.add(SimNode("client", "Producers", SimNodeType.CLIENT, -2.5f, 0f, 0f))
        nodes.add(SimNode("queue", "Kafka / RabbitMQ", SimNodeType.QUEUE, -0.8f, 0f, 0f))
        for (i in 1..workerCount) {
            val yOffset = (i - (workerCount + 1) / 2f) * 1.2f
            nodes.add(SimNode("worker-$i", "Worker #$i", SimNodeType.WORKER, 1.0f, yOffset, 0f))
            connections.add(SimConnection("queue", "worker-$i"))
        }
        nodes.add(SimNode("db", "Analytics Store", SimNodeType.DATABASE, 2.5f, 0f, 0f))
        for (i in 1..workerCount) {
            connections.add(SimConnection("worker-$i", "db"))
        }
        connections.add(SimConnection("client", "queue"))
    }

    private fun setupShardingScenario() {
        nodes.add(SimNode("client", "API Router", SimNodeType.API_GATEWAY, -2.0f, 0f, 0f))
        for (i in 1..4) {
            val yOffset = (i - 2.5f) * 1.1f
            nodes.add(SimNode("shard-$i", "Shard #$i [ID mod 4]", SimNodeType.DATABASE, 1.2f, yOffset, 0f))
            connections.add(SimConnection("client", "shard-$i"))
        }
    }

    private fun setupFailureScenario() {
        nodes.add(SimNode("gateway", "API Gateway", SimNodeType.API_GATEWAY, -2.0f, 0f, 0f))
        nodes.add(SimNode("service-a", "Order Service", SimNodeType.SERVER, 0f, 1.0f, 0f))
        nodes.add(SimNode("service-b", "Payment Service", SimNodeType.SERVER, 0f, -1.0f, 0f))
        nodes.add(SimNode("db-a", "Orders DB", SimNodeType.DATABASE, 2.0f, 1.0f, 0f))
        nodes.add(SimNode("db-b", "Payments DB", SimNodeType.DATABASE, 2.0f, -1.0f, 0f))

        connections.add(SimConnection("gateway", "service-a"))
        connections.add(SimConnection("gateway", "service-b"))
        connections.add(SimConnection("service-a", "db-a"))
        connections.add(SimConnection("service-b", "db-b"))
    }

    private fun setupPlaygroundScenario() {
        setupLoadBalancerScenario()
    }

    fun setServers(count: Int) {
        serverCount = count.coerceIn(1, 8)
        if (scenarioId == "sim-load-balancer") {
            setupLoadBalancerScenario()
        }
        update(0.016f)
    }

    fun setWorkers(count: Int) {
        workerCount = count.coerceIn(1, 6)
        if (scenarioId == "sim-message-queue") {
            setupQueueScenario()
        }
        update(0.016f)
    }

    fun toggleNodeFailure(nodeId: String) {
        nodes.find { it.id == nodeId }?.let {
            it.isFailed = !it.isFailed
            it.status = if (it.isFailed) SimNodeStatus.FAILED else SimNodeStatus.HEALTHY
        }
        update(0.016f)
    }

    fun triggerFailover() {
        // In replication scenario, promote replica-1 to primary
        val primary = nodes.find { it.id == "primary-db" }
        val replica = nodes.find { it.id == "replica-1" }
        if (primary != null && replica != null) {
            primary.status = SimNodeStatus.FAILED
            primary.isFailed = true
            replica.status = SimNodeStatus.HEALTHY
            replica.isFailed = false
        }
        update(0.016f)
    }

    fun update(deltaSeconds: Float) {
        val now = System.currentTimeMillis()

        // 1. Packet generation based on traffic QPS
        val spawnIntervalMs = (1000f / trafficQps.coerceAtLeast(1) * 40).coerceIn(40f, 600f).toLong()
        if (now - lastSpawnTime > spawnIntervalMs && connections.isNotEmpty()) {
            lastSpawnTime = now
            val randomConn = connections.random()
            val isHit = (Math.random() < 0.85)
            val isErr = (Math.random() < (failureRatePercent / 100f))

            if (packets.size < 60) {
                packets.add(
                    SimPacket(
                        id = ++packetIdCounter,
                        fromNodeId = randomConn.fromId,
                        toNodeId = randomConn.toId,
                        speed = 1.5f + (Math.random() * 0.8f).toFloat(),
                        isCacheHit = isHit,
                        isError = isErr
                    )
                )
            }
        }

        // 2. Advance packets
        val iterator = packets.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.progress += p.speed * deltaSeconds
            if (p.progress >= 1.0f) {
                iterator.remove()
            }
        }

        // 3. Compute dynamic live metrics
        val activeNodes = nodes.filter { !it.isFailed && it.type == SimNodeType.SERVER }
        val effectiveCapacity = activeNodes.size * 3000
        val cpuLoad = (trafficQps.toFloat() / effectiveCapacity.coerceAtLeast(1000)).coerceIn(0.1f, 1.0f)

        // If CPU load is high, latency increases exponentially
        val baseLatency = when (scenarioId) {
            "sim-caching" -> 1.2f
            "sim-message-queue" -> 4.5f
            else -> 8.0f
        }
        val latency = if (cpuLoad > 0.8f) {
            baseLatency * (1f + (cpuLoad - 0.8f) * 50f)
        } else {
            baseLatency + (cpuLoad * 4f)
        }

        val errors = if (cpuLoad >= 0.95f) {
            ((cpuLoad - 0.95f) * 200f).coerceIn(0f, 100f) + failureRatePercent
        } else {
            failureRatePercent
        }

        val queueDepthCalc = if (scenarioId == "sim-message-queue") {
            val incoming = trafficQps
            val processing = workerCount * 1200
            maxOf(0, incoming - processing)
        } else {
            0
        }

        nodes.forEach { node ->
            if (node.isFailed) {
                node.status = SimNodeStatus.FAILED
                node.cpuLoad = 0f
            } else if (cpuLoad > 0.85f && (node.type == SimNodeType.SERVER || node.type == SimNodeType.DATABASE)) {
                node.status = SimNodeStatus.OVERLOADED
                node.cpuLoad = cpuLoad
            } else {
                node.status = SimNodeStatus.HEALTHY
                node.cpuLoad = cpuLoad
            }
        }

        liveMetrics = SimLiveMetrics(
            qps = trafficQps,
            latencyMs = latency,
            errorRate = errors,
            avgCpuLoad = cpuLoad,
            activeServers = activeNodes.size,
            queueDepth = queueDepthCalc,
            cacheHitRatio = if (scenarioId == "sim-caching") 0.88f else 0.0f,
            systemAvailability = (100f - errors).coerceIn(0f, 100f)
        )
    }
}
