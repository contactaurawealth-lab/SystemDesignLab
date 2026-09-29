package com.systemdesignlab.app.feature.simulation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.model.SimulationItem
import com.systemdesignlab.app.simulation.engine.SystemSimulationEngine
import com.systemdesignlab.app.simulation.ui.InteractiveSimulationCanvas

@Composable
fun SimulationScreen(
    simulations: List<SimulationItem>,
    initialScenarioId: String = "sim-load-balancer",
    onCompleteScenario: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { SystemSimulationEngine().apply { setupScenario(initialScenarioId) } }
    var selectedScenarioId by remember { mutableStateOf(initialScenarioId) }

    var trafficValue by remember { mutableFloatStateOf(1000f) }
    var serverCountValue by remember { mutableIntStateOf(2) }

    val activeSim = simulations.find { it.id == selectedScenarioId } ?: simulations.firstOrNull()

    // Dynamic narrator message based on live metrics
    val narratorText = remember(engine.liveMetrics, selectedScenarioId) {
        val m = engine.liveMetrics
        when {
            m.avgCpuLoad >= 0.95f -> "🚨 CRITICAL OVERLOAD: Server utilization hit 100%! Latency spiked to ${m.latencyMs.toInt()}ms and error rate is ${m.errorRate.toInt()}%. Scale up servers or enable caching immediately."
            m.avgCpuLoad >= 0.8f -> "⚠️ HIGH LOAD: System is approaching capacity limits. Queue depth is building up. Consider adding another worker or cache node."
            selectedScenarioId == "sim-caching" && m.cacheHitRatio > 0.8f -> "⚡ CACHE EFFICIENCY: 88% of incoming queries are satisfied directly from in-memory Redis in 1.2ms, protecting Postgres from heavy disk I/O."
            selectedScenarioId == "sim-db-replication" -> "🔄 REPLICATION ACTIVE: Write traffic hits Primary DB and WAL stream replicates asynchronously to Followers. Test failover by killing the Primary."
            selectedScenarioId == "sim-message-queue" -> "📥 QUEUE BUFFERING: Spikes buffered in message queue. ${engine.workerCount} workers consuming tasks at steady pace with backpressure intact."
            else -> "✅ STABLE ARCHITECTURE: Traffic is evenly distributed across ${m.activeServers} active nodes. Average latency is ${m.latencyMs.toInt()}ms with 99.99% availability."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scenario Selection Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(simulations) { sim ->
                val isSelected = sim.id == selectedScenarioId
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedScenarioId = sim.id
                        engine.setupScenario(sim.id)
                    },
                    label = { Text(sim.title.replace("3D ", ""), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentEmerald,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = Slate400
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. 3D Simulation Canvas
            item {
                InteractiveSimulationCanvas(engine = engine)
            }

            // 2. Real-Time Metrics HUD
            item {
                val m = engine.liveMetrics
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricItem(label = "TRAFFIC", value = "${m.qps} QPS", color = AccentSky)
                        MetricItem(label = "LATENCY", value = "${m.latencyMs.toInt()} ms", color = if (m.latencyMs > 200) AccentRose else AccentEmerald)
                        MetricItem(label = "ERROR RATE", value = "${m.errorRate.toInt()}%", color = if (m.errorRate > 5) AccentRose else Slate400)
                        MetricItem(label = "CPU LOAD", value = "${(m.avgCpuLoad * 100).toInt()}%", color = if (m.avgCpuLoad > 0.85) AccentRose else AccentIndigo)
                    }
                }
            }

            // 3. Real-Time Educational Narrator Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (engine.liveMetrics.avgCpuLoad >= 0.9f) Color(0xFF451A1A) else Color(0xFF1E293B)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "🎙️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SYSTEM NARRATOR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (engine.liveMetrics.avgCpuLoad >= 0.9f) AccentRose else AccentSky,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = narratorText,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White, lineHeight = 19.sp)
                            )
                        }
                    }
                }
            }

            // 4. Interactive Simulation Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EXPERIMENT CONTROLS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Traffic Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Simulated Traffic", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
                            Text("${trafficValue.toInt()} req/sec", style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = trafficValue,
                            onValueChange = {
                                trafficValue = it
                                engine.trafficQps = it.toInt()
                            },
                            valueRange = 100f..25000f,
                            colors = SliderDefaults.colors(thumbColor = AccentIndigo, activeTrackColor = AccentIndigo)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Server / Worker Node Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Compute Nodes (Scale)", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = {
                                        if (serverCountValue > 1) {
                                            serverCountValue--
                                            engine.setServers(serverCountValue)
                                            engine.setWorkers(serverCountValue)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("-")
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("$serverCountValue", fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        if (serverCountValue < 8) {
                                            serverCountValue++
                                            engine.setServers(serverCountValue)
                                            engine.setWorkers(serverCountValue)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("+")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Failure Injection Triggers
                        Text("CHAOS / FAILURE INJECTION", style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    engine.toggleNodeFailure("server-1")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Kill Server", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    engine.triggerFailover()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Failover DB", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    onCompleteScenario(selectedScenarioId, 100)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pass Lab", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.Bold))
    }
}
