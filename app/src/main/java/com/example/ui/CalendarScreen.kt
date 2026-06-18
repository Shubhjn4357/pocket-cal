package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ColumnScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.model.Event
import com.example.model.Subtask
import com.example.ui.theme.*
import com.example.viewmodel.CalendarViewModel
import com.example.viewmodel.RibbonDay
import kotlinx.coroutines.launch
import com.example.util.NotificationScheduler
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.AccessTime

// Standalone Helper functions for date parsing
fun getYearFromDate(dateStr: String): Int {
    return try {
        dateStr.substring(0, 4).toInt()
    } catch (e: Exception) {
        Calendar.getInstance().get(Calendar.YEAR)
    }
}

fun getMonthFromDate(dateStr: String): Int {
    return try {
        dateStr.substring(5, 7).toInt() - 1
    } catch (e: Exception) {
        Calendar.getInstance().get(Calendar.MONTH)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedFilter by viewModel.selectedCategoryFilter.collectAsState()
    val events by viewModel.eventsForSelectedDate.collectAsState()
    val datesWithEvents by viewModel.datesWithEvents.collectAsState()
    val weeklyDays by viewModel.weeklyRibbonDays.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Expandable calendar states
    var isCalendarExpanded by remember { mutableStateOf(false) }
    var currentSelectorYear by remember { mutableStateOf(getYearFromDate(selectedDate)) }
    var currentSelectorMonth by remember { mutableStateOf(getMonthFromDate(selectedDate)) }

    // Sync selector month/year with selectedDate when it changes and calendar is not expanded
    LaunchedEffect(selectedDate) {
        if (!isCalendarExpanded) {
            currentSelectorYear = getYearFromDate(selectedDate)
            currentSelectorMonth = getMonthFromDate(selectedDate)
        }
    }

    // Deep theme values
    val bgStart = if (isDarkTheme) CosmicDarkBgStart else TwilightLightBgStart
    val bgEnd = if (isDarkTheme) CosmicDarkBgEnd else TwilightLightBgEnd
    val surfaceColor = if (isDarkTheme) CosmicDarkSurface else TwilightLightSurface
    val primaryText = if (isDarkTheme) CosmicDarkText else TwilightLightText

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(bgStart, bgEnd))
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.triggerHapticClick()
                        showBottomSheet = true
                    },
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 16.dp)
                        .testTag("add_event_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Agenda Item",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            var swipeDragAmount by remember { mutableStateOf(0f) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .pointerInput(selectedDate) {
                        detectHorizontalDragGestures(
                            onDragStart = { swipeDragAmount = 0f },
                            onDragEnd = {
                                if (swipeDragAmount < -120f) {
                                    // Swiped Left -> Go to Next Day
                                    viewModel.selectNextDay()
                                } else if (swipeDragAmount > 120f) {
                                    // Swiped Right -> Go to Previous Day
                                    viewModel.selectPreviousDay()
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                swipeDragAmount += dragAmount
                            }
                        )
                    }
            ) {
            // 1. Header Area
            HeaderArea(
                selectedDate = selectedDate,
                isDarkTheme = isDarkTheme,
                viewModel = viewModel,
                onOpenProfileSettings = { showSettingsSheet = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Expandable Grid Calendar Selector
            ExpandableCalendarSelector(
                selectedDate = selectedDate,
                datesWithEvents = datesWithEvents,
                isDarkTheme = isDarkTheme,
                onSelectDate = { viewModel.selectDate(it) },
                isExpanded = isCalendarExpanded,
                onToggleExpand = {
                    viewModel.triggerHapticClick()
                    isCalendarExpanded = !isCalendarExpanded
                },
                currentSelectorYear = currentSelectorYear,
                currentSelectorMonth = currentSelectorMonth,
                onSelectorYearChange = {
                    viewModel.triggerHapticClick()
                    currentSelectorYear = it
                },
                onSelectorMonthChange = {
                    viewModel.triggerHapticClick()
                    currentSelectorMonth = it
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (!isCalendarExpanded) {
                // 2. Sliding Weekly Ribbon
                SlidingWeeklyRibbon(
                    weeklyDays = weeklyDays,
                    selectedDate = selectedDate,
                    datesWithEvents = datesWithEvents,
                    onSelectDate = { viewModel.selectDate(it) }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // 3. Unified Category Filter Bar
            FilterBar(
                selectedFilter = selectedFilter,
                isDarkTheme = isDarkTheme,
                onSelectFilter = { viewModel.selectCategoryFilter(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Timeline & Agenda List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (events.isEmpty()) {
                    EmptyStatePlaceholder(
                        isDarkTheme = isDarkTheme,
                        activeFilter = selectedFilter,
                        dateLabel = selectedDate
                    )
                } else {
                    TimelineAgendaList(
                        events = events,
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onEditEvent = {
                            eventToEdit = it
                            showBottomSheet = true
                        }
                    )
                }
            }
        }

        // 5. Drawer Sheet Panel (Bottom Sheet for Add/Edit Agenda)
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false 
                    eventToEdit = null
                },
                sheetState = sheetState,
                containerColor = surfaceColor,
                contentColor = primaryText,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .background(
                                    color = if (isDarkTheme) Color.Gray.copy(alpha = 0.5f) else Color.LightGray,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            ) {
                BottomSheetAddContent(
                    viewModel = viewModel,
                    isDark = isDarkTheme,
                    eventToEdit = eventToEdit,
                    onDismiss = { 
                        showBottomSheet = false 
                        eventToEdit = null
                    }
                )
            }
        }

        // 5b. Settings and Profile bottom sheet panel
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = settingsSheetState,
                containerColor = surfaceColor,
                contentColor = primaryText,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .background(
                                    color = if (isDarkTheme) Color.Gray.copy(alpha = 0.5f) else Color.LightGray,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            ) {
                SettingsAndProfileSheetContent(
                    viewModel = viewModel,
                    isDark = isDarkTheme,
                    onDismiss = { showSettingsSheet = false }
                )
            }
        }
    }
}
}

// 1. Header Component with Custom Branding Logo
@Composable
fun BrandingLogo(isDarkTheme: Boolean) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDarkTheme) {
                        listOf(CosmicDarkPrimary, CosmicDarkSecondary)
                    } else {
                        listOf(TwilightLightPrimary, TwilightLightSecondary)
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.5.dp,
                color = if (isDarkTheme) CosmicDarkTertiary.copy(alpha = 0.6f) else TwilightLightTertiary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stylized top accent bar
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(4.dp)
                    .background(
                        color = if (isDarkTheme) CosmicDarkTertiary else TwilightLightTertiary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(3.dp))
            // Minimalist calendar grid layout
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.85f), CircleShape))
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.85f), CircleShape))
                }
            }
        }
    }
}

