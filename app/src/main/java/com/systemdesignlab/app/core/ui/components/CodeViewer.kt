package com.systemdesignlab.app.core.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeViewer(
    code: String,
    language: String = "yaml",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fontSizeSp by remember { mutableStateOf(13) }
    val scrollState = rememberScrollState()

    val lines = remember(code) { code.lines() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
    ) {
        // Top action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        fontSizeSp = if (fontSizeSp == 13) 15 else if (fontSizeSp == 15) 11 else 13
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "Toggle font size",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Code content with line numbers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .horizontalScroll(scrollState)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lines.size) {
                    Text(
                        text = "$i",
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSizeSp.sp,
                        color = Color(0xFF475569),
                        lineHeight = (fontSizeSp + 6).sp
                    )
                }
            }

            // Code lines with syntax highlighting
            Column(
                modifier = Modifier.padding(end = 16.dp)
            ) {
                for (line in lines) {
                    Text(
                        text = highlightCodeLine(line, language),
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp + 6).sp
                    )
                }
            }
        }
    }
}

private fun highlightCodeLine(line: String, language: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val trimmed = line.trimStart()
        if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
            // Comments
            withStyle(SpanStyle(color = Color(0xFF64748B))) {
                append(line)
            }
        } else if (trimmed.startsWith("\"") && trimmed.endsWith("\",")) {
            // String literal
            withStyle(SpanStyle(color = Color(0xFF38BDF8))) {
                append(line)
            }
        } else if (line.contains(":")) {
            // Key-value or config
            val parts = line.split(":", limit = 2)
            withStyle(SpanStyle(color = Color(0xFFA5B4FC), fontWeight = FontWeight.SemiBold)) {
                append(parts[0])
            }
            append(":")
            if (parts.size > 1) {
                withStyle(SpanStyle(color = Color(0xFFFDE047))) {
                    append(parts[1])
                }
            }
        } else {
            // Standard code
            withStyle(SpanStyle(color = Color(0xFFE2E8F0))) {
                append(line)
            }
        }
    }
}
