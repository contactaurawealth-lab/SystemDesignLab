package com.systemdesignlab.app.feature.tools

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemdesignlab.app.core.ui.theme.*
import kotlinx.coroutines.delay

enum class ReaderTool(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val subtitle: String
) {
    FERMI("fermi", "Fermi Math", Icons.Default.Calculate, "Back-of-envelope sizing"),
    SCALE("scale", "Scale Slider", Icons.Default.TrendingUp, "1k → 50M evolution"),
    POSTMORTEM("postmortem", "Post-Mortems", Icons.Default.Warning, "AWS, Cloudflare & Meta outages"),
    EXPLAIN("explain", "Feynman Recall", Icons.Default.RecordVoiceOver, "Explain-it-back evaluator"),
    BATTLE("battle", "Trade-Off Arena", Icons.Default.CompareArrows, "Head-to-head comparisons"),
    DISSECTOR("dissector", "Packet Dissector", Icons.Default.Layers, "TCP, TLS & HTTP/2 frames"),
    CODE("code", "Code Linker", Icons.Default.Code, "Theory to production code"),
    PAPERS("papers", "Seminal Papers", Icons.Default.MenuBook, "GFS, Dynamo & Raft in 5m"),
    RADAR("radar", "Weak-Spot Radar", Icons.Default.Shield, "SPOF diagnostic"),
    EXPORTER("export", "Note Exporter", Icons.Default.Share, "Obsidian & Anki export")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderToolsScreen(
    initialToolId: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember {
        mutableStateOf(
            ReaderTool.entries.find { it.id == initialToolId } ?: ReaderTool.FERMI
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Engineering Reader Tools",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Interactive deep-dive companions",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal tool selector scroller
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ReaderTool.entries) { tool ->
                    val isSelected = tool == selectedTool
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedTool = tool },
                        color = if (isSelected) AccentIndigo else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = if (isSelected) 4.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Tool container
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTool) {
                    ReaderTool.FERMI -> FermiMathTool()
                    ReaderTool.SCALE -> ScaleEvolutionTool()
                    ReaderTool.POSTMORTEM -> IncidentPostMortemTool()
                    ReaderTool.EXPLAIN -> FeynmanExplainTool()
                    ReaderTool.BATTLE -> TradeOffBattleTool()
                    ReaderTool.DISSECTOR -> ProtocolPacketDissectorTool()
                    ReaderTool.CODE -> TheoryCodeLinkerTool()
                    ReaderTool.PAPERS -> SeminalPapersTool()
                    ReaderTool.RADAR -> WeakSpotRadarTool()
                    ReaderTool.EXPORTER -> NoteExporterTool()
                }
            }
        }
    }
}