@Composable
fun HeaderArea(
    selectedDate: String,
    isDarkTheme: Boolean,
    viewModel: CalendarViewModel,
    onOpenProfileSettings: () -> Unit
) {
    val monthYearTitle = viewModel.getFormattedHeadingDate(selectedDate)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            BrandingLogo(isDarkTheme)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Pocket Cal",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = monthYearTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isDarkTheme) CosmicDarkText.copy(alpha = 0.7f) else TwilightLightText.copy(alpha = 0.65f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Theme Toggle Button
            IconButton(
                onClick = { viewModel.toggleTheme() },
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isDarkTheme) CosmicDarkSurface else TwilightLightSurface,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme style",
                    tint = if (isDarkTheme) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Circular Settings / Profile Button
            IconButton(
                onClick = {
                    viewModel.triggerHapticClick()
                    onOpenProfileSettings()
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isDarkTheme) CosmicDarkSurface else TwilightLightSurface,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Open Profile Settings",
                    tint = if (isDarkTheme) CosmicDarkText else TwilightLightText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ExpandableCalendarSelector(
    selectedDate: String,
    datesWithEvents: Set<String>,
    isDarkTheme: Boolean,
    onSelectDate: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    currentSelectorYear: Int,
    currentSelectorMonth: Int,
    onSelectorYearChange: (Int) -> Unit,
    onSelectorMonthChange: (Int) -> Unit
) {
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val shortMonthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val daysOfWeekShort = listOf("S", "M", "T", "W", "T", "F", "S")

    val bgStart = if (isDarkTheme) CosmicDarkSurface else TwilightLightSurface
    val primaryText = if (isDarkTheme) CosmicDarkText else TwilightLightText

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f))
            .testTag("expandable_calendar_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgStart.copy(alpha = 0.85f),
            contentColor = primaryText
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Trigger Bar for Expand / Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Full Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${monthNames[currentSelectorMonth]} $currentSelectorYear",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryText
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Collapse" else "Expand Grid",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))

                // Scroll-based Year Selector (LazyRow)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Year",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryText.copy(alpha = 0.6f),
                        modifier = Modifier.width(45.dp)
                    )
                    
                    val yearListState = rememberLazyListState()
                    val years = remember { (2020..2035).toList() }
                    
                    // Scroll to current visible year
                    LaunchedEffect(currentSelectorYear) {
                        val index = years.indexOf(currentSelectorYear)
                        if (index != -1) {
                            yearListState.animateScrollToItem(maxOf(0, index - 2))
                        }
                    }

                    LazyRow(
                        state = yearListState,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(years) { year ->
                            val isSelected = year == currentSelectorYear
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else primaryText.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectorYearChange(year) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else primaryText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grid-based Month Selector (separate container)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Month",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryText.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    // Displaying months in a 4x3 compact grid format
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (row in 0..2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (col in 0..3) {
                                    val monthIdx = row * 4 + col
                                    val isSelected = monthIdx == currentSelectorMonth
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else primaryText.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSelectorMonthChange(monthIdx) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shortMonthNames[monthIdx],
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else primaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day Grid
                Column(modifier = Modifier.fillMaxWidth()) {
                    // S M T W T F S header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeekShort.forEach { dayHead ->
                            Text(
                                text = dayHead,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = primaryText.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compute grid structure using java.util.Calendar
                    val calendar = remember(currentSelectorYear, currentSelectorMonth) {
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentSelectorYear)
                            set(Calendar.MONTH, currentSelectorMonth)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                    val firstDay = calendar.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Saturday = 7
                    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    
                    // Leading empty spaces (Sunday is col index 0, so firstDay - 1 empty cells)
                    val leadingSpaces = firstDay - 1
                    val totalCells = leadingSpaces + maxDay
                    val rowCount = (totalCells + 6) / 7

                    for (r in 0 until rowCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (c in 0..6) {
                                val cellIdx = r * 7 + c
                                val dayNum = cellIdx - leadingSpaces + 1

                                if (cellIdx < leadingSpaces || dayNum > maxDay) {
                                    // Empty cells
                                    Box(modifier = Modifier.weight(1f))
                                } else {
                                    // Formatted checking string for this cell day
                                    val cellDateStr = String.format(Locale.US, "%04d-%02d-%02d", currentSelectorYear, currentSelectorMonth + 1, dayNum)
                                    val isCellSelected = cellDateStr == selectedDate
                                    val hasEvent = datesWithEvents.contains(cellDateStr)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isCellSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                                )
                                                .border(
                                                    width = if (isCellSelected) 0.dp else 1.dp,
                                                    color = if (cellDateStr == String.format(Locale.US, "%04d-%02d-%02d", Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))) {
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                    } else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable { onSelectDate(cellDateStr) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isCellSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 13.sp
                                                    ),
                                                    color = if (isCellSelected) MaterialTheme.colorScheme.onPrimary else primaryText
                                                )
                                                
                                                if (hasEvent) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(top = 1.dp)
                                                            .size(4.dp)
                                                            .background(
                                                                if (isCellSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
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
    }
}

// 2. Continuous Date Ribbon roller
@Composable
fun SlidingWeeklyRibbon(
    weeklyDays: List<RibbonDay>,
    selectedDate: String,
    datesWithEvents: Set<String>,
    onSelectDate: (String) -> Unit
) {
    val ribbonState = rememberLazyListState()

    // Scroll to active index initially
    LaunchedEffect(key1 = selectedDate) {
        val index = weeklyDays.indexOfFirst { it.dateString == selectedDate }
        if (index != -1) {
            ribbonState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = ribbonState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(weeklyDays) { day ->
            val isSelected = day.dateString == selectedDate
            val hasTasks = datesWithEvents.contains(day.dateString)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = if (day.isToday) 1.5.dp else 1.dp,
                        color = when {
                            isSelected -> Color.Transparent
                            day.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) // Highlight physical today in brand primary
                            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onSelectDate(day.dateString) }
                    .padding(vertical = 12.dp)
                    .testTag("day_ribbon_${day.dateString}")
            ) {
                Text(
                    text = day.dayOfWeek,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    } else {
                        if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = day.dayOfMonth,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Glowing date indicator dot if tasks present
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (hasTasks) {
                                if (isSelected) Color.White else MaterialTheme.colorScheme.tertiary
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

// 3. Capsule filter tabs
@Composable
fun FilterBar(
    selectedFilter: String,
    isDarkTheme: Boolean,
    onSelectFilter: (String) -> Unit
) {
    val filters = listOf("All", "Work", "Personal", "Health", "Social", "Ideas")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { category ->
            val isSelected = category.lowercase() == selectedFilter.lowercase()

            val indicatorColor = if (category.lowercase() != "all") {
                if (isSelected) Color.White else getCategoryColor(category, isDarkTheme)
            } else {
                Color.Transparent
            }

            val itemBg = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
            }

            val itemBorderColor = if (isSelected) {
                Color.Transparent
            } else {
                if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
            }

            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(itemBg)
                    .border(
                        width = 1.dp,
                        color = itemBorderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectFilter(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("filter_tab_${category}")
            ) {
                if (category != "All") {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(indicatorColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = textColor
                )
            }
        }
    }
}

// 4. Chronological Layout Cards with Timeline connectors
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineAgendaList(
    events: List<Event>,
    viewModel: CalendarViewModel,
    isDarkTheme: Boolean,
    onEditEvent: (Event) -> Unit
) {
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }
    val sortedEvents = remember(events) { events.sortedWith(compareBy({ it.hour }, { it.minute })) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(sortedEvents, key = { it.id }) { event ->
                            val isExpanded = expandedStates[event.id] == true

                            // Chronological Event Agenda Card using SwipeToDismissBox
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteEvent(event)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val color = Color.Red.copy(alpha = 0.85f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Delete",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .animateContentSize()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("event_card_${event.id}")
                                        .clickable {
                                            viewModel.triggerHapticClick()
                                            expandedStates[event.id] = !isExpanded
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkTheme) CosmicDarkSurface else TwilightLightSurface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(
                                                                getEventDisplayColor(event, isDarkTheme),
                                                                CircleShape
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = event.category.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 1.sp
                                                        ),
                                                        color = getEventDisplayColor(event, isDarkTheme)
                                                    )

                                                    if (event.isRepeating) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .background(getEventDisplayColor(event, isDarkTheme).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "🔁 ${event.repeatInterval}",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                                                color = getEventDisplayColor(event, isDarkTheme)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = event.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                                    ),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = if (event.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Text(
                                                    text = event.time,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = if (event.isCompleted) {
                                                        (if (isDarkTheme) CosmicDarkTertiary else MaterialTheme.colorScheme.primary).copy(alpha = 0.5f)
                                                    } else {
                                                        if (isDarkTheme) CosmicDarkTertiary else MaterialTheme.colorScheme.primary
                                                    }
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Edit button
                                            IconButton(
                                                onClick = {
                                                    viewModel.triggerHapticClick()
                                                    onEditEvent(event)
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("event_edit_button_${event.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Agenda",
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // Round action completion Checkbox on right edge
                                            IconButton(
                                                onClick = { viewModel.toggleEventCompletion(event) },
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .testTag("event_checkbox_${event.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (event.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                                    contentDescription = "Toggle Complete",
                                                    tint = if (event.isCompleted) {
                                                        getEventDisplayColor(event, isDarkTheme)
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }

                                        // Collapsible Nested Detail Screen
                                        if (isExpanded) {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            LineSeparator(isDark = isDarkTheme)
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = "Agenda Details",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Text(
                                                text = event.description.ifEmpty { "No extra details available." },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Checklist subtask items
                                            val subtasks = event.getSubtasks()

                                            Text(
                                                text = "Subtask Checklist (${subtasks.filter { it.isCompleted }.size}/${subtasks.size})",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            if (subtasks.isEmpty()) {
                                                Text(
                                                    text = "No checklist subtasks. Add one below!",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            } else {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                ) {
                                                    subtasks.forEach { subtask ->
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable {
                                                                    viewModel.toggleSubtask(event, subtask.title)
                                                                }
                                                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                                                .testTag("subtask_item_${event.id}_${subtask.title}")
                                                        ) {
                                                            Icon(
                                                                imageVector = if (subtask.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                                                contentDescription = null,
                                                                tint = if (subtask.isCompleted) getEventDisplayColor(event, isDarkTheme) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = subtask.title,
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                                    fontWeight = if (subtask.isCompleted) FontWeight.Normal else FontWeight.Medium
                                                                ),
                                                                color = if (subtask.isCompleted) {
                                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                                                } else {
                                                                    MaterialTheme.colorScheme.onSurface
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Subtask Add Input Field inside item!
                                            var customSubtaskText by remember { mutableStateOf("") }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = customSubtaskText,
                                                    onValueChange = { customSubtaskText = it },
                                                    placeholder = { Text("Add checklist task...", fontSize = 13.sp) },
                                                    singleLine = true,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    textStyle = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        if (customSubtaskText.isNotBlank()) {
                                                            viewModel.addSubtask(event, customSubtaskText)
                                                            customSubtaskText = ""
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "New subtask item",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            // Action button options (Delete)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = { viewModel.deleteEvent(event) },
                                                    colors = ButtonDefaults.textButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    modifier = Modifier.testTag("delete_event_button_${event.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Remove Slot", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // Little directional expansion indicator
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
        }
    }
}

// 4b. Profile & Settings bottom sheet content
@Composable
fun SettingsAndProfileSheetContent(
    viewModel: CalendarViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val isGoogleConnected by viewModel.isGoogleConnected.collectAsState()
    val syncCalendar by viewModel.syncGoogleCalendar.collectAsState()
    val syncFit by viewModel.syncGoogleFit.collectAsState()
    val leadTime by viewModel.notificationLeadTime.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showGoogleAccountPickerDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Sheet Title
        Text(
            text = "Profile & Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // 1. Profile Customization Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.02f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Initials Badge
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (editedName.isNotBlank()) editedName.take(2).uppercase() else "User",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = editedName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isGoogleConnected) "shubhamjain.com.in@gmail.com" else "Offline Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                LineSeparator(isDark = isDark)

                // Name Edit Input
                OutlinedTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        viewModel.setUserName(it)
                    },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                )
            }
        }

        // 2. Google Session & Sync Combined Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.02f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Google Sync Integration",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isGoogleConnected) {
                        Box(
                            modifier = Modifier
                                .background(Color.Green.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CONNECTED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)),
                            )
                        }
                    }
                }

                Text(
                    text = "Authenticate Google integration to securely synchronize task items, fitness logs, calendar slots, and reminders into a single combined view.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (!isGoogleConnected) {
                    Button(
                        onClick = {
                            viewModel.triggerHapticClick()
                            showGoogleAccountPickerDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect Google Account")
                    }
                } else {
                    LineSeparator(isDark = isDark)

                    // Synchronize toggles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "Google Calendar Sync",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                "Sync scheduled calendar events",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Checkbox(
                            checked = syncCalendar,
                            onCheckedChange = { viewModel.setSyncGoogleCalendar(it) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "Google Fit Sync",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                "Sync fitness activity and targets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Checkbox(
                            checked = syncFit,
                            onCheckedChange = { viewModel.setSyncGoogleFit(it) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.triggerGoogleSyncCombined() },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        if (isSyncing) {
                            Text("Syncing with Google Services...")
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Fit & Calendar Combined")
                        }
                    }

                    TextButton(
                        onClick = { viewModel.setGoogleConnected(false) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Disconnect Account")
                    }
                }
            }
        }

        // 3. Notification Customization Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.02f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Push Notifications Settings",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Configure the lead time for event alarms to notify you in advance before your schedule active slots are set to start.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                LineSeparator(isDark = isDark)

                Text(
                    text = "Alarm Lead Time Reminder Offset:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                // Selectable choices
                val leadTimeOptions = listOf(
                    0 to "At event start",
                    5 to "5 min before",
                    10 to "10 min before",
                    15 to "15 min before",
                    30 to "30 min before"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    leadTimeOptions.forEach { (minutes, label) ->
                        val isSelected = leadTime == minutes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.triggerHapticClick()
                                    viewModel.setNotificationLeadTime(minutes)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        viewModel.triggerHapticClick()
                        // Trigger immediate local test notification
                        val testEvent = Event(
                            id = 9999,
                            title = "Pocket Cal Test Alarm",
                            description = "Your notification subsystem is verified as perfectly operational!",
                            category = "Ideas",
                            date = viewModel.todayDateString,
                            time = "Now",
                            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            minute = Calendar.getInstance().get(Calendar.MINUTE),
                            isCompleted = false
                        )
                        NotificationScheduler.scheduleAlarm(viewModel.context, testEvent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Trigger Test Push Alarm Now")
                }
            }
        }

        // About & Version Box
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pocket Cal • Modern Edition",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = "Version 1.4.0 (Sync & Lead-alarm Updates)",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }

    // Google Account Picker Dialog
    if (showGoogleAccountPickerDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showGoogleAccountPickerDialog = false }
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Red
                            )
                        }
                        Text(
                            text = "Choose an Account",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Select a synchronized Google account to combine calendar schedules & fitness active times with Pocket Cal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Main Choice
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.triggerHapticClick()
                                viewModel.setUserName("Shubham Jain")
                                viewModel.setGoogleConnected(true)
                                showGoogleAccountPickerDialog = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SJ", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }

                            Column {
                                Text(
                                    text = "Shubham Jain",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "shubhamjain.com.in@gmail.com",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Use Another Account Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.triggerHapticClick()
                                // Just connect as default user
                                viewModel.setUserName("Shubham Jain")
                                viewModel.setGoogleConnected(true)
                                showGoogleAccountPickerDialog = false
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            "Add another synchronized account",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showGoogleAccountPickerDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

fun formatHourLabel(hour: Int): String {
    val ampm = if (hour < 12) "AM" else "PM"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$h $ampm"
}

// 5. Drawer Sheet contents for adding/editing entries
@Composable
fun BottomSheetAddContent(
    viewModel: CalendarViewModel,
    isDark: Boolean,
    eventToEdit: Event? = null,
    onDismiss: () -> Unit
) {
    var rawInputText by remember { 
        mutableStateOf(if (eventToEdit != null) "${eventToEdit.title} at ${eventToEdit.time}" else "") 
    }
    var manualCategorySelection by remember { 
        mutableStateOf<String?>(eventToEdit?.category) 
    }
    var notesText by remember { 
        mutableStateOf(eventToEdit?.description ?: "") 
    }
    
    var isRepeating by remember { 
        mutableStateOf(eventToEdit?.isRepeating ?: false) 
    }
    var repeatInterval by remember { 
        mutableStateOf(if (eventToEdit?.isRepeating == true) eventToEdit.repeatInterval else "Daily") 
    }
    var newSubtaskText by remember { mutableStateOf("") }
    val pendingSubtasks = remember { 
        val list = mutableStateListOf<String>()
        if (eventToEdit != null) {
            list.addAll(eventToEdit.getSubtasks().map { it.title })
        }
        list
    }

    val categories = listOf("Work", "Personal", "Health", "Social", "Ideas")
    
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Intelligent preview as user types
    val nlpPreview = remember(rawInputText) {
        if (rawInputText.isNotBlank()) {
            com.example.util.NLPParser.parse(rawInputText)
        } else {
            null
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .imePadding() // Automatically adjusts padding for the on-screen keyboard
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (eventToEdit != null) "Edit Agenda Slot" else "New Agenda Slot",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Natural Language enabled",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Input Text Field with soft styled background
        OutlinedTextField(
            value = rawInputText,
            onValueChange = { rawInputText = it },
            placeholder = { Text("e.g. Sync meeting at 10:30 AM") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("input_event_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = true
        )

        // Real-time parsed intelligence NLP summary chip (Glowing preview indicator)
        if (nlpPreview != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Auto-parsed: \"${nlpPreview.title}\"",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Category: ${manualCategorySelection ?: nlpPreview.category} | Time: ${nlpPreview.time}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Override tag selector row
        Text(
            text = "Selector Category",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Compact horizontal scroll for categories
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = manualCategorySelection == cat
                val catColor = getCategoryColor(cat, isDark)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) catColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            viewModel.triggerHapticClick()
                            manualCategorySelection = if (isSelected) null else cat
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(catColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Agenda Notes Option - Single line, elegant notes field
        OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            placeholder = { Text("Agenda Notes (Optional)", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Loop task checkbox container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isRepeating) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isRepeating) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    viewModel.triggerHapticClick()
                    isRepeating = !isRepeating
                }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isRepeating,
                    onCheckedChange = {
                        viewModel.triggerHapticClick()
                        isRepeating = it
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Loop Repeated Reminder",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Auto reschedule alert sound automatically",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            if (isRepeating) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val intervals = listOf("Daily", "Weekly", "Yearly")
                    intervals.forEach { label ->
                        val active = repeatInterval == label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                )
                                .clickable {
                                    viewModel.triggerHapticClick()
                                    repeatInterval = label
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Checklist builder
        Text(
            text = "Build Checklist Subtasks",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSubtaskText,
                onValueChange = { newSubtaskText = it },
                placeholder = { Text("Task step (e.g. Bring files, Buy fruits)", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newSubtaskText.isNotBlank()) {
                        viewModel.triggerHapticClick()
                        pendingSubtasks.add(newSubtaskText.trim())
                        newSubtaskText = ""
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Step",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (pendingSubtasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                pendingSubtasks.forEachIndexed { idx, sub ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.triggerHapticClick()
                                pendingSubtasks.removeAt(idx)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Suggestion Prompts in a side-scroll ribbon for simplicity
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val suggestions = listOf(
                "Gym training at 7:00 AM",
                "Team sync @ 10:30 AM",
                "Coffee with Sarah @ 3 PM",
                "Draft project sketch @ 9 PM"
            )
            
            items(suggestions) { sug ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .clickable {
                            viewModel.triggerHapticClick()
                            rawInputText = sug
                        }
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sug,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CTA secure/update button
        Button(
            onClick = {
                if (rawInputText.isNotBlank()) {
                    if (eventToEdit != null) {
                        val finalSubtasks = pendingSubtasks.map { title ->
                            val existing = eventToEdit.getSubtasks().find { it.title == title }
                            Subtask(title = title, isCompleted = existing?.isCompleted ?: false)
                        }
                        viewModel.updateEventDetails(
                            id = eventToEdit.id,
                            rawInput = rawInputText,
                            manualCategoryOverride = manualCategorySelection,
                            optionalDescription = notesText,
                            isRepeating = isRepeating,
                            repeatInterval = repeatInterval,
                            subtasks = finalSubtasks,
                            isCompleted = eventToEdit.isCompleted,
                            date = eventToEdit.date
                        )
                    } else {
                        viewModel.addEvent(
                            rawInputText,
                            manualCategorySelection,
                            notesText,
                            isRepeating,
                            repeatInterval,
                            pendingSubtasks.map { Subtask(title = it, isCompleted = false) }
                        )
                    }
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("secure_agenda_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = if (isDark) Color.Black else Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (eventToEdit != null) "Reschedule Agenda Slot" else "Secure Agenda Slot",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.sp
                )
            )
        }
    }
}

// Visual category color helper
fun getCategoryColor(category: String, isDark: Boolean): Color {
    return if (isDark) {
        when (category.lowercase()) {
            "work" -> Color(0xFF30D158)       // Vibrant iOS Green
            "personal" -> Color(0xFFBF5AF2)   // Vibrant iOS Purple
            "health" -> Color(0xFF64D2FF)     // Vibrant iOS Light Blue / Teal
            "social" -> Color(0xFFFF375F)     // Vibrant iOS Pink / Red
            "ideas" -> Color(0xFFFFD60A)      // Vibrant iOS Orange / Gold
            else -> Color(0xFFAEAEB2)         // Standard Warm Grey
        }
    } else {
        when (category.lowercase()) {
            "work" -> Color(0xFF24B14C)       // Lush Green
            "personal" -> Color(0xFF5856D6)   // Royal Indigo / Purple
            "health" -> Color(0xFF00A2E0)     // Vivid Sky Blue
            "social" -> Color(0xFFFF2D55)     // Energetic Rose Red
            "ideas" -> Color(0xFFFF9500)      // Deep Warm Orange
            else -> Color(0xFF8E8E93)         // Cool Muted Grey
        }
    }
}

// Elegant checked/completed greying display color mapping
fun getEventDisplayColor(event: Event, isDark: Boolean): Color {
    return if (event.isCompleted) {
        if (isDark) Color(0xFF6E6E73) else Color(0xFFAEAEB2)
    } else {
        getCategoryColor(event.category, isDark)
    }
}

// Divider styling
@Composable
fun LineSeparator(isDark: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
            )
    )
}

// 4. Empty state visual widget representation
@Composable
fun EmptyStatePlaceholder(
    isDarkTheme: Boolean,
    activeFilter: String,
    dateLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(90.dp)
                .background(
                    color = if (isDarkTheme) CosmicDarkSurface else TwilightLightSurface,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (activeFilter == "All") "No Agenda Sessions Today" else "No $activeFilter Sessions Found",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap the floating action button in the corner to schedule your day instantly using natural language.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassBg = if (isDarkTheme) {
        Color.White.copy(alpha = 0.07f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }
    
    val borderBrush = Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.03f))
        } else {
            listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.12f))
        }
    )

    Column(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    listOf(glassBg, glassBg.copy(alpha = 0.03f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        content = content
    )
}

@Composable
fun GlassmorphicWidgetsShelf(
    viewModel: CalendarViewModel,
    isDarkTheme: Boolean,
    events: List<Event>
) {
    var isShelfVisible by remember { mutableStateOf(false) }

    if (!isShelfVisible) return // Fully decommissioned and replaced by RealOsWidgetStatusControl

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isShelfVisible = !isShelfVisible }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GLASSMORPHISM QUICK WIDGETS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = if (isShelfVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Shelf",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        if (isShelfVisible) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                // Widget 1: Quick Task Input (4x2 style)
                item {
                    var inputState by remember { mutableStateOf("") }
                    GlassmorphicCard(
                        modifier = Modifier.width(260.dp).height(110.dp),
                        isDarkTheme = isDarkTheme
                    ) {
                        Text(
                            text = "QUICK TASK INPUT • 4x2",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = inputState,
                            onValueChange = { inputState = it },
                            placeholder = { Text("e.g. Call gym trainer @ 5 PM", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)) },
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (inputState.isNotBlank()) {
                                            viewModel.addEvent(inputState, null, "")
                                            inputState = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = "Parse & Book",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                // Widget 2: Quick Check Box (2x2 style)
                item {
                    val nextPending = events.firstOrNull { !it.isCompleted }
                    GlassmorphicCard(
                        modifier = Modifier.width(130.dp).height(110.dp),
                        isDarkTheme = isDarkTheme
                    ) {
                        Text(
                            text = "QUICK CHECKBOX • 2x2",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (nextPending != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nextPending.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = nextPending.time,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleEventCompletion(nextPending) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Mark done",
                                        tint = getCategoryColor(nextPending.category, isDarkTheme),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "All completed! ✨",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Widget 3: Date Wise Design (2x2 style)
                item {
                    val sdfDay = SimpleDateFormat("dd", Locale.US)
                    val sdfDayOfWeek = SimpleDateFormat("EEE", Locale.US)
                    val dayStr = sdfDay.format(Calendar.getInstance().time)
                    val dowStr = sdfDayOfWeek.format(Calendar.getInstance().time).uppercase(Locale.US)
                    
                    GlassmorphicCard(
                        modifier = Modifier.width(130.dp).height(110.dp),
                        isDarkTheme = isDarkTheme
                    ) {
                        Text(
                            text = "DATE WISE • 2x2",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = dayStr,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        lineHeight = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dowStr,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            // Visual indicator beads
                            Column(horizontalAlignment = Alignment.End) {
                                val remaining = events.filter { !it.isCompleted }.size
                                Text(
                                    text = "$remaining left",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    events.take(3).forEach { ev ->
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(getCategoryColor(ev.category, isDarkTheme), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RealOsWidgetStatusControl(
    viewModel: CalendarViewModel,
    isDarkTheme: Boolean,
    events: List<Event>
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isStatusExpanded by remember { mutableStateOf(true) }
    
    val displayDateStr = remember {
        java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.US).format(java.util.Date())
    }
    
    val incompleteCount = remember(events) { events.filter { !it.isCompleted }.size }
    val totalCount = events.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isStatusExpanded = !isStatusExpanded }
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Glowing green dot to show synchronization is online
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REAL OS HOME SCREEN WIDGET SYSTEM",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = if (isStatusExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle Widget Info",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        if (isStatusExpanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("real_widget_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CosmicDarkSurface else TwilightLightSurface,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title and status text
                    Text(
                        text = "Android Launcher Widget (4x2)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This application synchronizes scheduled items with native home screen AppWidgets and long-press shortcuts reactively using background intents & providers.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated live widget info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVE DATE FEEDBACK",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = displayDateStr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "PENDING AGENDA",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "$incompleteCount / $totalCount Active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Guide steps
                    Text(
                        text = "To place this widget on your home screen:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val guideSteps = listOf(
                        "1. Return to your Android Launcher Home Screen.",
                        "2. Tap & long-press on any empty space.",
                        "3. Tap on 'Widgets' and scroll to discover 'PocketCal'.",
                        "4. Drag and hold the PocketCal Widget onto your desktop!",
                        "5. Long-press the PocketCal launcher app icon on your desktop to see the Live Date indicator!"
                    )
                    guideSteps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerHapticClick()
                                PocketCalWidgetProvider.triggerWidgetRefresh(context)
                                com.example.util.ShortcutHelper.updateLauncherShortcuts(context, events)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_broadcast_widget_update"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Broadcast Sync",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.triggerHapticClick()
                                // Call test unit-alarm notification preview
                                com.example.util.NotificationScheduler.scheduleAlarm(
                                    context,
                                    Event(
                                        id = 9999,
                                        title = "System Test Sync Update",
                                        description = "Your design system dynamic update broadcast is healthy!",
                                        category = "Ideas",
                                        date = viewModel.todayDateString,
                                        time = "Now",
                                        hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
                                        minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE) + 1,
                                        isCompleted = false
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_test_notification"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = "Test Dispatch",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}
