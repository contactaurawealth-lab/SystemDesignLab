package com.systemdesignlab.app.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DiagramCanvasNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val xNorm: Float, // 0.0 to 1.0
    val yNorm: Float, // 0.0 to 1.0
    val color: Color,
    val detail: String
)

data class DiagramCanvasEdge(
    val fromId: String,
    val toId: String,
    val label: String = ""
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun ArchitectureDiagramCanvas(
    diagramId: String,
    modifier: Modifier = Modifier
) {
    var selectedNode by remember { mutableStateOf<DiagramCanvasNode?>(null) }

    // Infinite animation for moving packets
    val infiniteTransition = rememberInfiniteTransition(label = "packetAnimation")
    val packetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packetProgress"
    )

    // Build scenario nodes & edges
    val (nodes, edges) = remember(diagramId) {
        when {
            diagramId.contains("caching") -> {
                val n = listOf(
                    DiagramCanvasNode("client", "Client", "Mobile / Web", 0.15f, 0.5f, Color(0xFF38BDF8), "Sends read/write requests over HTTPS."),
                    DiagramCanvasNode("app", "App Service", "Node / Go", 0.45f, 0.5f, Color(0xFF818CF8), "Executes business logic and cache lookups."),
                    DiagramCanvasNode("cache", "In-Memory Cache", "Redis Cluster", 0.82f, 0.28f, Color(0xFFF59E0B), "Sub-millisecond RAM read for hot queries."),
                    DiagramCanvasNode("db", "Primary DB", "PostgreSQL", 0.82f, 0.72f, Color(0xFFA855F7), "Persistent disk storage; source of truth.")
                )
                val e = listOf(
                    DiagramCanvasEdge("client", "app", "HTTPS"),
                    DiagramCanvasEdge("app", "cache", "Cache Check"),
                    DiagramCanvasEdge("app", "db", "Cache Miss Fallback")
                )
                Pair(n, e)
            }
            diagramId.contains("replication") -> {
                val n = listOf(
                    DiagramCanvasNode("app", "App Layer", "Write Client", 0.18f, 0.5f, Color(0xFF38BDF8), "Sends mutating write queries."),
                    DiagramCanvasNode("leader", "Primary DB", "Leader (RW)", 0.52f, 0.5f, Color(0xFFA855F7), "Handles transactions and appends to WAL."),
                    DiagramCanvasNode("rep1", "Replica #1", "Follower (RO)", 0.85f, 0.25f, Color(0xFF10B981), "Replicates WAL; serves read queries."),
                    DiagramCanvasNode("rep2", "Replica #2", "Follower (RO)", 0.85f, 0.75f, Color(0xFF10B981), "Async replica standby for failover.")
                )
                val e = listOf(
                    DiagramCanvasEdge("app", "leader", "SQL Write"),
                    DiagramCanvasEdge("leader", "rep1", "Async Replication"),
                    DiagramCanvasEdge("leader", "rep2", "Async Replication")
                )
                Pair(n, e)
            }
            diagramId.contains("queue") || diagramId.contains("message") -> {
                val n = listOf(
                    DiagramCanvasNode("producer", "Producers", "API Ingestion", 0.15f, 0.5f, Color(0xFF38BDF8), "Emits task events asynchronously."),
                    DiagramCanvasNode("queue", "Message Queue", "Kafka / SQS", 0.48f, 0.5f, Color(0xFFF59E0B), "Buffers bursts with FIFO order guarantee."),
                    DiagramCanvasNode("w1", "Worker #1", "Consumer", 0.82f, 0.25f, Color(0xFF10B981), "Pulls and processes heavy jobs."),
                    DiagramCanvasNode("w2", "Worker #2", "Consumer", 0.82f, 0.75f, Color(0xFF10B981), "Scales consumer pool under load.")
                )
                val e = listOf(
                    DiagramCanvasEdge("producer", "queue", "Publish"),
                    DiagramCanvasEdge("queue", "w1", "Pull"),
                    DiagramCanvasEdge("queue", "w2", "Pull")
                )
                Pair(n, e)
            }
            else -> {
                // Default: Load Balancing architecture
                val n = listOf(
                    DiagramCanvasNode("clients", "Clients", "Global Traffic", 0.14f, 0.5f, Color(0xFF38BDF8), "Concurrent user requests from internet."),
                    DiagramCanvasNode("lb", "Load Balancer", "Nginx / Envoy", 0.46f, 0.5f, Color(0xFF10B981), "Distributes traffic via Round Robin / Least Conns."),
                    DiagramCanvasNode("s1", "Server #1", "Compute Node", 0.82f, 0.2f, Color(0xFF818CF8), "Stateless microservice instance."),
                    DiagramCanvasNode("s2", "Server #2", "Compute Node", 0.82f, 0.5f, Color(0xFF818CF8), "Stateless microservice instance."),
                    DiagramCanvasNode("s3", "Server #3", "Compute Node", 0.82f, 0.8f, Color(0xFF818CF8), "Stateless microservice instance.")
                )
                val e = listOf(
                    DiagramCanvasEdge("clients", "lb", "Port 443"),
                    DiagramCanvasEdge("lb", "s1", "Balance"),
                    DiagramCanvasEdge("lb", "s2", "Balance"),
                    DiagramCanvasEdge("lb", "s3", "Balance")
                )
                Pair(n, e)
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B132B))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INTERACTIVE SYSTEM ARCHITECTURE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "Tap node to inspect",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF38BDF8)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(nodes) {
                        detectTapGestures { tapOffset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val hit = nodes.find { node ->
                                val nx = node.xNorm * w
                                val ny = node.yNorm * h
                                val dx = tapOffset.x - nx
                                val dy = tapOffset.y - ny
                                (dx * dx + dy * dy) < (45 * 45)
                            }
                            selectedNode = hit
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val nodeMap = nodes.associateBy { it.id }

                // 1. Draw connecting lines
                edges.forEach { edge ->
                    val from = nodeMap[edge.fromId] ?: return@forEach
                    val to = nodeMap[edge.toId] ?: return@forEach

                    val fx = from.xNorm * w
                    val fy = from.yNorm * h
                    val tx = to.xNorm * w
                    val ty = to.yNorm * h

                    // Draw connection line
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(fx, fy),
                        end = Offset(tx, ty),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )

                    // Draw moving packet
                    val px = fx + (tx - fx) * packetProgress
                    val py = fy + (ty - fy) * packetProgress

                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color(0x6638BDF8),
                        radius = 7.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // 2. Draw nodes
                val cardWidth = 84.dp.toPx()
                val cardHeight = 44.dp.toPx()

                nodes.forEach { node ->
                    val cx = node.xNorm * w
                    val cy = node.yNorm * h

                    val isSelected = selectedNode?.id == node.id

                    // Node background
                    drawRoundRect(
                        color = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827),
                        topLeft = Offset(cx - cardWidth / 2, cy - cardHeight / 2),
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Fill
                    )

                    // Node border
                    drawRoundRect(
                        color = if (isSelected) Color(0xFF38BDF8) else node.color.copy(alpha = 0.6f),
                        topLeft = Offset(cx - cardWidth / 2, cy - cardHeight / 2),
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        style = Stroke(width = if (isSelected) 2.dp.toPx() else 1.dp.toPx())
                    )

                    // Node title
                    val textLayout = textMeasurer.measure(
                        text = AnnotatedString(node.title),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(cx - textLayout.size.width / 2, cy - 14.dp.toPx())
                    )

                    // Node subtitle
                    val subLayout = textMeasurer.measure(
                        text = AnnotatedString(node.subtitle),
                        style = TextStyle(
                            color = Color(0xFF94A3B8),
                            fontSize = 8.sp
                        )
                    )
                    drawText(
                        textLayoutResult = subLayout,
                        topLeft = Offset(cx - subLayout.size.width / 2, cy + 2.dp.toPx())
                    )
                }
            }
        }

        // Selected node explanation banner
        selectedNode?.let { node ->
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(node.color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${node.title} (${node.subtitle})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = node.detail,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }
    }
}