// ==========================================
// 1. FERMI MATH TOOL
// ==========================================
@Composable
fun FermiMathTool() {
    var dauMillion by remember { mutableFloatStateOf(300f) }
    var actionsPerUser by remember { mutableFloatStateOf(5f) }
    var readRatio by remember { mutableIntStateOf(50) } // 50:1

    val totalDau = dauMillion.toLong() * 1_000_000L
    val totalRequestsPerDay = totalDau * actionsPerUser.toLong()
    val writeRatio = 1
    val writeRequestsPerDay = totalRequestsPerDay / (readRatio + writeRatio)
    val readRequestsPerDay = writeRequestsPerDay * readRatio

    val writeQps = writeRequestsPerDay / 86400
    val readQps = readRequestsPerDay / 86400
    val peakReadQps = (readQps * 2.5).toLong()

    val storagePerMonthGb = (writeRequestsPerDay * 30 * 2048) / (1024L * 1024L * 1024L) // 2KB payload
    val ramWorkingSetGb = (readRequestsPerDay * 0.20 * 512) / (1024L * 1024L * 1024L) // 80/20 rule

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📐 Back-of-the-Envelope Fermi Calculator",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Estimate system scale in 10 seconds before writing architectural specs.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // DAU Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Active Users (DAU)", style = MaterialTheme.typography.labelMedium)
                        Text("${dauMillion.toInt()} Million", fontWeight = FontWeight.Bold, color = AccentIndigo)
                    }
                    Slider(
                        value = dauMillion,
                        onValueChange = { dauMillion = it },
                        valueRange = 10f..1000f,
                        steps = 98
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Actions slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Actions per User / Day", style = MaterialTheme.typography.labelMedium)
                        Text("${actionsPerUser.toInt()} reqs", fontWeight = FontWeight.Bold, color = AccentEmerald)
                    }
                    Slider(
                        value = actionsPerUser,
                        onValueChange = { actionsPerUser = it },
                        valueRange = 1f..50f,
                        steps = 49
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Read:Write Ratio
                    Text("Read to Write Ratio", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 50, 100).forEach { ratio ->
                            FilterChip(
                                selected = readRatio == ratio,
                                onClick = { readRatio = ratio },
                                label = { Text("$ratio:1") }
                            )
                        }
                    }
                }
            }
        }

        // Live calculation results grid
        item {
            Text("Calculated Sizing Metrics", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricResultCard(
                        title = "Write QPS (Avg)",
                        value = "$writeQps",
                        unit = "req/s",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    MetricResultCard(
                        title = "Peak Read QPS (2.5x)",
                        value = "$peakReadQps",
                        unit = "req/s",
                        color = AccentSky,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricResultCard(
                        title = "New Storage / Month",
                        value = "$storagePerMonthGb",
                        unit = "GB/mo",
                        color = AccentEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    MetricResultCard(
                        title = "RAM Working Set (80/20)",
                        value = "$ramWorkingSetGb",
                        unit = "GB RAM",
                        color = AccentAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Explanation Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🧮 SRE Mathematical Formulation",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Total Daily Requests = ${dauMillion.toInt()}M × ${actionsPerUser.toInt()} = ${(totalRequestsPerDay / 1_000_000)}M req/day\n" +
                               "• Seconds in a Day = 86,400s\n" +
                               "• Total QPS = ${(totalRequestsPerDay / 86400)} req/s\n" +
                               "• Write QPS = $writeQps | Read QPS = $readQps (Peak = $peakReadQps req/s)\n" +
                               "• 80/20 Cache Working Set = Top 20% of reads served from Redis RAM ($ramWorkingSetGb GB)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate600,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MetricResultCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

// ==========================================
// 2. SCALE EVOLUTION SLIDER (1K → 50M)
// ==========================================
@Composable
fun ScaleEvolutionTool() {
    var stageIndex by remember { mutableIntStateOf(1) } // 0 to 4

    val stages = listOf(
        ScaleStage(
            users = "1k Users",
            day = "Day 1",
            name = "Single Droplet Monolith",
            arch = "Client ➔ Monolith (Node/Go) ➔ SQLite / Local DB",
            bottleneck = "Single Point of Failure (SPOF); process crashes drop all active users.",
            unlock = "Package monolith into container, configure automated daily backups.",
            tradeoff = "Zero operational overhead, but no redundancy."
        ),
        ScaleStage(
            users = "50k Users",
            day = "Day 30",
            name = "Decoupled Database & Caching",
            arch = "Client ➔ App Server ➔ Redis (Cache-Aside) ➔ PostgreSQL",
            bottleneck = "Relational database connection limits & expensive join queries.",
            unlock = "Introduce Redis read-aside caching for hot queries and user sessions.",
            tradeoff = "Cache invalidation complexity; temporary eventual consistency on writes."
        ),
        ScaleStage(
            users = "500k Users",
            day = "Day 100",
            name = "Horizontal App Cluster + Read Replicas",
            arch = "Client ➔ L7 Reverse Proxy (Nginx) ➔ 4x Stateless Apps ➔ Master/Replica DB + CDN",
            bottleneck = "Database write throughput & asset delivery bandwidth.",
            unlock = "Stateless application servers + static assets offloaded to Cloudflare CDN.",
            tradeoff = "Replication lag on read replicas can cause stale data reads."
        ),
        ScaleStage(
            users = "5M Users",
            day = "Day 365",
            name = "Asynchronous Decoupling & Sharding",
            arch = "Edge CDN ➔ API Gateway ➔ App Cluster ➔ Kafka Event Bus ➔ Sharded DBs (Hash Ring)",
            bottleneck = "Synchronous HTTP cascades and single primary database write capacity.",
            unlock = "Kafka queue for async writes + consistent hashing database sharding.",
            tradeoff = "Cross-shard joins are impossible; distributed transactions require Saga patterns."
        ),
        ScaleStage(
            users = "50M Users",
            day = "Day 1000",
            name = "Multi-Region Active-Active Mesh",
            arch = "Global Anycast DNS ➔ Multi-Region Gateways ➔ Service Mesh (Envoy/gRPC) ➔ Distributed Spanner",
            bottleneck = "Speed of light cross-continent latency & regional datacenter blackouts.",
            unlock = "Active-active multi-region deployment with Paxos/Raft consensus datastores.",
            tradeoff = "CAP Theorem trade-offs; conflict resolution & complex split-brain recovery."
        )
    )

    val currentStage = stages[stageIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📈 1k → 50M System Scale Evolution",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Scrub through growth milestones to see how architectural bottlenecks emerge and get solved.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentStage.day, style = MaterialTheme.typography.labelSmall.copy(color = AccentSky, fontWeight = FontWeight.Bold))
                        Text(currentStage.users, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AccentIndigo))
                    }

                    Slider(
                        value = stageIndex.toFloat(),
                        onValueChange = { stageIndex = it.toInt() },
                        valueRange = 0f..4f,
                        steps = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1k", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                        Text("50k", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                        Text("500k", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                        Text("5M", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                        Text("50M", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AccentIndigo.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${stageIndex + 1}", fontWeight = FontWeight.Bold, color = AccentIndigo)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentStage.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("TOPOLOGY FLOW", style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.Bold))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = currentStage.arch,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    EvolutionItemRow(
                        label = "CRITICAL BOTTLENECK",
                        text = currentStage.bottleneck,
                        icon = Icons.Default.Warning,
                        tint = AccentRose
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    EvolutionItemRow(
                        label = "ENGINEERING UNLOCK",
                        text = currentStage.unlock,
                        icon = Icons.Default.CheckCircle,
                        tint = AccentEmerald
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    EvolutionItemRow(
                        label = "ACCEPTED TRADE-OFF",
                        text = currentStage.tradeoff,
                        icon = Icons.Default.Balance,
                        tint = AccentAmber
                    )
                }
            }
        }
    }
}

@Composable
fun EvolutionItemRow(
    label: String,
    text: String,
    icon: ImageVector,
    tint: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = tint, fontWeight = FontWeight.Bold))
            Text(text = text, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
        }
    }
}

