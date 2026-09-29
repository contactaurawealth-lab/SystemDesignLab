package com.systemdesignlab.app.feature.audiobook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.core.audio.AudiobookManager
import com.systemdesignlab.app.core.audio.AudiobookPlayerState
import com.systemdesignlab.app.core.ui.theme.*
import com.systemdesignlab.app.data.model.AudiobookChapter

@Composable
fun AudiobookScreen(
    chapters: List<AudiobookChapter>,
    audiobookManager: AudiobookManager,
    onJumpToLesson: (String) -> Unit,
    onJumpToSimulation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by audiobookManager.playerState.collectAsState()

    val currentChapter = chapters.find { it.id == playerState.currentChapterId }
        ?: chapters.firstOrNull()

    // Determine current visual sync point
    val currentSec = (playerState.currentPositionMs / 1000).toInt()
    val activeSyncPoint = currentChapter?.visualSyncPoints
        ?.filter { it.timestampSeconds <= currentSec }
        ?.maxByOrNull { it.timestampSeconds }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Chapter Artwork & Title Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(Slate700, Slate800))
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Artwork Box
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(AccentIndigo, AccentSky))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Audiobook",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CHAPTER 0${currentChapter?.chapterNumber ?: 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentSky,
                            letterSpacing = 1.2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentChapter?.title ?: "Foundations of Distributed Systems",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentChapter?.subtitle ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )
                }
            }
        }

        // 2. Audio + Visual Sync Concept Highlight Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (playerState.isPlaying) AccentEmerald else Slate600)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUDIO + VISUAL SYNC",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentEmerald,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Text(
                            text = if (playerState.isPlaying) "Live Narration" else "Paused",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeSyncPoint?.caption ?: "Listen to master architectural trade-offs and scaling concepts.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeSyncPoint?.let { sync ->
                            OutlinedButton(
                                onClick = { onJumpToLesson(sync.conceptId) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Jump to Lesson", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onJumpToSimulation("sim-load-balancer") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simulate", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Audio Player Transport Controls & Scrubbing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val pos = playerState.currentPositionMs.toFloat()
                    val dur = playerState.durationMs.coerceAtLeast(1L).toFloat()

                    Slider(
                        value = (pos / dur).coerceIn(0f, 1f),
                        onValueChange = { frac ->
                            audiobookManager.seekTo((frac * dur).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentIndigo,
                            activeTrackColor = AccentIndigo,
                            inactiveTrackColor = Slate800
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(playerState.currentPositionMs),
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                        Text(
                            text = formatTime(playerState.durationMs),
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Transport Buttons: Prev, -15s, Play/Pause, +15s, Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val currIdx = chapters.indexOfFirst { it.id == currentChapter?.id }
                                if (currIdx > 0) {
                                    val prev = chapters[currIdx - 1]
                                    audiobookManager.playChapter(prev.id, prev.audioAssetPath)
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Slate400)
                        }

                        IconButton(onClick = { audiobookManager.skipBackward(15) }) {
                            Icon(imageVector = Icons.Default.Replay10, contentDescription = "-15s", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                if (playerState.currentChapterId == null && currentChapter != null) {
                                    audiobookManager.playChapter(currentChapter.id, currentChapter.audioAssetPath)
                                } else {
                                    audiobookManager.togglePlayPause()
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AccentIndigo)
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = { audiobookManager.skipForward(15) }) {
                            Icon(imageVector = Icons.Default.Forward10, contentDescription = "+15s", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                val currIdx = chapters.indexOfFirst { it.id == currentChapter?.id }
                                if (currIdx in 0 until chapters.size - 1) {
                                    val next = chapters[currIdx + 1]
                                    audiobookManager.playChapter(next.id, next.audioAssetPath)
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = Slate400)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate800)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Speed & Sleep Timer Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSpeedDialog = true }) {
                            Text(
                                text = "Speed: ${playerState.playbackSpeed}x",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }

                        TextButton(onClick = { showSleepTimerDialog = true }) {
                            Text(
                                text = if (playerState.sleepTimerRemainingSec != null) {
                                    "Timer: ${playerState.sleepTimerRemainingSec!! / 60}m"
                                } else {
                                    "Sleep Timer"
                                },
                                color = if (playerState.sleepTimerRemainingSec != null) AccentEmerald else Slate400,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Chapter Playlist
        item {
            Text(
                text = "COURSE AUDIO CHAPTERS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
            )
        }

        items(chapters) { ch ->
            val isCurrent = ch.id == currentChapter?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        audiobookManager.playChapter(ch.id, ch.audioAssetPath)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Slate800 else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = if (isCurrent) {
                        androidx.compose.ui.graphics.SolidColor(AccentIndigo)
                    } else {
                        androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chapter 0${ch.chapterNumber}: ${ch.title}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = ch.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${ch.durationSeconds / 60} min",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                }
            }
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed") },
            text = {
                Column {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { spd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audiobookManager.setPlaybackSpeed(spd)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${spd}x")
                            if (playerState.playbackSpeed == spd) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AccentEmerald)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Close") }
            }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Sleep Timer") },
            text = {
                Column {
                    listOf(0 to "Off", 5 to "5 Minutes", 15 to "15 Minutes", 30 to "30 Minutes").forEach { (min, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audiobookManager.setSleepTimer(min)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label)
                            if (playerState.sleepTimerMinutes == min) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AccentEmerald)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) { Text("Close") }
            }
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format("%02d:%02d", m, s)
}
