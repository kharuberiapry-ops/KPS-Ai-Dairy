package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Holiday
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HolidayCalendarView(
    holidays: List<Holiday>,
    modifier: Modifier = Modifier
) {
    // Standard year 2026 as per local context, but allows switching months/years
    var currentYear by remember { mutableIntStateOf(2026) }
    var currentMonth by remember { mutableIntStateOf(5) } // June (0-indexed, so Jan=0, Jun=5)

    // Keep track of the currently selected day to display details
    var selectedDayStr by remember { mutableStateOf<String?>(null) }
    var selectedHolidayDetail by remember { mutableStateOf<Holiday?>(null) }
    var selectedIsSunday by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Calculate dates in month
    val calendar = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.
    val offsetDays = firstDayOfWeek - 1

    // Map holidays for fast lookup
    val holidaysMap = remember(holidays) {
        holidays.associateBy { it.date }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${monthNames[currentMonth]} $currentYear",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                IconButton(
                    onClick = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Days of the Week headings
            val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    val isSun = day == "SUN"
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSun) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Calendar Grid Items (Days)
            val totalCells = offsetDays + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - offsetDays + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cellIndex >= offsetDays && dayNumber <= daysInMonth) {
                                    val isSunday = col == 0
                                    
                                    // Assemble Date key: YYYY-MM-DD
                                    val formattedMonth = String.format("%02d", currentMonth + 1)
                                    val formattedDay = String.format("%02d", dayNumber)
                                    val dateKey = "$currentYear-$formattedMonth-$formattedDay"

                                    val hasHoliday = holidaysMap.containsKey(dateKey)
                                    val holidayInfo = holidaysMap[dateKey]

                                    val isSelected = selectedDayStr == dateKey

                                    // Highlighting Colors
                                    val cellBgColor = when {
                                        isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                        hasHoliday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        isSunday -> Color(0xFFFEF2F2) // Sunday soft red base
                                        else -> Color.Transparent
                                    }

                                    val cellBorderModifier = if (hasHoliday) {
                                        Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    } else Modifier

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cellBgColor)
                                            .then(cellBorderModifier)
                                            .clickable {
                                                selectedDayStr = dateKey
                                                selectedHolidayDetail = holidayInfo
                                                selectedIsSunday = isSunday
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayNumber.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (hasHoliday || isSunday) FontWeight.Bold else FontWeight.Medium,
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.secondary
                                                        isSunday -> Color(0xFFDC2626) // Vivid red for Sundays
                                                        hasHoliday -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    fontSize = 14.sp
                                                )
                                            )
                                            // Tiny dot under holidays or Sundays
                                            if (hasHoliday) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                )
                                            } else if (isSunday) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(Color(0xFFEF4444), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Display Card at Bottom based on selected cell
    AnimatedVisibility(
        visible = selectedDayStr != null,
        enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut()
    ) {
        selectedDayStr?.let { date ->
            val formatterInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatterOutput = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            val readableDate = try {
                val d = formatterInput.parse(date)
                d?.let { formatterOutput.format(it) } ?: date
            } catch (e: Exception) {
                date
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (selectedHolidayDetail != null || selectedIsSunday) {
                                    Color(0xFFFCA5A5) // Reddish accent
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Day detail",
                            tint = if (selectedHolidayDetail != null || selectedIsSunday) {
                                Color(0xFFDC2626)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = readableDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                selectedHolidayDetail != null -> selectedHolidayDetail!!.name
                                selectedIsSunday -> "School Weekly Holiday (রবিবার)"
                                else -> "Regular School Class Day"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        selectedHolidayDetail?.description?.let { desc ->
                            if (desc.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
