package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    admin: User,
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Statistics & Users, 1 = Bulk Upload Students, 2 = Holiday Calendar Management
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "KHARUBERIA PRIMARY SCHOOL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "KPS Admin Console",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        // Profile Avatar displaying first letter of admin's name
                        val firstLetter = admin.fullName.trim().firstOrNull()?.toString()?.uppercase() ?: "A"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    viewModel.logout()
                                    onLogout()
                                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                }
                                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstLetter,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Stats & Users") },
                    label = { Text("Registry") }
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Bulk Upload") },
                    label = { Text("Bulk Student") }
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Holidays") },
                    label = { Text("Holiday Cal") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> StatisticsAndUsersTab(viewModel = viewModel)
                1 -> ImportExportTab(viewModel = viewModel)
                2 -> HolidayCalendarManagementTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StatisticsAndUsersTab(viewModel: MainViewModel) {
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val teachers by viewModel.allTeachers.collectAsStateWithLifecycle()
    val diaries by viewModel.allDiaryEntries.collectAsStateWithLifecycle()
    val resources by viewModel.generatedItems.collectAsStateWithLifecycle()

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var listTabSelection by remember { mutableIntStateOf(0) } // 0 = Students, 1 = Teachers

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Statistics Section
        item {
            Text(
                text = "KPS System Statistics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
        }

        item {
            // Stats Grid (Row combinations)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Students Registered",
                        value = students.size.toString(),
                        subtitle = "Class I - V list",
                        icon = Icons.Default.School,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Teachers Active",
                        value = teachers.size.toString(),
                        subtitle = "Publishing staff",
                        icon = Icons.Default.Group,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val homeworkCount = diaries.count { it.type == "HOMEWORK" }
                    StatCard(
                        title = "Homeworks",
                        value = homeworkCount.toString(),
                        subtitle = "Active diaries",
                        icon = Icons.Default.Book,
                        color = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "AI Gen Items",
                        value = resources.size.toString(),
                        subtitle = "Lesson worksheets",
                        icon = Icons.Default.AutoAwesome,
                        color = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAddStudentDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Student", fontSize = 13.sp)
                }

                Button(
                    onClick = { showAddTeacherDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Teacher", fontSize = 13.sp)
                }
            }
        }

        // Section Tabs
        item {
            TabRow(selectedTabIndex = listTabSelection) {
                Tab(selected = listTabSelection == 0, onClick = { listTabSelection = 0 }) {
                    Text("Students List (${students.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = listTabSelection == 1, onClick = { listTabSelection = 1 }) {
                    Text("Teachers List (${teachers.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }

        // User lists display
        if (listTabSelection == 0) {
            if (students.isEmpty()) {
                item {
                    Text("No students added yet.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                }
            } else {
                items(students) { student ->
                    StudentRowCard(student = student, onDelete = {
                        viewModel.deleteUser(student.username)
                    }, onReset = {
                        viewModel.resetPassword(student.username, "KPS@123")
                    })
                }
            }
        } else {
            if (teachers.isEmpty()) {
                item {
                    Text("No teachers added yet.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                }
            } else {
                items(teachers) { teacher ->
                    TeacherRowCard(teacher = teacher, onDelete = {
                        viewModel.deleteUser(teacher.username)
                    }, onReset = {
                        viewModel.resetPassword(teacher.username, "KPS@123")
                    })
                }
            }
        }
    }

    // --- Dialogs ---
    if (showAddStudentDialog) {
        AddStudentDialog(onDismiss = { showAddStudentDialog = false }, onSave = { name, clazz, sec, roll ->
            viewModel.addStudentAndGenerateUsername(name, clazz, sec, roll)
            showAddStudentDialog = false
        })
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(onDismiss = { showAddTeacherDialog = false }, onSave = { username, text ->
            viewModel.addTeacher(username, text)
            showAddTeacherDialog = false
        })
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), maxLines = 1)
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
                Text(subtitle, fontSize = 10.sp, color = color.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun StudentRowCard(student: User, onDelete: () -> Unit, onReset: () -> Unit) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Username: ${student.username} | Pw: ${student.password}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Class: ${student.studentClass} | Section: ${student.section} | Roll: ${student.rollNumber}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = {
                onReset()
                Toast.makeText(context, "Password reset to default KPS@123 for ${student.username}", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.LockReset, contentDescription = "Reset Pw", tint = MaterialTheme.colorScheme.secondary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE11D48))
            }
        }
    }
}

@Composable
fun TeacherRowCard(teacher: User, onDelete: () -> Unit, onReset: () -> Unit) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Credentials -> User: ${teacher.username} | Pw: ${teacher.password}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = {
                onReset()
                Toast.makeText(context, "Password reset to default KPS@123", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.LockReset, contentDescription = "Reset", tint = MaterialTheme.colorScheme.secondary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE11D48))
            }
        }
    }
}

// --- Dynamic Input Dialogs ---
@Composable
fun AddStudentDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Class I") }
    var section by remember { mutableStateOf("A") }
    var roll by remember { mutableStateOf("1") }

    val classes = listOf("Class I", "Class II", "Class III", "Class IV", "Class V")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student First Name (English)") },
                    placeholder = { Text("e.g. Rahul, Priya") },
                    singleLine = true
                )

                // Class Select
                Column {
                    Text("Select Class", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    var classExpanded by remember { mutableStateOf(false) }
                    Button(onClick = { classExpanded = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Text(selectedClass)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                        classes.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = {
                                selectedClass = c
                                classExpanded = false
                            })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = roll,
                        onValueChange = { roll = it },
                        label = { Text("Roll No") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedClass, section, roll) },
                enabled = name.isNotBlank()
            ) {
                Text("Save & Generate Username")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTeacherDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Teacher Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("e.g. amal, priya_t") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name & Subjects") },
                    placeholder = { Text("e.g. Amal Roy (Maths)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (username.isNotBlank() && fullName.isNotBlank()) onSave(username, fullName) },
                enabled = username.isNotBlank() && fullName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// --- Tab 1: CSV Excel-style Importer & Exporter ---
@Composable
fun ImportExportTab(viewModel: MainViewModel) {
    var importText by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Excel/CSV Importer Consoles",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Upload Students list from Excel:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Format: First Name, Class, Section, Roll Number (One student per line). Avoid row headings.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Sample CSV Load helper button
                Button(
                    onClick = {
                        importText = "Sipra,Class IV,A,4\nKuntal,Class V,B,12\nManoj,Class II,A,18\nRitwick,Class III,C,2"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text("Load Sample Excel Data", fontSize = 11.sp)
                }

                // CSV Pasting area
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    placeholder = { Text("Paste CSV or Excel lines here. E.g.\nRahul,Class IV,A,1\nPriya,Class III,B,15") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = {
                        if (importText.isNotBlank()) {
                            viewModel.importStudentsFromCSV(importText) { count ->
                                Toast.makeText(context, "$count Students imported and accounts created automatically with default credentials!", Toast.LENGTH_LONG).show()
                                importText = ""
                            }
                        } else {
                            Toast.makeText(context, "CSV field is empty.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Import")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import and Generate Database", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Export console
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Export Student Registry to Excel / CSV:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "This will compile all current student details, their generated Usernames and passwords. Copy and paste it easily into Microsoft Excel or Notepad.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                val exportedCSV = remember { viewModel.exportStudentsToCSV() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp)
                ) {
                    Text(
                        text = exportedCSV,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(exportedCSV))
                        Toast.makeText(context, "CSV data copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Student CSV Registry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HolidayCalendarManagementTab(viewModel: MainViewModel) {
    val holidays by viewModel.allHolidays.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isGeneratingAI.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var manualDate by remember { mutableStateOf("") }
    var manualName by remember { mutableStateOf("") }
    var manualDesc by remember { mutableStateOf("") }

    var documentText by remember { mutableStateOf("") }

    val sampleImageText = """
        [OCR SCAN FROM NOTICE BOARD IMAGE]
        Kharuberia Primary School — Summer Vacation Notice
        Date: 2026-07-15 to 2026-07-18
        All classes from Class I to Class V will remain closed due to excessive heat. School will re-open on Tuesday. Enjoy summer block holidays!
        Signed by,
        Headmaster
    """.trimIndent()

    val samplePdfText = """
        [PDF CIRCULAR DOCUMENT]
        Govt Primary Education Board notification no. 482-KPS
        Sub: Durga Puja Vacation National Holiday List 2026
        This is to notify that Kharuberia Primary School (KPS) will observe dynamic holidays on:
        - 2026-10-15 (Durga Puja Saptami)
        - 2026-10-16 (Durga Puja Ashtami / Maha Holiday)
        We wish all children, parents, and teachers a very prosperous Puja festival!
    """.trimIndent()

    val sampleExcelText = """
        [SPREADSHEET ROWS EXTRACTION]
        Date | Name | Description
        2026-06-23 | Ratha Yatra | রথযাত্রা উৎসব ও ছুটি
        2026-08-15 | Independence Day | ভারতের জাতীয় স্বাধীনতা দিবস উদযাপন
        2026-09-05 | Teacher's Day | শিক্ষক দিবস ও বিশেষ অনুষ্ঠান
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Title
        Column {
            Text(
                text = "হলিডে তালিকা ও ক্যালেন্ডার",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "AI Holiday & Calendar Administration Panel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // 1. IN-APP CALENDAR LIVE PREVIEW CARD
        Text(
            text = "Live Calendar Preview (লাইভ ক্যালেন্ডার প্রিভিউ):",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        
        HolidayCalendarView(holidays = holidays)

        // 2. AI UNSTRUCTURED OCR / PDF / IMAGE / EXCEL IMPORT CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AI-Powered Holiday Document Extractor",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Extract dynamic holiday schedules from uploaded Image OCR transcripts, PDF copies, or copy-pasted Excel tables using Gemini AI. Load a quick simulation template below:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )

                // Doc Templates Loader
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { documentText = sampleImageText },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Image OCR Scan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { documentText = samplePdfText },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("PDF Circular", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { documentText = sampleExcelText },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Excel Copy Paste", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = documentText,
                    onValueChange = { documentText = it },
                    placeholder = { Text("Paste unstructured text, document transcripts, or OCR lines here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        if (documentText.isNotBlank()) {
                            viewModel.importHolidaysFromDocAI(documentText) { count ->
                                if (count > 0) {
                                    Toast.makeText(context, "$count Holidays successfully extracted by Gemini AI and integrated into the calendar!", Toast.LENGTH_LONG).show()
                                    documentText = ""
                                } else {
                                    Toast.makeText(context, "AI extraction failed or no new holidays found in dates format.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter doc text or tap a layout preset above.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAiGenerating && documentText.isNotBlank()
                ) {
                    if (isAiGenerating) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp)) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Analyzing Schedule...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run AI Holiday Extraction (জেমিনি AI)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. MANUAL HOLIDAY CREATION CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Holiday Manually (ম্যানুয়ালি ছুটির দিন যোগ করুন)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = manualDate,
                    onValueChange = { manualDate = it },
                    placeholder = { Text("e.g. 2026-10-15") },
                    label = { Text("Date (YYYY-MM-DD)", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = manualName,
                    onValueChange = { manualName = it },
                    placeholder = { Text("e.g. Durga Puja Saptami") },
                    label = { Text("Holiday Name", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = manualDesc,
                    onValueChange = { manualDesc = it },
                    placeholder = { Text("e.g. দুর্গাপূজা উপলক্ষে বিদ্যালয় ছুটি") },
                    label = { Text("Description / Bangla Details (Optional)", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (manualDate.matches("\\d{4}-\\d{2}-\\d{2}".toRegex()) && manualName.isNotBlank()) {
                            viewModel.addHoliday(manualDate, manualName, manualDesc)
                            Toast.makeText(context, "Added holiday: $manualName", Toast.LENGTH_SHORT).show()
                            manualDate = ""
                            manualName = ""
                            manualDesc = ""
                        } else {
                            Toast.makeText(context, "Invalid format. Ensure Date is YYYY-MM-DD.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Holiday Slot", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. RESET ACTIONS SECTION
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Calendar Reset Actions (ক্যালেন্ডার রিসেট)",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    fontSize = 13.sp
                )
                Text(
                    text = "To clear all dynamic events and return the school schedule cleanly back to only Sunday Holidays, click below:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )

                Button(
                    onClick = {
                        viewModel.clearAllHolidays()
                        Toast.makeText(context, "All dynamic holidays cleared!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Flush All Dynamic Holidays", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