data class ScaleStage(
    val users: String,
    val day: String,
    val name: String,
    val arch: String,
    val bottleneck: String,
    val unlock: String,
    val tradeoff: String
)

// ==========================================
// 3. INCIDENT POST-MORTEMS
// ==========================================
@Composable
fun IncidentPostMortemTool() {
    var selectedIncident by remember { mutableStateOf("s3") }

    val incidents = mapOf(
        "s3" to IncidentData(
            title = "AWS S3 US-EAST-1 Blackout (Feb 2017)",
            duration = "4 Hours 17 Minutes",
            impact = "Thousands of tier-1 web services (Slack, Trello, Quora) crashed worldwide.",
            rootCause = "A typed command during routine debugging intended to take a few servers offline contained a typo that decommissioned an entire critical subsystem of the S3 placement service.",
            cascade = "The remaining index servers could not handle the sudden rerouted load. When the subsystem was restarted, cold bootstrap caches took 4+ hours to index billions of storage nodes.",
            architecturalFix = "1) Decouple bootstrap dependencies so subsystems can warm restart without circular dependencies.\n2) Partition blast radius into strict multi-tenant cells with automated rate-limits on operational commands."
        ),
        "cloudflare" to IncidentData(
            title = "Cloudflare Global CPU ReDoS Collapse (July 2019)",
            duration = "27 Minutes",
            impact = "502 Bad Gateway errors across roughly 12% of worldwide internet HTTP traffic.",
            rootCause = "A new regex rule added to Cloudflare's Web Application Firewall (WAF) to detect XSS contained an unanchored pattern with nested repetitions (`.*.*`).",
            cascade = "A request containing a specific whitespace sequence triggered catastrophic backtracking (ReDoS). All CPU cores on edge proxies hit 100% utilization instantly.",
            architecturalFix = "1) Transition all edge inspection engines to non-backtracking automata engines (RE2 or Rust regex).\n2) Enforce strict CPU runtime execution quotas per inspection rule."
        ),
        "meta" to IncidentData(
            title = "Meta Global Backbone Peering Sever (Oct 2021)",
            duration = "5 Hours 45 Minutes",
            impact = "Facebook, Instagram, WhatsApp, and internal employee access completely unresolvable.",
            rootCause = "A routine maintenance script intended to assess global backbone capacity accidentally withdrew all BGP routes advertising Facebook's authoritative DNS servers to the internet.",
            cascade = "Because DNS could not resolve, internal engineers could not VPN into servers. Keycards to physical datacenter cages failed because authentication servers were unreachable.",
            architecturalFix = "1) Implement an out-of-band management network completely isolated from the production DNS backbone.\n2) Automated sanity verification that blocks configuration pushes that sever network routes."
        )
    )

    val current = incidents[selectedIncident] ?: incidents["s3"]!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🔥 Real-World Production Post-Mortems", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Learn system design from the costliest architectural outages in history.", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("s3" to "AWS S3 (2017)", "cloudflare" to "Cloudflare (2019)", "meta" to "Meta (2021)").forEach { (id, label) ->
                    FilterChip(
                        selected = selectedIncident == id,
                        onClick = { selectedIncident = id },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(current.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Outage Duration: ${current.duration}", style = MaterialTheme.typography.labelSmall.copy(color = AccentRose, fontWeight = FontWeight.Bold))

                    Spacer(modifier = Modifier.height(12.dp))

                    IncidentSection(label = "GLOBAL BLAST RADIUS", text = current.impact, color = AccentRose)
                    Spacer(modifier = Modifier.height(10.dp))
                    IncidentSection(label = "TRIGGER & ROOT CAUSE", text = current.rootCause, color = AccentAmber)
                    Spacer(modifier = Modifier.height(10.dp))
                    IncidentSection(label = "CASCADING FAILURE MECHANISM", text = current.cascade, color = AccentSky)
                    Spacer(modifier = Modifier.height(10.dp))
                    IncidentSection(label = "SYSTEMIC ARCHITECTURAL PREVENTATIVE", text = current.architecturalFix, color = AccentEmerald)
                }
            }
        }
    }
}

