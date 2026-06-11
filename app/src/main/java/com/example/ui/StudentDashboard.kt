package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.TutorMessage
import com.example.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    student: User,
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Digital Diary, 1 = AI Tutor Help, 2 = Holiday Calendar
    val context = LocalContext.current

    val entries by viewModel.allDiaryEntries.collectAsStateWithLifecycle()
    // Filter diary entries matching student's exact class or target "All"
    val studentClass = student.studentClass ?: "Class I"
    val filteredEntries = remember(entries) {
        entries.filter {
            it.targetClass == "All" || it.targetClass.equals(studentClass, ignoreCase = true)
        }
    }

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
                            text = "KPS Digital Diary",
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
                        // Profile Avatar displaying first letter of student's name
                        val firstLetter = student.fullName.trim().firstOrNull()?.toString()?.uppercase() ?: "S"
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
                    icon = { Icon(Icons.Default.Book, contentDescription = "Diary") },
                    label = { Text("Diary") }
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Tutor") },
                    label = { Text("AI Tutor") }
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar") }
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
                0 -> DiaryTab(
                    student = student,
                    items = filteredEntries,
                    onAskGeminiClick = { activeTab = 1 }
                )
                1 -> AITutorTab(student = student, viewModel = viewModel)
                2 -> StudentCalendarTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HeroGradientBanner(
    studentName: String,
    onAskClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_banner"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Icon",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "AI Personal Tutor",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                    Text(
                        text = "Need help with your lesson, $studentName?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = onAskClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Ask Gemini AI",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Brain",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiaryTab(
    student: User,
    items: List<DiaryEntry>,
    onAskGeminiClick: () -> Unit
) {
    val today = remember {
        val sdf = java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.ENGLISH)
        sdf.format(java.util.Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Gradient Banner
        item {
            HeroGradientBanner(
                studentName = student.fullName,
                onAskClick = onAskGeminiClick
            )
        }

        // 2. Timeline section header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S TIMELINE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = today,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }

        // 3. Optional Empty state inside the timeline list
        if (items.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No updates on your timeline today.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Enjoy your day! Check back again later.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(items) { entry ->
                DiaryCard(entry = entry)
            }
        }
    }
}

@Composable
fun DiaryCard(entry: DiaryEntry) {
    val context = LocalContext.current
    val cardAccentColor = when (entry.type) {
        "HOMEWORK" -> Color(0xFFE11D48) // Red-rose
        "CLASSWORK" -> Color(0xFF0284C7) // Blue
        "ASSIGNMENT" -> Color(0xFFD97706) // Orange
        "STUDY_MATERIAL" -> Color(0xFF059669) // Green
        else -> Color(0xFF7C3AED) // Purple/Announcement
    }

    val formattedTime = remember(entry.timestamp) {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        sdf.format(java.util.Date(entry.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LEFT COLUMN (Timeline tracking line & Time badge)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                // A vertical connection track
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(cardAccentColor.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Smaller colored indicator dot representing state
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(cardAccentColor, CircleShape)
                )
            }

            // RIGHT COLUMN (Content, Badges, etc.)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Category Type Badge • Subject
                Text(
                    text = "${entry.type} • ${entry.subject}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = cardAccentColor,
                        letterSpacing = 0.8.sp
                    )
                )

                // Title
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // Body content text
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                // Attachments Trigger if any
                entry.attachmentUrl?.let { url ->
                    Spacer(modifier = Modifier.height(4.dp))
                    if (url.startsWith("video:")) {
                        val videoAddr = url.substringAfter("video:")
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoAddr))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open video link: $videoAddr", Toast.LENGTH_LONG).show()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play Video",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🎥 ভিডিও ক্লাস (Watch Video Lesson)",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else if (url.startsWith("doc:") || url.startsWith("pdf:")) {
                        val pdfName = url.substringAfter("pdf:")
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .clickable {
                                    Toast.makeText(context, "Opening study doc: $pdfName...", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF document",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📄 ফাইলটি খুলুন (View PDF Study Note)",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Footer submitted by
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Teacher logo",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Submitted by: ${entry.teacherName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun AITutorTab(
    student: User,
    viewModel: MainViewModel
) {
    val messages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingAI.collectAsStateWithLifecycle()
    val activeMode by viewModel.selectedTutorMode.collectAsStateWithLifecycle()
    var messageInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    // TTS Setup
    var ttsObj by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var currentlyPlayingText by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val speechListener = android.speech.tts.TextToSpeech.OnInitListener { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        val tts = android.speech.tts.TextToSpeech(context, speechListener)
        ttsObj = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val speakOut: (String) -> Unit = { text ->
        ttsObj?.let { obj ->
            if (isTtsReady) {
                // Strip emoji and some special characters for better speech
                val cleanText = text.replace(Regex("[\\p{So}]"), "")
                currentlyPlayingText = text
                // Check if text has Bengali characters
                val hasBengaliChar = cleanText.any { it.code in 0x0980..0x09FF }
                if (hasBengaliChar) {
                    obj.language = java.util.Locale("bn", "IN")
                } else {
                    obj.language = java.util.Locale.US
                }
                obj.speak(cleanText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "AITutorSpeech")
                // Monitor speech complete to reset icon
                obj.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        currentlyPlayingText = null
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        currentlyPlayingText = null
                    }
                })
            } else {
                Toast.makeText(context, "Text to Speech is preparing...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // On Load, collect previous chat turns
    LaunchedEffect(student.username) {
        viewModel.loadChatHistory(student.username)
    }

    var selectedSubject by remember { mutableStateOf("Math") }
    
    val subjectPrompts = remember {
        mapOf(
            "Math" to listOf(
                Pair("১২ ও ১৩ এর নামতা 🔢", "কড়াইখুন্তি গোনা দিয়ে বা সহজ কোন উপায়ে ১২ এবং ১৩ এর ঘরের নামতা আমাকে বুঝিয়ে দাও"),
                Pair("ভগ্নাংশ কী? 🍰", "ভগ্নাংশ জিনিসটা কী এবং এটি কত প্রকার হয়, আমাকে সহজ বাংলায় সুন্দর করে বুঝিয়ে দিন"),
                Pair("যোগ ও বিয়োগ শেখার ট্রিক ➕", "ছোট সংখ্যার যোগ ও বিয়োগ মুখে মুখে করার কয়েকটি সহজ নিয়ম ও উদাহরণ শিখিয়ে দাও")
            ),
            "English" to listOf(
                Pair("ইংরেজি বাক্য তৈরি 📝", "আমি রোজ নিয়ম করে পড়তে বসি - এই বাংলা বাক্যটিকে ইংরেজিতে অনুবাদ করে সুন্দরভাবে বুঝিয়ে দিন"),
                Pair("Vowels ও Consonants 🔤", "ভাওয়েল (Vowels) এবং কনসোনেন্ট (Consonants) কী এবং ইংরেজি পড়ার সময় এদের কাজ কী, সুন্দর করে বুঝিয়ে দিন।"),
                Pair("सहज शब्द খেলা 🔎", "Class-এর বন্ধুদের সাথে খেলার জন্য ৩টি সহজ ইংরেজি শব্দ তাদের বাংলা অর্থসহ আমাকে শিখিয়ে দিন।")
            ),
            "Science" to listOf(
                Pair("গাছের খাবার তৈরি 🌾", "গাছ কিভাবে সূর্য আর মাটির জল দিয়ে নিজের খাবার নিজে বানায় তা আমাকে শিখিয়ে দিন"),
                Pair("মেঘ এবং জলচক্র 🌧️", "আকাশে মেঘ কীভাবে তৈরি হয় আর বৃষ্টি কেমন করে মাটি থেকে আকাশে আর আকাশ থেকে মাটিতে ঘুরেফিরে আসে?"),
                Pair("জল বাঁচানোর উপায় 💧", "আমাদের জল কেন অপচয় করা উচিত নয় এবং আমরা কীভাবে বাড়িতে ও বিদ্যালয়ে জল সংরক্ষণ করতে শিখব?")
            ),
            "Bengali" to listOf(
                Pair("যুক্তবর্ণ চেনার খেলা 🤝", "বাংলা কঠিন যুক্তবর্ণগুলো মনে রাখার সহজ উপায় ও ৫টি উদাহরণ আমাকে সহজ করে বুঝিয়ে দিন"),
                Pair("সুন্দর গল্প লিখি 📖", "বাঙলা পরীক্ষায় একটি চমৎকার রচনা বা বানিয়ে বাড়িয়ে গল্প লেখার কিছু সহজ ট্রিকস শিখিয়ে দাও")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Welcome Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Head",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "KPS AI Tutor (কৃত্রিম বুদ্ধিমত্তা শিক্ষকমশাই)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "Ask me homework doubts in Bengali (বাংলা) or English! I will solve your doubts step-by-step with love.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Tutor Mode Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Customize Teaching Style (শিক্ষক মহাশয়ের স্টাইল নির্বাচন করো):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val modes = listOf(
                        Triple("Normal", "Balanced", "সাধারণ"),
                        Triple("Soft", "Sweet", "স্নেহভরা ✨"),
                        Triple("StepByStep", "Clues Only", "ধাপে ধাপে 🧩"),
                        Triple("Bilingual", "Bilingual", "দ্বিভাষী 🌐")
                    )
                    modes.forEach { (modeVal, englishLabel, banglaLabel) ->
                        val isSelected = activeMode.equals(modeVal, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setTutorMode(modeVal) }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = banglaLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = englishLabel,
                                    fontSize = 8.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick HELP / Question Drawer
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subject Doubt Starters (সহজে প্রশ্ন করো):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val subjects = listOf(
                        Pair("Math", "অঙ্ক 🔢"),
                        Pair("English", "ইংরেজি 🔤"),
                        Pair("Science", "বিজ্ঞান 🌧️"),
                        Pair("Bengali", "বাংলা 📖")
                    )
                    subjects.forEach { (subKey, label) ->
                        val isSubSelected = selectedSubject == subKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSubSelected) MaterialTheme.colorScheme.secondaryContainer 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (isSubSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedSubject = subKey }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjectPrompts[selectedSubject]?.forEach { (shortTitle, fullPrompt) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.sendTutorMessage(
                                        student.username,
                                        student.username,
                                        student.studentClass ?: "Class I",
                                        fullPrompt
                                    )
                                }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shortTitle,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Chat Container
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Head",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Hello, ${student.username}! I am your KPS AI Tutor.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tap on any orange subject button or blue question chip above, or type your own question below! Let's start daily fun learning.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        onSpeakClick = speakOut,
                        isPlaying = currentlyPlayingText == msg.text
                    )
                }

                if (isGenerating) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp),
                                modifier = Modifier.padding(end = 48.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI Tutor is writing response (ভাবছে)...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input Field Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.clearChatHistory(student.username)
                    ttsObj?.stop()
                    currentlyPlayingText = null
                }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Ask KPS Tutor (পড়াশোনা সম্পর্কে জিজ্ঞেস করো)...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            val msg = messageInput.trim()
                            messageInput = ""
                            viewModel.sendTutorMessage(
                                student.username,
                                student.username,
                                student.studentClass ?: "Class I",
                                msg
                            )
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("ai_send_button"),
                    enabled = !isGenerating && messageInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: TutorMessage,
    onSpeakClick: ((String) -> Unit)? = null,
    isPlaying: Boolean = false
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
    }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        horizontalAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = bubbleShape,
            modifier = Modifier
                .padding(
                    start = if (isUser) 48.dp else 0.dp,
                    end = if (isUser) 0.dp else 48.dp
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You / তুমি" else "KPS AI Tutor (শিক্ষকমশাই)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    if (!isUser && onSpeakClick != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isPlaying) {
                                Text(
                                    text = "আওয়াজ হচ্ছে... ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Playing",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                IconButton(
                                    onClick = { onSpeakClick(message.text) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeDown,
                                        contentDescription = "Read Aloud",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun StudentCalendarTab(viewModel: MainViewModel) {
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
                text = "বিদ্যালয়ের ছুটির তালিকা ও ক্যালেন্ডার",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "KPS Interactive School Holiday Schedule",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        HolidayCalendarView(holidays = holidays)
    }
}
