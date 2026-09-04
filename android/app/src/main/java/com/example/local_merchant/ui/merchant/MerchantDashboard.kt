package com.example.local_merchant.ui.merchant

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_merchant.data.model.DashboardStatsResponse
import com.example.local_merchant.ui.components.Background
import com.example.local_merchant.ui.merchant.chat.MerchantBottomNav
import com.example.local_merchant.viewmodel.merchant.MerchantDashboardViewModel
import java.util.Calendar

@Composable
fun MerchantDashboardScreen(
    viewModel: MerchantDashboardViewModel,
    onNavigateToChats: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsState()

    Background {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                MerchantBottomNav(
                    currentRoute = "merchant_dashboard",
                    onNavigateToDashboard = {},
                    onNavigateToChats = onNavigateToChats,
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val currentState = state) {
                    is MerchantDashboardViewModel.DashboardState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF0EA5E9)
                        )
                    }
                    is MerchantDashboardViewModel.DashboardState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = currentState.message, color = Color.Red, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.fetchDashboard() }) {
                                Text("Retry")
                            }
                        }
                    }
                    is MerchantDashboardViewModel.DashboardState.Success -> {
                        DashboardContent(stats = currentState.stats)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(stats: DashboardStatsResponse) {
    val isActive = stats.isActive ?: false
    val currencySymbol = stats.currencySymbol.orEmpty()
    val todayRevenueStr = stats.todayRevenue.orEmpty()
    val revenueGrowth = stats.revenueGrowth.orEmpty()
    val isGrowthPositive = stats.isGrowthPositive ?: true
    val activeDeals = stats.activeDeals ?: 0
    val closedToday = stats.closedToday ?: 0
    val trendLabels = stats.trendLabels ?: listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val todayRevenueInt = todayRevenueStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val calendar = Calendar.getInstance()
    val currentDayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val revenueTrend = List(7) { index ->
        if (index == currentDayIndex) todayRevenueInt else 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("MERCHANT OVERVIEW", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Dashboard", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusColor = if (isActive) Color(0xFF10B981) else Color.Gray
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isActive) "AI AGENT STATUS: ACTIVE" else "AI AGENT STATUS: INACTIVE",
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Today's Revenue", color = Color.Gray, fontSize = 14.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$currencySymbol$todayRevenueStr", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)

                        val growthColor = if (isGrowthPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                        val growthIcon = if (isGrowthPositive) "↗" else "↘"
                        val growthPrefix = if (isGrowthPositive) "+" else "-"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(growthColor.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("$growthIcon $growthPrefix$revenueGrowth", color = growthColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardMiniCard(modifier = Modifier.weight(1f), title = "Active Deals", value = activeDeals.toString())
                        DashboardMiniCard(modifier = Modifier.weight(1f), title = "Closed Today", value = closedToday.toString())
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Revenue Trend", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Last 7 days", color = Color.Gray, fontSize = 12.sp)
                        }

                        val totalTrendRevenue = revenueTrend.sum()
                        Text("$currencySymbol$totalTrendRevenue", color = Color(0xFF10B981), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    DynamicRevenueLineChart(data = revenueTrend)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        trendLabels.forEachIndexed { index, label ->
                            val isCurrentDay = index == currentDayIndex
                            Text(
                                text = label,
                                color = if (isCurrentDay) Color(0xFF10B981) else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DashboardMiniCard(modifier: Modifier = Modifier, title: String, value: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF272730))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DynamicRevenueLineChart(data: List<Int>?) {
    val chartData = data ?: emptyList()
    if (chartData.isEmpty()) return

    Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
        val width = size.width
        val height = size.height
        val maxVal = (chartData.maxOrNull() ?: 1).toFloat().coerceAtLeast(1f)
        val minVal = (chartData.minOrNull() ?: 0).toFloat()
        val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

        val points = chartData.mapIndexed { index, value ->
            val x = (index.toFloat() / (chartData.size - 1).coerceAtLeast(1)) * width
            val normalizedY = (value - minVal) / range
            val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
            Offset(x, y)
        }

        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        points.forEach { path.lineTo(it.x, it.y) }

        drawPath(
            path = path,
            color = Color(0xFF0EA5E9),
            style = Stroke(width = 3.dp.toPx())
        )

        drawCircle(
            color = Color(0xFF10B981),
            radius = 5.dp.toPx(),
            center = points.last()
        )

        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0EA5E9).copy(alpha = 0.3f), Color.Transparent)
            )
        )
    }
}