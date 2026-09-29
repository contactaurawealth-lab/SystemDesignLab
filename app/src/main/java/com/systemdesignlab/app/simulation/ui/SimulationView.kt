package com.systemdesignlab.app.simulation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.simulation.engine.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun InteractiveSimulationCanvas(
    engine: SystemSimulationEngine,
    modifier: Modifier = Modifier
) {
    var cameraAngleY by remember { mutableStateOf(0f) }
    var cameraAngleX by remember { mutableStateOf(20f) }
    var zoomScale by remember { mutableStateOf(1.0f) }

    // Animation ticker driving 60 FPS simulation updates
    val infiniteTransition = rememberInfiniteTransition(label = "simTicker")
    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker"
    )

    LaunchedEffect(ticker) {
        engine.update(0.016f)
    }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B16))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    cameraAngleY += dragAmount.x * 0.4f
                    cameraAngleX = (cameraAngleX - dragAmount.y * 0.3f).coerceIn(-40f, 60f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Draw 3D Ground Grid (Isometric Tech Plane)
            val gridStep = 40.dp.toPx()
            for (gx in -4..4) {
                val x1 = cx + (gx * gridStep)
                val y1 = cy + 60.dp.toPx()
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.4f),
                    start = Offset(x1 - 100f, y1 - 40f),
                    end = Offset(x1 + 100f, y1 + 40f),
                    strokeWidth = 1f
                )
            }

            // 2. Draw Connections & Data Flow Paths
            val nodeMap = engine.nodes.associateBy { it.id }
            engine.connections.forEach { conn ->
                val from = nodeMap[conn.fromId] ?: return@forEach
                val to = nodeMap[conn.toId] ?: return@forEach

                val fx = cx + from.x * 70.dp.toPx()
                val fy = cy + from.y * 50.dp.toPx()
                val tx = cx + to.x * 70.dp.toPx()
                val ty = cy + to.y * 50.dp.toPx()

                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(fx, fy),
                    end = Offset(tx, ty),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 3. Draw Moving Packets
            engine.packets.forEach { p ->
                val from = nodeMap[p.fromNodeId] ?: return@forEach
                val to = nodeMap[p.toNodeId] ?: return@forEach

                val fx = cx + from.x * 70.dp.toPx()
                val fy = cy + from.y * 50.dp.toPx()
                val tx = cx + to.x * 70.dp.toPx()
                val ty = cy + to.y * 50.dp.toPx()

                val px = fx + (tx - fx) * p.progress
                val py = fy + (ty - fy) * p.progress

                val pColor = when {
                    p.isError -> Color.Red
                    p.isCacheHit -> Color(0xFF10B981) // Green hit
                    else -> Color(0xFF38BDF8) // Blue request
                }

                drawCircle(
                    color = pColor,
                    radius = 4.dp.toPx(),
                    center = Offset(px, py)
                )
                drawCircle(
                    color = pColor.copy(alpha = 0.4f),
                    radius = 8.dp.toPx(),
                    center = Offset(px, py)
                )
            }

            // 4. Draw 3D Isometric Nodes
            val nodeW = 80.dp.toPx()
            val nodeH = 44.dp.toPx()

            engine.nodes.forEach { node ->
                val nx = cx + node.x * 70.dp.toPx()
                val ny = cy + node.y * 50.dp.toPx()

                val nodeColor = when (node.status) {
                    SimNodeStatus.FAILED -> Color(0xFF475569)
                    SimNodeStatus.OVERLOADED -> Color(0xFFEF4444)
                    SimNodeStatus.DEGRADED -> Color(0xFFF59E0B)
                    SimNodeStatus.HEALTHY -> when (node.type) {
                        SimNodeType.CLIENT -> Color(0xFF0EA5E9)
                        SimNodeType.LOAD_BALANCER -> Color(0xFF10B981)
                        SimNodeType.CACHE -> Color(0xFFF59E0B)
                        SimNodeType.DATABASE, SimNodeType.REPLICA -> Color(0xFF8B5CF6)
                        SimNodeType.QUEUE -> Color(0xFFEC4899)
                        else -> Color(0xFF6366F1)
                    }
                }

                // Node Base Shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(nx - nodeW / 2 + 4f, ny + nodeH / 2 - 4f),
                    size = Size(nodeW - 8f, 16.dp.toPx())
                )

                // Node 3D Box
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(nx - nodeW / 2, ny - nodeH / 2),
                    size = Size(nodeW, nodeH),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )

                drawRoundRect(
                    color = nodeColor,
                    topLeft = Offset(nx - nodeW / 2, ny - nodeH / 2),
                    size = Size(nodeW, nodeH),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = if (node.status == SimNodeStatus.OVERLOADED) 3.dp.toPx() else 1.5.dp.toPx())
                )

                // CPU Load bar inside server node
                if (node.type == SimNodeType.SERVER) {
                    val barW = (nodeW - 20f) * node.cpuLoad
                    drawRect(
                        color = if (node.cpuLoad > 0.85f) Color.Red else Color(0xFF10B981),
                        topLeft = Offset(nx - nodeW / 2 + 10f, ny + nodeH / 2 - 8.dp.toPx()),
                        size = Size(barW, 3.dp.toPx())
                    )
                }

                // Node text
                val titleLayout = textMeasurer.measure(
                    text = AnnotatedString(node.name),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                drawText(
                    textLayoutResult = titleLayout,
                    topLeft = Offset(nx - titleLayout.size.width / 2, ny - 14.dp.toPx())
                )

                val statusLayout = textMeasurer.measure(
                    text = AnnotatedString(
                        if (node.status == SimNodeStatus.FAILED) "OFFLINE"
                        else if (node.type == SimNodeType.SERVER) "${(node.cpuLoad * 100).toInt()}% CPU"
                        else "${node.type.name}"
                    ),
                    style = TextStyle(
                        color = if (node.status == SimNodeStatus.FAILED) Color.Red else Color(0xFF94A3B8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                drawText(
                    textLayoutResult = statusLayout,
                    topLeft = Offset(nx - statusLayout.size.width / 2, ny + 2.dp.toPx())
                )
            }
        }
    }
}