@Composable
fun IncidentSection(label: String, text: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp))
    }
}

data class IncidentData(
    val title: String,
    val duration: String,
    val impact: String,
    val rootCause: String,
    val cascade: String,
    val architecturalFix: String
)

// ==========================================
// 4. FEYNMAN EXPLAIN-IT-BACK TOOL
// ==========================================
@Composable
fun FeynmanExplainTool() {
    var selectedConcept by remember { mutableStateOf("Consistent Hashing") }
    var userExplanation by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var evaluationResult by remember { mutableStateOf<EvaluationScore?>(null) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎙️ Feynman \"Explain-It-Back\" Protocol",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "If you cannot explain a concept simply to a peer, you do not truly understand it.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Text("Select Architectural Concept:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Consistent Hashing", "Raft Consensus", "Token Bucket").forEach { concept ->
                            FilterChip(
                                selected = selectedConcept == concept,
                                onClick = {
                                    selectedConcept = concept
                                    evaluationResult = null
                                },
                                label = { Text(concept) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Your Explanation", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                        // Voice simulation button
                        Button(
                            onClick = {
                                isRecording = !isRecording
                                if (!isRecording && userExplanation.isEmpty()) {
                                    userExplanation = "Consistent hashing maps nodes and keys to a 360-degree hash ring. Virtual nodes prevent hot spots, and adding a server only moves K/N keys rather than all keys."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) AccentRose else AccentIndigo
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRecording) "Recording ($recordingSeconds s)" else "Voice Simulation")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = userExplanation,
                        onValueChange = { userExplanation = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = { Text("Explain how $selectedConcept works in simple terms, without buzzwords...") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val lower = userExplanation.lowercase()
                            val score = when (selectedConcept) {
                                "Consistent Hashing" -> {
                                    var s = 50
                                    if (lower.contains("ring") || lower.contains("circle")) s += 15
                                    if (lower.contains("virtual") || lower.contains("replica")) s += 15
                                    if (lower.contains("k/n") || lower.contains("minimal") || lower.contains("rebalance")) s += 15
                                    EvaluationScore(
                                        score = s,
                                        feedback = if (s > 80) "Excellent! You highlighted the hash ring, virtual nodes for uniform distribution, and minimal remapping." else "Good start. Make sure to explain virtual nodes and why standard modulo hashing causes full cache invalidation."
                                    )
                                }
                                "Raft Consensus" -> {
                                    var s = 50
                                    if (lower.contains("leader") || lower.contains("follower")) s += 20
                                    if (lower.contains("heartbeat") || lower.contains("election")) s += 15
                                    if (lower.contains("log") || lower.contains("majority")) s += 10
                                    EvaluationScore(
                                        score = s,
                                        feedback = "Raft hinges on Leader Election, Log Replication, and Safety Invariants with odd-numbered quorums (2F+1)."
                                    )
                                }
                                else -> {
                                    EvaluationScore(
                                        score = 85,
                                        feedback = "Solid grasp of token generation rate and bucket capacity for burst traffic absorption."
                                    )
                                }
                            }
                            evaluationResult = score
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Evaluate My Explanation")
                    }
                }
            }
        }

        // Evaluation result card
        evaluationResult?.let { eval ->
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (eval.score >= 80) Color(0x1A10B981) else Color(0x1AF59E0B)
                    ),
                    border = BorderStroke(1.dp, if (eval.score >= 80) AccentEmerald else AccentAmber)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Score: ${eval.score} / 100",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (eval.score >= 80) AccentEmerald else AccentAmber
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = eval.feedback,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }
            }
        }
    }
}

data class EvaluationScore(val score: Int, val feedback: String)

