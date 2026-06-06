package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.ui.theme.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.security.GpsUtils
import com.example.security.SecurityUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppScreen(viewModel: AppViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = activeScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                "setup_wizard" -> SetupWizardScreen(viewModel)
                "auth_login" -> LoginScreen(viewModel)
                "auth_register" -> StudentRegisterScreen(viewModel)
                "superadmin_dashboard" -> SuperAdminDashboard(viewModel)
                "admin_dashboard" -> AdminDashboard(viewModel)
                "student_dashboard" -> StudentDashboard(viewModel)
                else -> LoginScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 1. FIRST TIME SETUP SCREEN
// ==========================================
@Composable
fun SetupWizardScreen(viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .testTag("setup_wizard_card"),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to CampusTrack",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Super-Admin setup wizard",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Super Admin Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_name"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_email"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Create Strong Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_password"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.registerSuperAdmin(name, email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("setup_submit_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Initialize College Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ==========================================
// 2. AUTH LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(viewModel: AppViewModel) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant LOGO
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MapsHomeWork,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CampusTrack",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "GPS Attendance Management System",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        label = { Text("Email or Register Number") },
                        leadingIcon = { Icon(Icons.Default.Badge, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                            Text("Remember Me", fontSize = 13.sp)
                        }
                        TextButton(onClick = { /* Simulated Forgot Password */ }) {
                            Text("Forgot password?", fontSize = 13.sp)
                        }
                    }

                    if (authState is AppViewModel.AuthState.Error) {
                        Text(
                            text = (authState as AppViewModel.AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.login(identifier, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Authenticate", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Are you a new student? ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                TextButton(onClick = { viewModel.navigateTo("auth_register") }) {
                    Text("Register Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ==========================================
// 3. STUDENT REGISTRATION SCREEN
// ==========================================
@Composable
fun StudentRegisterScreen(viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var regNo by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var deptExpanded by remember { mutableStateOf(false) }

    val departments by viewModel.allDepartments.collectAsStateWithLifecycle()
    var selectedDept by remember { mutableStateOf<Department?>(null) }
    var errorMsg by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Student Registration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "GPS Registration Wizard",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = regNo,
                        onValueChange = { regNo = it },
                        label = { Text("Register Number") },
                        leadingIcon = { Icon(Icons.Default.Layers, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Department Dropdown Selection
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDept?.name ?: "Select Department",
                            onValueChange = {},
                            label = { Text("Department") },
                            leadingIcon = { Icon(Icons.Default.HomeWork, null) },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { deptExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text("[${dept.code}] ${dept.name}") },
                                    onClick = {
                                        selectedDept = dept
                                        deptExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email ID") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (errorMsg.isNotBlank()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            val deptId = selectedDept?.id ?: 0
                            viewModel.registerStudent(name, regNo, deptId, mobile, email, pass) { success, msg ->
                                if (!success) {
                                    errorMsg = msg
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Complete Enrollment", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.navigateTo("auth_login") }) {
                Text("Return to Login Portal")
            }
        }
    }
}

// ==========================================
// 4. STUDENT DASHBOARD
// ==========================================
@Composable
fun StudentDashboard(viewModel: AppViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    if (authState !is AppViewModel.AuthState.StudentLoggedIn) return
    val student = (authState as AppViewModel.AuthState.StudentLoggedIn).user

    val attendanceLogs by viewModel.allAttendance.collectAsStateWithLifecycle()
    val departments by viewModel.allDepartments.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val activePreset by viewModel.currentSelectedPreset.collectAsStateWithLifecycle()
    val activeSecStatus by viewModel.securityStatus.collectAsStateWithLifecycle()
    val activeLimitLocation by viewModel.activeLocation.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("home") }

    val myAttendance = attendanceLogs.filter { it.studentId == student.id }
    val myLeaves = viewModel.allLeaveRequests.collectAsStateWithLifecycle().value.filter { it.studentId == student.id }
    val deptName = departments.firstOrNull { it.id == student.departmentId }?.name ?: "Student"

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == "home",
                        onClick = { activeTab = "home" },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = activeTab == "attendance",
                        onClick = { activeTab = "attendance" },
                        icon = { Icon(Icons.Default.LocationOn, null) },
                        label = { Text("Check-in") }
                    )
                    NavigationBarItem(
                        selected = activeTab == "leave",
                        onClick = { activeTab = "leave" },
                        icon = { Icon(Icons.Default.Class, null) },
                        label = { Text("Leave") }
                    )
                    NavigationBarItem(
                        selected = activeTab == "profile",
                        onClick = { activeTab = "profile" },
                        icon = { Icon(Icons.Default.Face, null) },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Dark Dashboard Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val initials = student.name.split(" ")
                        .filter { it.isNotEmpty() }
                        .take(2)
                        .joinToString("") { it.take(1) }
                        .uppercase()
                        .ifEmpty { "ST" }

                    // Initial bubble avatar (e.g. AS)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = "STUDENT ID: ${student.registerNumber.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = student.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Sleek Logout circular button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.logout() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedContent(targetState = activeTab, label = "StudentTabTransitions") { tab ->
                when (tab) {
                    "home" -> StudentHomeTab(student, myAttendance, notifications, deptName)
                    "attendance" -> StudentCheckInTab(viewModel, student, activePreset, activeSecStatus, activeLimitLocation ?: AttendanceLocation())
                    "leave" -> StudentLeaveTab(viewModel, student, myLeaves)
                    "profile" -> StudentProfileTab(viewModel, student, deptName, myAttendance.size)
                }
            }
        }
    }
}

@Composable
fun StudentHomeTab(user: User, logs: List<Attendance>, notifs: List<NotificationEntity>, deptName: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Attendance circular metric card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Semester Progress",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = deptName,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val presents = logs.count { it.status == "Present" || it.status == "Late" }
                        val absents = logs.count { it.status == "Absent" }
                        Text("Direct Check-Ins: $presents", fontSize = 13.sp)
                        Text("Absent Marks: $absents", fontSize = 13.sp)
                    }

                    // Percentage arc gauge
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val total = logs.size.coerceAtLeast(1)
                        val percentage = (logs.count { it.status == "Present" || it.status == "Late" } * 100.0 / total).toInt()
                        val color = if (percentage >= 75) MaterialTheme.colorScheme.primary else ElegantError
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = trackColor.copy(alpha = 0.5f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = color,
                                startAngle = -90f,
                                sweepAngle = (percentage * 3.6f),
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$percentage%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // System Announcements
        item {
            Text("Latest College Announcements", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        val itemsNotif = notifs.take(3)
        if (itemsNotif.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Text(
                        "No administrative notifications currently.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(itemsNotif) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(notif.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        // Brief historical list
        item {
            Text("Recent Logged Attendance", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        }

        if (logs.isEmpty()) {
            item {
                Text("No attendance recorded yet this semester.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(logs.take(5)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(log.date, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(log.remarks ?: "Clock trace verified", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val color = when (log.status) {
                            "Present" -> ElegantSuccess
                            "Late" -> Color(0xFFFFB74D)
                            "Absent" -> ElegantError
                            else -> Color(0xFF9575CD)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(color)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(log.status, color = if (log.status == "Present") Color(0xFF1E381E) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCheckInTab(
    viewModel: AppViewModel,
    user: User,
    preset: GpsUtils.LocationPreset,
    sec: SecurityUtils.SecurityStatus,
    targetLoc: AttendanceLocation
) {
    val distance = GpsUtils.calculateDistanceMeters(preset.latitude, preset.longitude, targetLoc.latitude, targetLoc.longitude)
    val colorAccent = if (distance <= targetLoc.radius && !sec.hasBreaches) ElegantSuccess else ElegantError
    val buttonEnabled = distance <= targetLoc.radius && !sec.hasBreaches

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "GPS Geolocation Verification",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Main Campus Verification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Pulse + Accent Limit Status)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val insideRadius = distance <= targetLoc.radius && !sec.hasBreaches
                    val statusText = if (insideRadius) "Inside Campus Radius" else "Outside Campus Bounds"
                    val statusColor = if (insideRadius) ElegantSuccess else ElegantError

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pulsing dot indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Acc: 3.2m",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Location detail row with map pin
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Column {
                            Text(
                                text = if (distance <= targetLoc.radius) "Main Admin Block Core Area" else "Near Campus Bounds",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Current coords: %.5f, %.5f".format(preset.latitude, preset.longitude),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Distance: ${distance.toInt()}m from Admin Block (Limit: ${targetLoc.radius.toInt()}m)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 26.dp)
                    )
                }

                // Elegant central Check In button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .shadow(
                                elevation = if (buttonEnabled) 16.dp else 0.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            .clip(CircleShape)
                            .background(if (buttonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            .clickable(enabled = buttonEnabled) {
                                viewModel.submitStudentAttendance(true, user.id, user.registerNumber)
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (buttonEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "CHECK IN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (buttonEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Shift times row
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SHIFT START", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("09:00 AM", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SHIFT END", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("04:30 PM", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Interactive Testing Location Simulator (Required for Studio Build Testing)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GPS Signal Preset Simulator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select simulated physical location inputs to evaluate boundaries & anti-fraud features:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                GpsUtils.Presets.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectGpsPreset(p) }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = preset.name == p.name, onClick = { viewModel.selectGpsPreset(p) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            if (p.isFakeGps) {
                                Text("[Spoofed GPS Flag Detected]", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // Anti-Fraud Device Signatures
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ANTI-FRAUD CORES STATUS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Pair("Emulator Sandbox Signature", sec.isEmulator),
                    Pair("Secure Root Environment Check", sec.isRooted),
                    Pair("Virtual / Simulated Location App Check", sec.isMockLocation),
                    Pair("GPS Coordinates Spoof Signature", sec.isFakeGps)
                ).forEach { (check, failed) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(check, fontSize = 13.sp)
                        Icon(
                            imageVector = if (failed) Icons.Default.Cancel else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (failed) ElegantError else ElegantSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (sec.hasBreaches) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(12.dp)
            ) {
                Text(
                    text = "ALERT: Android verification flags raised suspicious device activity. Clock-in button locked to preserve record accuracy.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp
                )
            }
        }

        // Submit buttons (Dual fallback control layout with test tags intact)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.submitStudentAttendance(true, user.id, user.registerNumber) },
                enabled = distance <= targetLoc.radius && !sec.hasBreaches,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantSuccess, disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("check_in_btn")
            ) {
                Text("CHECK IN", fontWeight = FontWeight.Bold, color = if (buttonEnabled) Color(0xFF1E381E) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }

            Button(
                onClick = { viewModel.submitStudentAttendance(false, user.id, user.registerNumber) },
                enabled = distance <= targetLoc.radius && !sec.hasBreaches,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("check_out_btn")
            ) {
                Text("CHECK OUT", fontWeight = FontWeight.Bold, color = if (buttonEnabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun StudentLeaveTab(viewModel: AppViewModel, user: User, myLeaves: List<LeaveRequest>) {
    var reason by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var leaveTypeExpanded by remember { mutableStateOf(false) }

    val leaveTypes = listOf("Medical Leave", "Personal Leave", "Emergency Leave")
    var selectedType by remember { mutableStateOf(leaveTypes[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Apply College Leave", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Type selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        label = { Text("Leave Category") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { leaveTypeExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = leaveTypeExpanded,
                        onDismissRequest = { leaveTypeExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        leaveTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = {
                                selectedType = type
                                leaveTypeExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    value = start,
                    onValueChange = { start = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = end,
                    onValueChange = { end = it },
                    label = { Text("End Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Details & Reasoning") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Button(
                    onClick = {
                        viewModel.applyLeave(user.id, user.registerNumber, selectedType, reason, start, end, "simulated_doc.pdf")
                        reason = ""
                        start = ""
                        end = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload Documentation & Send Application", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Historic Leave Logs", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (myLeaves.isEmpty()) {
            Text("No leave requests filed yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            myLeaves.forEach { leave ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(leave.leaveType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${leave.startDate} to ${leave.endDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(leave.reason, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        val pillColor = when (leave.status) {
                            "Approved" -> Color(0xFF81C784)
                            "Rejected" -> Color(0xFFE57373)
                            else -> Color(0xFFFFB74D) // Pending
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pillColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(leave.status, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileTab(viewModel: AppViewModel, user: User, deptName: String, checkinCount: Int) {
    var mobile by remember { mutableStateOf(user.mobileNumber) }
    var email by remember { mutableStateOf(user.email) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Selection simulation
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Face, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
        }

        Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(user.registerNumber, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Edit Mobile Contact Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.updateStudentProfile(user.copy(mobileNumber = mobile, email = email))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Alterations")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Swipes Recorded", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("$checkinCount times", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ==========================================
// 5. ADMIN DASHBOARD
// ==========================================
@Composable
fun AdminDashboard(viewModel: AppViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    if (authState !is AppViewModel.AuthState.AdminLoggedIn) return
    val admin = (authState as AppViewModel.AuthState.AdminLoggedIn).admin

    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val leafRequests by viewModel.allLeaveRequests.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.allAttendance.collectAsStateWithLifecycle()
    val departments by viewModel.allDepartments.collectAsStateWithLifecycle()
    val reportsLogs by viewModel.allReports.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("logs") }
    var docReportFeed by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = activeTab == "logs",
                    onClick = { activeTab = "logs" },
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Listings") }
                )
                NavigationBarItem(
                    selected = activeTab == "leaves",
                    onClick = { activeTab = "leaves" },
                    icon = { Icon(Icons.Default.EventNote, null) },
                    label = { Text("Leaves") }
                )
                NavigationBarItem(
                    selected = activeTab == "analytics",
                    onClick = { activeTab = "analytics" },
                    icon = { Icon(Icons.Default.TrendingUp, null) },
                    label = { Text("Metrics") }
                )
                NavigationBarItem(
                    selected = activeTab == "exports",
                    onClick = { activeTab = "exports" },
                    icon = { Icon(Icons.Default.CloudDownload, null) },
                    label = { Text("Export") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(admin.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Role: Admin Panel", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.Default.PowerSettingsNew, "Logout", tint = MaterialTheme.colorScheme.error)
                }
            }

            AnimatedContent(targetState = activeTab, label = "AdminTabTransitions") { tab ->
                when (tab) {
                    "logs" -> AdminLogsTab(viewModel, students, attendanceLogs, departments)
                    "leaves" -> AdminLeavesTab(viewModel, leafRequests, students, admin.name)
                    "analytics" -> AdminAnalyticsTab(viewModel, students, attendanceLogs)
                    "exports" -> AdminExportTab(viewModel, reportsLogs, admin.name) { body ->
                        docReportFeed = body
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLogsTab(viewModel: AppViewModel, students: List<User>, logs: List<Attendance>, depts: List<Department>) {
    val deptsMap = depts.associateBy { it.id }
    val studMap = students.associateBy { it.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Daily Swipe Records", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (logs.isEmpty()) {
            item {
                Text("No clock signals recorded yet today.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(logs) { log ->
                val s = studMap[log.studentId]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(s?.name ?: "Unknown Student", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Reg No: ${s?.registerNumber ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Dept: ${deptsMap[s?.departmentId]?.name ?: "None"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(log.remarks ?: "Clock trace verified", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(log.checkInTime ?: "No Time", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val pillColor = when (log.status) {
                                "Present" -> Color(0xFF81C784)
                                "Late" -> Color(0xFFFFB74D)
                                "Absent" -> Color(0xFFE57373)
                                else -> Color(0xFF9575CD)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(pillColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(log.status, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLeavesTab(viewModel: AppViewModel, leafRequests: List<LeaveRequest>, students: List<User>, adminName: String) {
    val studMap = students.associateBy { it.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Pending Leave Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        val pendings = leafRequests.filter { it.status == "Pending" }
        if (pendings.isEmpty()) {
            item {
                Text("Excellent! No pending leave applications.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(pendings) { leave ->
                val s = studMap[leave.studentId]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(s?.name ?: "Student Detail", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Reg No: ${s?.registerNumber ?: "N/A"}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Category: ${leave.leaveType}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text("Duration: ${leave.startDate} to ${leave.endDate}", fontSize = 13.sp)
                        Text("Reason: ${leave.reason}", fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.approveOrRejectLeave(leave.id, true, adminName) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("APPROVE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.approveOrRejectLeave(leave.id, false, adminName) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("REJECT", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(viewModel: AppViewModel, students: List<User>, logs: List<Attendance>) {
    val totalStudents = students.size.coerceAtLeast(1)
    val todayDate = viewModel.getTodayDateString()
    val logsToday = logs.filter { it.date == todayDate }

    val presents = logsToday.count { it.status == "Present" }
    val lates = logsToday.count { it.status == "Late" }
    val absents = totalStudents - presents - lates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Administrative Analytics Grid", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        // Metrics Row Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Presents Today", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                    Text("$presents students", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Late Logins Today", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                    Text("$lates students", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Remaining Absents", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
                    Text("$absents students", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Custom canvas bar histograms
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Hourly Peak Attendance Logs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Local check-in telemetry histogram:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val bars = listOf(
                        Pair("08 AM", 0.9f),
                        Pair("09 AM", 0.6f),
                        Pair("10 AM", 0.3f),
                        Pair("11 AM", 0.1f),
                        Pair("12 PM", 0.05f),
                        Pair("01 PM", 0.25f)
                    )
                    val widthSpace = size.width / (bars.size)
                    val bottomY = size.height - 30f

                    // Draw baseline
                    drawLine(Color.Gray.copy(alpha = 0.5f), start = Offset(0f, bottomY), end = Offset(size.width, bottomY), strokeWidth = 2f)

                    bars.forEachIndexed { idx, bar ->
                        val barHeight = bottomY * bar.second
                        val startX = (idx * widthSpace) + (widthSpace * 0.2f)
                        val endX = startX + (widthSpace * 0.6f)

                        // Draw Rounded Bar
                        drawRoundRect(
                            color = Color(0xFF81C784),
                            topLeft = Offset(startX, bottomY - barHeight),
                            size = Size(endX - startX, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }
            }
        }

        // Segment line graph of past 5 days
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Daily Trend Rate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val points = listOf(60f, 75f, 82f, 80f, 92f)
                    val spaceX = size.width / (points.size - 1)
                    val path = Path()

                    points.forEachIndexed { i, p ->
                        val currentY = size.height - (size.height * (p / 100f))
                        val currentX = i * spaceX
                        if (i == 0) path.moveTo(currentX, currentY)
                        else path.lineTo(currentX, currentY)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF9575CD),
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminExportTab(viewModel: AppViewModel, reports: List<ReportEntity>, adminName: String, onBufferReport: (String) -> Unit) {
    var title by remember { mutableStateOf("Main Campus Attendance Log") }
    var rawText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("CSV Local Data Exporter", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Report Save Label") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val body = viewModel.generateReportCSV(title, "Daily", adminName)
                        rawText = body
                        onBufferReport(body)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate, Export & Save DB Entry", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (rawText.isNotEmpty()) {
            Text("Console File Preview Buffer", fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = rawText,
                    color = Color(0xFF81C784),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Text("Registered Database Reports History", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        reports.forEach { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(r.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(r.summaryText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}


// ==========================================
// 6. SUPER-ADMIN DASHBOARD
// ==========================================
@Composable
fun SuperAdminDashboard(viewModel: AppViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    if (authState !is AppViewModel.AuthState.SuperAdminLoggedIn) return
    val sa = (authState as AppViewModel.AuthState.SuperAdminLoggedIn).superAdmin

    val admins by viewModel.allAdmins.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val departments by viewModel.allDepartments.collectAsStateWithLifecycle()
    val activeSettings by viewModel.activeSettings.collectAsStateWithLifecycle()
    val activeLocation by viewModel.activeLocation.collectAsStateWithLifecycle()
    val securityLogs by viewModel.allSecurityLogs.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("admins") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = activeTab == "admins",
                    onClick = { activeTab = "admins" },
                    icon = { Icon(Icons.Default.SupervisedUserCircle, null) },
                    label = { Text("Admins") }
                )
                NavigationBarItem(
                    selected = activeTab == "gates",
                    onClick = { activeTab = "gates" },
                    icon = { Icon(Icons.Default.SettingsInputAntenna, null) },
                    label = { Text("Limits") }
                )
                NavigationBarItem(
                    selected = activeTab == "scheduler",
                    onClick = { activeTab = "scheduler" },
                    icon = { Icon(Icons.Default.HourglassEmpty, null) },
                    label = { Text("Scheduler") }
                )
                NavigationBarItem(
                    selected = activeTab == "security",
                    onClick = { activeTab = "security" },
                    icon = { Icon(Icons.Default.GppBad, null) },
                    label = { Text("Sec-Logs") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(sa.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Role: Portal Super Admin", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.Default.PowerSettingsNew, "Logout", tint = MaterialTheme.colorScheme.error)
                }
            }

            AnimatedContent(targetState = activeTab, label = "SuperTabTransitions") { tab ->
                when (tab) {
                    "admins" -> SuperAdminsTab(viewModel, admins, departments)
                    "gates" -> SuperGatesTab(viewModel, activeSettings ?: AttendanceSettings(), activeLocation ?: AttendanceLocation())
                    "scheduler" -> SuperSchedulerTab(viewModel, sa.email, students.size)
                    "security" -> SuperSecurityTab(viewModel, securityLogs)
                }
            }
        }
    }
}

@Composable
fun SuperAdminsTab(viewModel: AppViewModel, admins: List<Admin>, departments: List<Department>) {
    var adminName by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminPass by remember { mutableStateOf("") }

    var deptExpanded by remember { mutableStateOf(false) }
    var selectedDept by remember { mutableStateOf<Department?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Register College Administrator", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = adminName,
                    onValueChange = { adminName = it },
                    label = { Text("Admin Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Admin Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = adminPass,
                    onValueChange = { adminPass = it },
                    label = { Text("Initial Strong Password") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional Dept dropdown mapping
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDept?.name ?: "All Departments (Global Campus Admin)",
                        onValueChange = {},
                        label = { Text("Restrict Admin Department (Optional)") },
                        leadingIcon = { Icon(Icons.Default.HomeWork, null) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { deptExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text("Global / Entire Campus Scope") },
                            onClick = {
                                selectedDept = null
                                deptExpanded = false
                            }
                        )
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text("[${dept.code}] ${dept.name}") },
                                onClick = {
                                    selectedDept = dept
                                    deptExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (adminName.isNotBlank() && adminEmail.isNotBlank()) {
                            viewModel.createAdmin(adminName, adminEmail, adminPass, selectedDept?.id)
                            adminName = ""
                            adminEmail = ""
                            adminPass = ""
                            selectedDept = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register Admin Credentials", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Registered Gatekeepers & Staff List", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (admins.isEmpty()) {
            Text("No standard administrator staff profiles registered.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            admins.forEach { ad ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(ad.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(ad.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val scope = if (ad.departmentId == null) "Global Scope" else "Dept Restriction ID: ${ad.departmentId}"
                            Text("Scope: $scope", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { viewModel.deleteAdmin(ad.id, ad.email) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuperGatesTab(viewModel: AppViewModel, settings: AttendanceSettings, location: AttendanceLocation) {
    var latText by remember { mutableStateOf(location.latitude.toString()) }
    var lngText by remember { mutableStateOf(location.longitude.toString()) }
    var radValue by remember { mutableStateOf(location.radius) }
    var campusName by remember { mutableStateOf(location.name) }

    var startTime by remember { mutableStateOf(settings.startTime) }
    var lateTime by remember { mutableStateOf(settings.lateTime) }
    var endTime by remember { mutableStateOf(settings.endTime) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("GPS Radius Geofence Shield", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = campusName,
                    onValueChange = { campusName = it },
                    label = { Text("Geofence Location Label") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Campus Center Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lngText,
                    onValueChange = { lngText = it },
                    label = { Text("Campus Center Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Allowed Geofence Radius Limit: ${radValue.toInt()} meters", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = radValue.toFloat(),
                    onValueChange = { radValue = it.toDouble() },
                    valueRange = 50f..1000f,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val lat = latText.toDoubleOrNull() ?: location.latitude
                        val lng = lngText.toDoubleOrNull() ?: location.longitude
                        viewModel.updateLocation(campusName, lat, lng, radValue)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Bounds Coordinates & Radius", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("College Class Timing Thresholds", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Roll-call Start Hour (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lateTime,
                    onValueChange = { lateTime = it },
                    label = { Text("Tardy / Late Mark Hour (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Class End and Dismissal Hour (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.updateSettings(startTime, endTime, lateTime)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Attendance Hours", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SuperSchedulerTab(viewModel: AppViewModel, email: String, totalStudents: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Alarm, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Text("Daily Auto-Absent Synchronization Scheduler", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

        Text(
            text = "Executing this synchronized task crawls all $totalStudents students in the registrar for today's date. Any student who fails to check-in before the target late clock hour is automatically set to ABSENT inside of the local database log files.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Button(
            onClick = { viewModel.triggerAutoAbsentScheduler(email) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Simulate Auto-Mark Scheduler Now", fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Column {
                Text("Automation Scheduler Specifications", fontWeight = FontWeight.Normal, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Frequency: Runs automatically once daily at 16:30 IST", fontSize = 13.sp)
                Text("• Targets: Registered candidates failing campus biometric bounds", fontSize = 13.sp)
                Text("• Leaves Alignment: Correctly retains APPROVED leaves as Leave category", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SuperSecurityTab(viewModel: AppViewModel, logs: List<SecurityLog>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Device Anti-Fraud Security Events", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (logs.isEmpty()) {
            item {
                Text("All systems secure. No spoofing incidents verified today.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(logs) { log ->
                val cardColor = if (log.eventType == "POLICY_VIOLATION" || log.eventType == "FAKE_GPS") {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.eventType, fontWeight = FontWeight.Black, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                            val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            Text(formattedTime, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text("Caller: ${log.userId} (${log.role})", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        Text(log.description, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        Text("Validated Hardware: ${log.deviceModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
