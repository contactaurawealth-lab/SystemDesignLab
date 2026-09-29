package com.systemdesignlab.app.feature.learningmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.model.KnowledgeNode
import com.systemdesignlab.app.data.model.LearningMapData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun LearningMapScreen(
    mapData: LearningMapData,
    onOpenLesson: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var selectedNode by remember { mutableStateOf<KnowledgeNode?>(null) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 2.5f)
        offset += offsetChange
    }

    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Interactive Learning Map", fontWeight = FontWeight.Bold)
                        Text("Pan & zoom to inspect concept dependencies", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF070B16))
                .padding(innerPadding)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = state)
                    .pointerInput(mapData.nodes) {
                        detectTapGestures { tapOffset ->
                            val localTapX = (tapOffset.x - offset.x) / scale
                            val localTapY = (tapOffset.y - offset.y) / scale

                            val hit = mapData.nodes.find { node ->
                                val row = (node.order - 1) / 3
                                val col = (node.order - 1) % 3
                                val nx = 70.dp.toPx() + col * 125.dp.toPx()
                                val ny = 80.dp.toPx() + row * 110.dp.toPx()
                                val dx = localTapX - nx
                                val dy = localTapY - ny
                                (dx * dx + dy * dy) < (50 * 50)
                            }
                            selectedNode = hit
                        }
                    }
            ) {
                val nodeWidth = 105.dp.toPx()
                val nodeHeight = 52.dp.toPx()

                // Draw dependency connections between nodes
                for (i in 0 until mapData.nodes.size - 1) {
                    val curr = mapData.nodes[i]
                    val next = mapData.nodes[i + 1]

                    val r1 = (curr.order - 1) / 3
                    val c1 = (curr.order - 1) % 3
                    val x1 = 70.dp.toPx() + c1 * 125.dp.toPx()
                    val y1 = 80.dp.toPx() + r1 * 110.dp.toPx()

                    val r2 = (next.order - 1) / 3
                    val c2 = (next.order - 1) % 3
                    val x2 = 70.dp.toPx() + c2 * 125.dp.toPx()
                    val y2 = 80.dp.toPx() + r2 * 110.dp.toPx()

                    drawLine(
                        color = if (curr.isCompleted && next.isUnlocked) AccentIndigo else Color(0xFF1E293B),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Draw Knowledge Nodes
                mapData.nodes.forEach { node ->
                    val row = (node.order - 1) / 3
                    val col = (node.order - 1) % 3
                    val nx = 70.dp.toPx() + col * 125.dp.toPx()
                    val ny = 80.dp.toPx() + row * 110.dp.toPx()

                    val isSelected = selectedNode?.id == node.id

                    val borderColor = when {
                        isSelected -> Color(0xFF38BDF8)
                        node.isCompleted -> AccentEmerald
                        node.isUnlocked -> AccentIndigo
                        else -> Slate700
                    }

                    val bgColor = when {
                        node.isCompleted -> Color(0xFF0F291E)
                        node.isUnlocked -> Color(0xFF0F172A)
                        else -> Color(0xFF080C14)
                    }

                    // Node Card
                    drawRoundRect(
                        color = bgColor,
                        topLeft = Offset(nx - nodeWidth / 2, ny - nodeHeight / 2),
                        size = Size(nodeWidth, nodeHeight),
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )

                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(nx - nodeWidth / 2, ny - nodeHeight / 2),
                        size = Size(nodeWidth, nodeHeight),
                        cornerRadius = CornerRadius(10.dp.toPx()),
                        style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx())
                    )

                    // Node Title Text
                    val textLayout = textMeasurer.measure(
                        text = AnnotatedString(node.label),
                        style = TextStyle(
                            color = if (node.isUnlocked) Color.White else Slate600,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(nx - textLayout.size.width / 2, ny - 14.dp.toPx())
                    )

                    // Node Status Subtitle
                    val statusText = when {
                        node.isCompleted -> "COMPLETED"
                        node.isUnlocked -> "UNLOCKED"
                        else -> "LOCKED"
                    }
                    val statusLayout = textMeasurer.measure(
                        text = AnnotatedString(statusText),
                        style = TextStyle(
                            color = if (node.isCompleted) AccentEmerald else if (node.isUnlocked) AccentIndigo else Slate600,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = statusLayout,
                        topLeft = Offset(nx - statusLayout.size.width / 2, ny + 4.dp.toPx())
                    )
                }
            }

            // Node Inspector Bottom Sheet
            selectedNode?.let { node ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = node.label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (node.isCompleted) {
                                Text("✓ Completed", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else if (!node.isUnlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccentRose, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Locked", color = AccentRose, fontSize = 12.sp)
                                }
                            }
                        }

                        if (!node.isUnlocked && node.prerequisites.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Prerequisites required: ${node.prerequisites.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val id = node.id
                                selectedNode = null
                                onOpenLesson(id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Launch Lesson")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