// ==========================================
// 5. TRADE-OFF BATTLE ARENA
// ==========================================
@Composable
fun TradeOffBattleTool() {
    var selectedMatchup by remember { mutableStateOf("kafka_rabbit") }
    var selectedScenarioOutcome by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("⚔️ Trade-Off Battle Arena", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Architecture is the art of choosing the right set of problems.", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedMatchup == "kafka_rabbit",
                    onClick = {
                        selectedMatchup = "kafka_rabbit"
                        selectedScenarioOutcome = null
                    },
                    label = { Text("Kafka vs RabbitMQ") }
                )
                FilterChip(
                    selected = selectedMatchup == "sql_nosql",
                    onClick = {
                        selectedMatchup = "sql_nosql"
                        selectedScenarioOutcome = null
                    },
                    label = { Text("PostgreSQL vs Cassandra") }
                )
            }
        }

        if (selectedMatchup == "kafka_rabbit") {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ContenderCard(
                        title = "Apache Kafka",
                        role = "Immutable Distributed Log",
                        pros = "• Massive throughput (1M+ msg/s)\n• Replayability & time-travel\n• Partition-level strict ordering",
                        cons = "• Complex ZooKeeper/KRaft cluster\n• Coarse consumer offset routing",
                        modifier = Modifier.weight(1f)
                    )
                    ContenderCard(
                        title = "RabbitMQ",
                        role = "Smart Broker Message Queue",
                        pros = "• Flexible routing (Exchange/Topic)\n• Per-message acknowledgment\n• Native priority queues",
                        cons = "• Lower throughput under deep queues\n• Message deleted upon consumption",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🎯 Production Scenario Challenge", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = AccentIndigo))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Scenario: You are designing an Uber-like driver GPS telemetry ingestion pipeline receiving 2,000,000 coordinate pings per second. Which do you choose?",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    selectedScenarioOutcome = "✔ Correct! Kafka’s append-only sequential disk writes and partition parallelism easily ingest millions of telemetry points, allowing multiple consumers (fraud, ETA calculation, map display) to read the same stream."
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Choose Kafka")
                            }
                            OutlinedButton(
                                onClick = {
                                    selectedScenarioOutcome = "✖ Suboptimal. RabbitMQ's per-message state tracking and RAM queue queues will exhaust broker resources under millions of streaming coordinate packets."
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Choose RabbitMQ")
                            }
                        }

                        selectedScenarioOutcome?.let { outcome ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (outcome.startsWith("✔")) Color(0x1A10B981) else Color(0x1AF43F5E),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = outcome,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ContenderCard(
                        title = "PostgreSQL",
                        role = "ACID Relational Datastore",
                        pros = "• Strong linearizability\n• Complex multi-table joins\n• Rich ecosystem (JSONB, PostGIS)",
                        cons = "• Master write bottleneck\n• Sharding is non-trivial",
                        modifier = Modifier.weight(1f)
                    )
                    ContenderCard(
                        title = "Apache Cassandra",
                        role = "Peer-to-Peer Wide-Column (AP)",
                        pros = "• Linear write scaling\n• No single point of failure\n• Multi-datacenter native replication",
                        cons = "• No joins or transactions\n• Eventual consistency read repairs",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ContenderCard(
    title: String,
    role: String,
    pros: String,
    cons: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(role, style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
            Spacer(modifier = Modifier.height(8.dp))
            Text("STRENGTHS", style = MaterialTheme.typography.labelSmall.copy(color = AccentEmerald, fontWeight = FontWeight.Bold))
            Text(pros, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("WEAKNESSES", style = MaterialTheme.typography.labelSmall.copy(color = AccentRose, fontWeight = FontWeight.Bold))
            Text(cons, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp))
        }
    }
}

// ==========================================
// 6. PROTOCOL PACKET DISSECTOR
// ==========================================
@Composable
fun ProtocolPacketDissectorTool() {
    var selectedProtocol by remember { mutableStateOf("tcp") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🔬 Protocol Packet & Frame Dissector", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Inspect wire-level byte headers to understand latency, overhead, and handshakes.", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("tcp" to "TCP 3-Way Handshake", "tls" to "TLS 1.3 Key Exchange", "http2" to "HTTP/2 Frames").forEach { (id, label) ->
                    FilterChip(
                        selected = selectedProtocol == id,
                        onClick = { selectedProtocol = id },
                        label = { Text(label) }
                    )
                }
            }
        }

        when (selectedProtocol) {
            "tcp" -> {
                item {
                    PacketStepCard(
                        step = "Step 1 of 3: Client ➔ Server",
                        packet = "SYN Packet",
                        flag = "[SYN] Seq = 1000, Ack = 0, Win = 65535, MSS = 1460",
                        purpose = "Client establishes initial sequence number and negotiates Maximum Segment Size."
                    )
                }
                item {
                    PacketStepCard(
                        step = "Step 2 of 3: Server ➔ Client",
                        packet = "SYN-ACK Packet",
                        flag = "[SYN, ACK] Seq = 8000, Ack = 1001, Win = 65535",
                        purpose = "Server acknowledges Client's sequence (Ack = 1001) and presents its own sequence."
                    )
                }
                item {
                    PacketStepCard(
                        step = "Step 3 of 3: Client ➔ Server",
                        packet = "ACK Packet (Connection Established)",
                        flag = "[ACK] Seq = 1001, Ack = 8001 (1 Full RTT Cost)",
                        purpose = "Socket transitions to ESTABLISHED; HTTP payload can now safely piggyback."
                    )
                }
            }
            "tls" -> {
                item {
                    PacketStepCard(
                        step = "Round Trip 1: ClientHello",
                        packet = "ClientHello (1-RTT in TLS 1.3)",
                        flag = "CipherSuites + KeyShare (ECDH Public Key: x25519)",
                        purpose = "Unlike TLS 1.2 which required 2 RTTs, TLS 1.3 speculatively transmits public key shares immediately."
                    )
                }
                item {
                    PacketStepCard(
                        step = "Round Trip 1: ServerHello",
                        packet = "ServerHello + EncryptedExtensions + Finished",
                        flag = "ServerKeyShare + HMAC Finished Tag",
                        purpose = "Server completes symmetric session key derivation. Immediate Application Data follows."
                    )
                }
            }
            else -> {
                item {
                    PacketStepCard(
                        step = "Binary Framing",
                        packet = "HTTP/2 Frame Header (9 Octets)",
                        flag = "Length (24b) | Type: DATA (8b) | Flags: END_STREAM (8b) | Stream ID (31b)",
                        purpose = "Streams multiplex over a single TCP connection, eliminating head-of-line blocking at the HTTP layer."
                    )
                }
            }
        }
    }
}

