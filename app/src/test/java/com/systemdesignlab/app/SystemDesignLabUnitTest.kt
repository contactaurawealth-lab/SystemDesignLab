package com.systemdesignlab.app

import com.systemdesignlab.app.data.ai.CompactCourseContext
import com.systemdesignlab.app.simulation.engine.SimNodeStatus
import com.systemdesignlab.app.simulation.engine.SimNodeType
import com.systemdesignlab.app.simulation.engine.SystemSimulationEngine
import org.junit.Assert.*
import org.junit.Test

class SystemDesignLabUnitTest {

    @Test
    fun testSimulationEngineLoadBalancing() {
        val engine = SystemSimulationEngine().apply {
            setupScenario("sim-load-balancer")
            setServers(1)
            trafficQps = 15000 // Huge spike for 1 server
        }

        // Advance 2 seconds
        engine.update(2.0f)

        val metrics = engine.liveMetrics
        assertTrue("Single server under 15k QPS should be severely overloaded", metrics.avgCpuLoad >= 0.9f)
        assertTrue("Latency should spike under overload", metrics.latencyMs > 50f)

        // Now scale to 6 servers
        engine.setServers(6)
        engine.trafficQps = 5000
        engine.update(2.0f)

        val scaledMetrics = engine.liveMetrics
        assertTrue("Scaled servers should maintain healthy CPU load", scaledMetrics.avgCpuLoad < 0.6f)
        assertTrue("Scaled servers should restore low latency", scaledMetrics.latencyMs < 20f)
    }

    @Test
    fun testSimulationReplicationFailover() {
        val engine = SystemSimulationEngine().apply {
            setupScenario("sim-db-replication")
        }

        val primary = engine.nodes.find { it.id == "primary-db" }
        assertNotNull(primary)
        assertEquals(SimNodeStatus.HEALTHY, primary?.status)

        // Trigger failover election
        engine.triggerFailover()

        assertEquals("Primary should be marked failed", SimNodeStatus.FAILED, primary?.status)
        val replica = engine.nodes.find { it.id == "replica-1" }
        assertEquals("Replica 1 should be promoted to healthy leader", SimNodeStatus.HEALTHY, replica?.status)
    }

    @Test
    fun testCompactCourseContextBuilder() {
        val context = CompactCourseContext(
            lessonTitle = "Consistent Hashing",
            lessonProblem = "Modulo hashing invalidates cache keys on topology change.",
            lessonTakeaway = "Ring mapping bounds key redistribution to k/N.",
            isHintMode = true
        )

        val promptCtx = context.toPromptContext()
        assertTrue(promptCtx.contains("Lesson: Consistent Hashing"))
        assertTrue(promptCtx.contains("Core Problem: Modulo hashing"))
        assertTrue(promptCtx.contains("Key Takeaway: Ring mapping"))
        assertTrue(context.isHintMode)
    }

    @Test
    fun testSM2IntervalMath() {
        // SM-2 logic test
        var repetitions = 0
        var intervalDays = 1
        var easeFactor = 2.5f

        val quality = 5 // Perfect recall
        if (quality >= 3) {
            repetitions += 1
            intervalDays = when (repetitions) {
                1 -> 1
                2 -> 3
                3 -> 7
                else -> (intervalDays * easeFactor).toInt()
            }
        }
        assertEquals(1, repetitions)
        assertEquals(1, intervalDays)

        // Second repetition
        repetitions += 1
        intervalDays = when (repetitions) {
            1 -> 1
            2 -> 3
            3 -> 7
            else -> (intervalDays * easeFactor).toInt()
        }
        assertEquals(2, repetitions)
        assertEquals(3, intervalDays)
    }
}
