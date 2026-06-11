package com.example.ui

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DiaryEntry
import com.example.data.GeneratedItem
import com.example.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    teacher: User,
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Publish, 1 = AI Generators, 2 = AI Resource Library, 3 = School Calendar
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
                            text = "KPS Teacher Console",
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
                        // Profile Avatar displaying first letter of teacher's name
                        val firstLetter = teacher.fullName.trim().firstOrNull()?.toString()?.uppercase() ?: "T"
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
                    icon = { Icon(Icons.Default.AddComment, contentDescription = "Publish") },
                    label = { Text("Publish") }
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Generators") },
                    label = { Text("AI Generators") }
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.FolderZip, contentDescription = "Library") },
                    label = { Text("AI Resources") }
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("School Cal") }
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
                0 -> PublishDiaryTab(teacher = teacher, viewModel = viewModel)
                1 -> AIGeneratorTab(viewModel = viewModel)
                2 -> AIResourcesTab(viewModel = viewModel)
                3 -> TeacherCalendarTab(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishDiaryTab(teacher: User, viewModel: MainViewModel) {
    var type by remember { mutableStateOf("HOMEWORK") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var targetClass by remember { mutableStateOf("Class I") }
    var targetSection by remember { mutableStateOf("All") }
    var attachmentType by remember { mutableStateOf("None") } // "None", "Video Link", "PDF Note"
    var attachmentValue by remember { mutableStateOf("") }

    val context = LocalContext.current

    val types = listOf("HOMEWORK", "CLASSWORK", "ASSIGNMENT", "STUDY_MATERIAL", "ANNOUNCEMENT")
    val subjects = listOf("Mathematics", "English", "Bengali", "Environmental Science / EVS", "General Notice")
    val classes = listOf("Class I", "Class II", "Class III", "Class IV", "Class V", "All")
    val sections = listOf("All", "A", "B", "C")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create Digital Diary Entry",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dropdown choices: Entry Type
                Text("Diary Post Type", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.take(3).forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.drop(3).forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                // Subject choice
                Text("Subject (বিষয়)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box {
                    var expandedSub by remember { mutableStateOf(false) }
                    Button(
                        onClick = { expandedSub = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(subject)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Open")
                    }
                    DropdownMenu(expanded = expandedSub, onDismissRequest = { expandedSub = false }) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    subject = s
                                    expandedSub = false
                                }
                            )
                        }
                    }
                }

                // Target Class & Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Target Class (ক্লাস)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var expandedClass by remember { mutableStateOf(false) }
                        Button(
                            onClick = { expandedClass = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(targetClass, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open")
                        }
                        DropdownMenu(expanded = expandedClass, onDismissRequest = { expandedClass = false }) {
                            classes.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        targetClass = c
                                        expandedClass = false
                                    }
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Section (সেকশন)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var expandedSection by remember { mutableStateOf(false) }
                        Button(
                            onClick = { expandedSection = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(targetSection, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open")
                        }
                        DropdownMenu(expanded = expandedSection, onDismissRequest = { expandedSection = false }) {
                            sections.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        targetSection = s
                                        expandedSection = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (শিরোনাম)") },
                    placeholder = { Text("e.g. Science Homework 5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Content Input (Large multi-line notes block)
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Post Body or Homework details") },
                    placeholder = { Text("Describe the task, page numbers, instructions, questions...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    maxLines = 6
                )

                // Attachment Toggle
                Text("Add Attachment (ঐচ্ছিক)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("None", "Video Link", "PDF Note").forEach { tab ->
                        FilterChip(
                            selected = attachmentType == tab,
                            onClick = {
                                attachmentType = tab
                                attachmentValue = ""
                            },
                            label = { Text(tab, fontSize = 11.sp) }
                        )
                    }
                }

                if (attachmentType != "None") {
                    OutlinedTextField(
                        value = attachmentValue,
                        onValueChange = { attachmentValue = it },
                        label = { Text(if (attachmentType == "Video Link") "Paste Video URL (YouTube)" else "Enter PDF Attachment file name (Mock)") },
                        placeholder = { Text(if (attachmentType == "Video Link") "https://youtube.com/..." else "Notes_Class_IV.pdf") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // One-Click Publish Button (At least 48dp touch high)
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            val attachmentUrlString = when (attachmentType) {
                                "Video Link" -> "video:$attachmentValue"
                                "PDF Note" -> "pdf:$attachmentValue"
                                else -> null
                            }
                            viewModel.publishDiaryEntry(
                                type = type,
                                title = title,
                                content = content,
                                subject = subject,
                                teacherName = teacher.fullName,
                                targetClass = targetClass,
                                targetSection = targetSection,
                                attachmentUrl = attachmentUrlString
                            )
                            Toast.makeText(context, "Published to Digital Diary immediately!", Toast.LENGTH_SHORT).show()
                            // Clear inputs
                            title = ""
                            content = ""
                            attachmentType = "None"
                            attachmentValue = ""
                        } else {
                            Toast.makeText(context, "Please enter both Title and description details.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Publish icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish to Students Immediately", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIGeneratorTab(viewModel: MainViewModel) {
    var creatorType by remember { mutableStateOf("HOMEWORK") } // "HOMEWORK", "WORKSHEET", "QUESTION_BANK", "EXAM"
    var className by remember { mutableStateOf("Class I") }
    var subject by remember { mutableStateOf("Mathematics") }
    var chapter by remember { mutableStateOf("") }
    
    // Sub-variants for worksheets or exams
    var subVariantStr by remember { mutableStateOf("Practice") } // "Practice", "Revision", "Assignment" for worksheets
    var examVariantStr by remember { mutableStateOf("Class Test") } // "Class Test", "Unit Test", "Mock Test" for exams

    val isGenerating by viewModel.isGeneratingAI.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponseText.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val classes = listOf("Class I", "Class II", "Class III", "Class IV", "Class V")
    val subjects = listOf("Mathematics", "English", "Bengali", "Environmental Science / EVS")
    val worksheetTypes = listOf("Practice", "Revision", "Assignment")
    val examTypes = listOf("Class Test", "Unit Test", "Practice Test", "Mock Test")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Learning Creator (Gemini)",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector Row for Creator tool
                Text("Select Generator Tool", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("HOMEWORK", "WORKSHEET").forEach { tool ->
                        FilterChip(
                            selected = creatorType == tool,
                            onClick = { creatorType = tool },
                            label = { Text(tool, fontSize = 11.sp) }
                        )
                    }
                    listOf("QUESTION_BANK", "EXAM").forEach { tool ->
                        FilterChip(
                            selected = creatorType == tool,
                            onClick = { creatorType = tool },
                            label = { Text(tool.replace("_", " "), fontSize = 11.sp) }
                        )
                    }
                }

                HorizontalDivider()

                // Configuration grid
                // Class Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Select Class", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var expandedClass by remember { mutableStateOf(false) }
                        Button(
                            onClick = { expandedClass = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(className)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open")
                        }
                        DropdownMenu(expanded = expandedClass, onDismissRequest = { expandedClass = false }) {
                            classes.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        className = c
                                        expandedClass = false
                                    }
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Select Subject", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var expandedSub by remember { mutableStateOf(false) }
                        Button(
                            onClick = { expandedSub = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(subject, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open")
                        }
                        DropdownMenu(expanded = expandedSub, onDismissRequest = { expandedSub = false }) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        subject = s
                                        expandedSub = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Sub-parameter depending on Tool: Worksheet Type or Exam Type
                if (creatorType == "WORKSHEET") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Worksheet Style", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            worksheetTypes.forEach { wt ->
                                FilterChip(
                                    selected = subVariantStr == wt,
                                    onClick = { subVariantStr = wt },
                                    label = { Text(wt) }
                                )
                            }
                        }
                    }
                } else if (creatorType == "EXAM") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Exam Style", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            examTypes.forEach { et ->
                                FilterChip(
                                    selected = examVariantStr == et,
                                    onClick = { examVariantStr = et },
                                    label = { Text(et, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Chapter Input
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter Title or Learning Topic") },
                    placeholder = { Text("e.g. Addition and carry, Solar system, Nouns") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Generate Button with loading indicator
                Button(
                    onClick = {
                        if (chapter.isNotBlank()) {
                            val targetParam = if (creatorType == "WORKSHEET") subVariantStr else examVariantStr
                            viewModel.generateAIContent(
                                creatorType = creatorType,
                                className = className,
                                subject = subject,
                                chapter = chapter,
                                extraParam = targetParam
                            )
                        } else {
                            Toast.makeText(context, "Please input a Chapter or Learning topic.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("ai_generate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isGenerating && chapter.isNotBlank()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Generating via Gemini...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask Gemini AI to Generate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Generated Content panel (with editing & direct mapping/saving)
        AnimatedVisibility(visible = aiResponse != null) {
            var currentBodyText by remember(aiResponse) { mutableStateOf(aiResponse ?: "") }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated View & Ready to Publish",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "KPS AI Generated $creatorType")
                                putExtra(Intent.EXTRA_TEXT, currentBodyText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share learning material"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share text", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Editable Content field for teachers to polish before they launch
                    OutlinedTextField(
                        value = currentBodyText,
                        onValueChange = { currentBodyText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        label = { Text("Draft Content (Edit/Customize here)") },
                        textStyle = MaterialTheme.typography.bodySmall,
                        maxLines = 15
                    )

                    // Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Action 1: Save to Resource Library
                        Button(
                            onClick = {
                                viewModel.saveContentAsGeneratedItem(
                                    type = creatorType,
                                    subject = subject,
                                    className = className,
                                    chapter = chapter,
                                    content = currentBodyText
                                )
                                Toast.makeText(context, "Saved successfully to AI Resource Library!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save library", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Info Library", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 2: Mapper: instantly publish to Students Digital Diary!
                        Button(
                            onClick = {
                                viewModel.publishDiaryEntry(
                                    type = if (creatorType == "HOMEWORK") "HOMEWORK" else "STUDY_MATERIAL",
                                    title = "AI Gen: $chapter - $creatorType",
                                    content = currentBodyText,
                                    subject = subject,
                                    teacherName = "Gemini AI Helper",
                                    targetClass = className,
                                    targetSection = "All"
                                )
                                Toast.makeText(context, "Direct Published immediately to $className Diary!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.SendTimeExtension, contentDescription = "Mapping", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish directly to Diary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIResourcesTab(viewModel: MainViewModel) {
    val items by viewModel.generatedItems.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Empty",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No saved AI resources yet.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = "Generate questions/worksheets above and tap 'Save' to populate.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Saved AI Questions, Worksheets & Tests",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(items) { resource ->
                SavedResourceCard(resource = resource, onDelete = {
                    viewModel.deleteGeneratedResource(resource.id)
                    Toast.makeText(context, "Removed resource from library", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}

@Composable
fun SavedResourceCard(resource: GeneratedItem, onDelete: () -> Unit) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        resource.type,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Delete resource button
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Specs
            Text(
                text = "${resource.className} - ${resource.subject}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "Chapter: ${resource.chapterName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(if (isExpanded) "Hide Full Text" else "View Full Generated Text", fontSize = 12.sp)
                    Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expand info")
                }

                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "KPS AI Shared ${resource.type}")
                        putExtra(Intent.EXTRA_TEXT, resource.contentJson)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share library item"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = resource.contentJson,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherCalendarTab(viewModel: MainViewModel) {
    val holidays by viewModel.allHolidays.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "বিদ্যালয়ের ছুটির তালিকা ও ক্যালেন্ডার (শিক্ষক ড্যাশবোর্ড)",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "KPS Teacher Interactive Holiday Schedule",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        HolidayCalendarView(holidays = holidays)
    }
}