@Composable
fun PacketStepCard(step: String, packet: String, flag: String, purpose: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(step, style = MaterialTheme.typography.labelSmall.copy(color = AccentSky, fontWeight = FontWeight.Bold))
            Text(packet, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = flag,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(purpose, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

// ==========================================
// 7. THEORY-TO-CODE LINKER
// ==========================================
@Composable
fun TheoryCodeLinkerTool() {
    var selectedCode by remember { mutableStateOf("ratelimit") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("💻 Theory-to-Production Code Linker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("See how abstract system design theories are written in real production code.", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "ratelimit" to "Redis Rate Limiter (Lua)",
                    "hashring" to "Consistent Hash Ring (Go)",
                    "circuit" to "Circuit Breaker (Kotlin)"
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = selectedCode == id,
                        onClick = { selectedCode = id },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            val (title, lang, explanation, code) = when (selectedCode) {
                "ratelimit" -> Quad(
                    "Sliding Window Counter in Redis (Atomic Lua)",
                    "LUA",
                    "By executing via Redis Lua, all timestamp comparisons, zremrangebyscore, and counter insertions happen atomically on the single-threaded Redis engine without race conditions.",
                    """
-- KEYS[1]: User rate limit key (e.g. rate:user:1024)
-- ARGV[1]: Current UNIX epoch in milliseconds
-- ARGV[2]: Window size (e.g. 60000 ms)
-- ARGV[3]: Max allowed requests (e.g. 100)

local current_time = tonumber(ARGV[1])
local window_start = current_time - tonumber(ARGV[2])
local max_limit = tonumber(ARGV[3])

-- Remove requests outside current sliding window
redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', window_start)

-- Count remaining requests in window
local current_requests = redis.call('ZCARD', KEYS[1])

if current_requests < max_limit then
    -- Under limit: add current timestamp and grant access
    redis.call('ZADD', KEYS[1], current_time, current_time)
    redis.call('PEXPIRE', KEYS[1], ARGV[2])
    return 1 -- Allowed
else
    return 0 -- Throttled (HTTP 429)
end
                    """.trimIndent()
                )
                "hashring" -> Quad(
                    "Consistent Hash Ring with Virtual Nodes",
                    "GO",
                    "Virtual nodes ensure uniform hash distribution. Lookup is O(log N) via binary search over the sorted 32-bit ring array.",
                    """
type HashRing struct {
    virtualNodes int
    ring         []uint32          // Sorted hash points
    nodeMap      map[uint32]string // Hash -> Physical node address
    sync.RWMutex
}

func (h *HashRing) GetNode(key string) string {
    h.RLock()
    defer h.RUnlock()
    if len(h.ring) == 0 { return "" }

    hash := crc32.ChecksumIEEE([]byte(key))
    // Binary search on sorted ring for clockwise successor
    idx := sort.Search(len(h.ring), func(i int) bool {
        return h.ring[i] >= hash
    })

    if idx == len(h.ring) { idx = 0 } // Wrap around ring
    return h.nodeMap[h.ring[idx]]
}
                    """.trimIndent()
                )
                else -> Quad(
                    "Circuit Breaker Finite State Machine",
                    "KOTLIN",
                    "Prevents cascading service degradation by tripping to OPEN state upon reaching error thresholds, failing fast without tying up server threads.",
                    """
enum class CircuitState { CLOSED, OPEN, HALF_OPEN }

class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val resetTimeoutMs: Long = 10_000L
) {
    private var state = CircuitState.CLOSED
    private var failureCount = 0
    private var lastStateChange = System.currentTimeMillis()

    @Synchronized
    fun <T> execute(action: () -> T): T {
        val now = System.currentTimeMillis()
        if (state == CircuitState.OPEN && now - lastStateChange > resetTimeoutMs) {
            state = CircuitState.HALF_OPEN
        }
        if (state == CircuitState.OPEN) {
            throw ServiceUnavailableException("Circuit OPEN: Fast fail")
        }
        return try {
            val result = action()
            if (state == CircuitState.HALF_OPEN) {
                state = CircuitState.CLOSED
                failureCount = 0
            }
            result
        } catch (ex: Exception) {
            failureCount++
            if (failureCount >= failureThreshold) {
                state = CircuitState.OPEN
                lastStateChange = now
            }
            throw ex
        }
    }
}
                    """.trimIndent()
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentIndigo.copy(alpha = 0.15f)
                        ) {
                            Text(lang, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(color = AccentIndigo, fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(explanation, style = MaterialTheme.typography.bodySmall.copy(color = Slate600))

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

data class Quad(val first: String, val second: String, val third: String, val fourth: String)

// ==========================================
// 8. SEMINAL PAPERS DIGEST
// ==========================================
@Composable
fun SeminalPapersTool() {
    val papers = listOf(
        PaperDigest(
            title = "Google File System (GFS)",
            authors = "Sanjay Ghemawat, Howard Gobioff, Shun-Tak Leung (2003)",
            coreProblem = "Managing petabytes of data reliably on cheap, failing commodity hardware.",
            breakthrough = "Single master for metadata + 64MB large chunks stored across commodity Chunkservers with 3x replication and append-only semantics.",
            acceptedTradeoff = "Single master creates metadata RAM bottleneck; poor performance on millions of tiny files.",
            offspring = "Apache Hadoop HDFS, Ceph, Kubernetes persistent volume primitives."
        ),
        PaperDigest(
            title = "Amazon Dynamo",
            authors = "Giuseppe DeCandia et al. (2007)",
            coreProblem = "Shopping cart checkout must never drop a write even during catastrophic datacenter network partitions.",
            breakthrough = "Always-writable AP datastore utilizing Consistent Hashing, Vector Clocks, Sloppy Quorums (W + R > N), and Merkle tree anti-entropy.",
            acceptedTradeoff = "Eventual consistency: reads can return conflicting object versions that the client application must reconcile.",
            offspring = "Apache Cassandra, Riak, Amazon DynamoDB, ScyllaDB."
        ),
        PaperDigest(
            title = "In Search of an Understandable Consensus Algorithm (Raft)",
            authors = "Diego Ongaro and John Ousterhout, Stanford (2014)",
            coreProblem = "Paxos is notoriously difficult to comprehend, verify, and implement without subtle split-brain bugs.",
            breakthrough = "Decomposed distributed consensus into distinct, orthogonal subproblems: Leader Election, Log Replication, and Safety Invariants.",
            acceptedTradeoff = "Strict leader bottleneck: all write traffic must funnel through the elected leader.",
            offspring = "etcd (Kubernetes state store), HashiCorp Consul/Vault, CockroachDB, TiKV."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("📜 Seminal Systems Papers Digest", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("5-minute distillations of foundational whitepapers that created modern distributed systems.", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))
        }

        items(papers) { paper ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(paper.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(paper.authors, style = MaterialTheme.typography.labelSmall.copy(color = Slate400))

                    Spacer(modifier = Modifier.height(10.dp))
                    DigestRow(label = "CORE PROBLEM", text = paper.coreProblem, color = AccentRose)
                    Spacer(modifier = Modifier.height(6.dp))
                    DigestRow(label = "BREAKTHROUGH INSIGHT", text = paper.breakthrough, color = AccentEmerald)
                    Spacer(modifier = Modifier.height(6.dp))
                    DigestRow(label = "ACCEPTED LIMITATION", text = paper.acceptedTradeoff, color = AccentAmber)
                    Spacer(modifier = Modifier.height(6.dp))
                    DigestRow(label = "MODERN DESCENDANTS", text = paper.offspring, color = AccentIndigo)
                }
            }
        }
    }
}

@Composable
fun DigestRow(label: String, text: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold))
        Text(text, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
    }
}

data class PaperDigest(
    val title: String,
    val authors: String,
    val coreProblem: String,
    val breakthrough: String,
    val acceptedTradeoff: String,
    val offspring: String
)

// ==========================================
// 9. WEAK-SPOT RADAR TOOL
// ==========================================
@Composable
fun WeakSpotRadarTool() {
    var statelssWeb by remember { mutableStateOf(true) }
    var readReplicas by remember { mutableStateOf(true) }
    var circuitBreakers by remember { mutableStateOf(false) }
    var idempotencyKeys by remember { mutableStateOf(false) }
    var multiAzDns by remember { mutableStateOf(false) }
    var rateLimiter by remember { mutableStateOf(true) }

    val checks = listOf(statelssWeb, readReplicas, circuitBreakers, idempotencyKeys, multiAzDns, rateLimiter)
    val score = (checks.count { it } * 100) / checks.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎯 Architecture Weak-Spot Diagnostic", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Audit your system design against the 6 pillars of fault-tolerant systems.", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("System Resilience Score", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            "$score %",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (score >= 80) AccentEmerald else if (score >= 50) AccentAmber else AccentRose
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (score >= 80) AccentEmerald else if (score >= 50) AccentAmber else AccentRose
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pillar Checklist", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    RadarCheckRow("Stateless Web Tier (Horizontal Scaling)", statelssWeb) { statelssWeb = it }
                    RadarCheckRow("Database Read Replicas (Read Scaling)", readReplicas) { readReplicas = it }
                    RadarCheckRow("Circuit Breakers & Retries (Cascading Failure)", circuitBreakers) { circuitBreakers = it }
                    RadarCheckRow("Idempotency Keys on Payment/Order writes", idempotencyKeys) { idempotencyKeys = it }
                    RadarCheckRow("Multi-AZ Datacenter Replication", multiAzDns) { multiAzDns = it }
                    RadarCheckRow("Edge Rate Limiting & Token Bucket WAF", rateLimiter) { rateLimiter = it }
                }
            }
        }

        if (score < 80) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1AF59E0B)),
                    border = BorderStroke(1.dp, AccentAmber)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Identified Single Points of Failure", fontWeight = FontWeight.Bold, color = AccentAmber)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!circuitBreakers) Text("• Missing Circuit Breakers: A downstream API latency spike will exhaust server worker threads and crash the parent gateway.", style = MaterialTheme.typography.bodySmall)
                        if (!idempotencyKeys) Text("• Missing Idempotency Keys: Network retries on payment POST requests will cause duplicate customer charges.", style = MaterialTheme.typography.bodySmall)
                        if (!multiAzDns) Text("• Single Availability Zone: A power failure in US-EAST-1a takes down 100% of traffic.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun RadarCheckRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ==========================================
// 10. 1-CLICK OBSIDIAN & ANKI EXPORTER
// ==========================================
@Composable
fun NoteExporterTool() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var exportFormat by remember { mutableStateOf("obsidian") }

    val sampleMarkdown = """
# [[System Design Lab]] - Load Balancing Notes
Tags: #system-design #distributed-systems #scaling

## 1. Core Motivation
Load balancing distributes incoming network traffic across a server farm to prevent any single server from becoming a Single Point of Failure (SPOF).

## 2. L4 vs L7 Comparison
- **L4 (Transport):** Operates on IP and TCP port. High performance, zero inspection of HTTP headers.
- **L7 (Application):** Operates on full HTTP request. Supports SSL termination, path-based routing (`/api` vs `/static`), and cookie affinity.

## 3. Consistent Hashing
Maps servers to a 360-degree hash ring. When a server node is added or removed, only K/N keys need migration.
Virtual nodes are required to prevent non-uniform distribution (hot spots).

---
*Generated offline via System Design Lab Native Android Academy*
    """.trimIndent()

    val sampleAnki = """
What is the primary difference between L4 and L7 Load Balancing?	L4 routes at Transport layer (IP/Port) with zero payload inspection; L7 inspects HTTP headers, paths, and cookies.
How many keys must be remapped when a node joins a Consistent Hash Ring with N servers and K keys?	Only K/N keys are moved; modulo hashing requires moving almost all keys.
What is the 80/20 rule in distributed caching sizing?	80% of read queries target 20% of the working dataset; sizing cache RAM for this 20% covers most requests.
What does Raft use to guarantee that uncommitted log entries are not lost upon leader election?	Election Safety: A candidate must have a log at least as up-to-date as the majority to be elected.
    """.trimIndent()

    val currentContent = if (exportFormat == "obsidian") sampleMarkdown else sampleAnki

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📥 1-Click Knowledge Exporter", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Export structured notes and spaced-repetition flashcards directly into your second brain.", style = MaterialTheme.typography.bodySmall.copy(color = Slate400))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = exportFormat == "obsidian",
                            onClick = { exportFormat = "obsidian" },
                            label = { Text("Obsidian / Notion (.md)") }
                        )
                        FilterChip(
                            selected = exportFormat == "anki",
                            onClick = { exportFormat = "anki" },
                            label = { Text("Anki Flashcards (.tsv)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(currentContent))
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, currentContent)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Export to Second Brain")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Export")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("PREVIEW", style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
