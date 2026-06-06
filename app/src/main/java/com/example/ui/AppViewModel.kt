package com.example.ui

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.security.GpsUtils
import com.example.security.SecurityUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao())

    // --- Authentication State ---
    sealed interface AuthState {
        object Loading : AuthState
        object FirstTimeSetup : AuthState // No Super Admin exists yet
        object Guest : AuthState
        data class StudentLoggedIn(val user: User) : AuthState
        data class AdminLoggedIn(val admin: Admin) : AuthState
        data class SuperAdminLoggedIn(val superAdmin: SuperAdmin) : AuthState
        data class Error(val message: String) : AuthState
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // --- Active Flow States ---
    val allStudents: StateFlow<List<User>> = repository.allStudentsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allAdmins: StateFlow<List<Admin>> = repository.allAdminsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allDepartments: StateFlow<List<Department>> = repository.allDepartmentsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendanceFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allLeaveRequests: StateFlow<List<LeaveRequest>> = repository.allLeaveRequestsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotificationsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allReports: StateFlow<List<ReportEntity>> = repository.allReportsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allSecurityLogs: StateFlow<List<SecurityLog>> = repository.allSecurityLogsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeSettings: StateFlow<AttendanceSettings?> = repository.settingsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val activeLocation: StateFlow<AttendanceLocation?> = repository.locationFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // --- Student Active Attendance and Security ---
    private val _currentSelectedPreset = MutableStateFlow(GpsUtils.Presets[0])
    val currentSelectedPreset = _currentSelectedPreset.asStateFlow()

    private val _securityStatus = MutableStateFlow(SecurityUtils.performSelfSecurityCheck())
    val securityStatus = _securityStatus.asStateFlow()

    // --- Navigation Helper ---
    private val _activeScreen = MutableStateFlow("auth_login")
    val activeScreen = _activeScreen.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaults()
            checkSuperAdminExistence()
        }
    }

    fun navigateTo(screen: String) {
        _activeScreen.value = screen
    }

    private suspend fun checkSuperAdminExistence() {
        val superAdmin = repository.getSuperAdmin()
        if (superAdmin == null) {
            _authState.value = AuthState.FirstTimeSetup
            _activeScreen.value = "setup_wizard"
        } else {
            _authState.value = AuthState.Guest
            _activeScreen.value = "auth_login"
        }
    }

    // --- Helper date formatter ---
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getNowTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- Super Admin Actions ---
    fun registerSuperAdmin(name: String, email: String, pass: String) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _authState.value = AuthState.Error("Fields cannot be empty")
                return@launch
            }
            val sa = SuperAdmin(name = name, email = email, passwordHash = pass)
            repository.createSuperAdmin(sa)
            repository.logSecurityEvent(email, "SUPERADMIN", "SUPERADMIN_REGISTER", "First time setup complete.", "Nexus 6S")
            _authState.value = AuthState.SuperAdminLoggedIn(sa)
            _activeScreen.value = "superadmin_dashboard"
        }
    }

    fun createAdmin(name: String, email: String, pass: String, departmentId: Int?) {
        viewModelScope.launch {
            val exists = repository.getAdminByEmail(email) ?: repository.getSuperAdminByEmail(email) ?: repository.getStudentByEmail(email)
            if (exists != null) {
                repository.logSecurityEvent("SYSTEM", "SUPERADMIN", "POLICY_VIOLATION", "Attempted duplicate email register: $email", "Simulated Root")
                return@launch
            }
            val admin = Admin(name = name, email = email, passwordHash = pass, departmentId = departmentId)
            repository.createAdmin(admin)
            sendSystemNotification("New Admin Added", "Admin $name joined CampusTrack management panel.", "ALL")
        }
    }

    fun deleteAdmin(id: Int, email: String) {
        viewModelScope.launch {
            repository.deleteAdmin(id)
            repository.logSecurityEvent(email, "SUPERADMIN", "POLICY_VIOLATION", "Deleted admin ID $id", "Nexus")
        }
    }

    fun resetAdminPassword(id: Int, newPass: String, email: String) {
        viewModelScope.launch {
            repository.resetAdminPassword(id, newPass)
            repository.logSecurityEvent(email, "SUPERADMIN", "POLICY_VIOLATION", "Admin password reset ID $id", "Nexus")
        }
    }

    fun addDepartment(name: String, code: String) {
        viewModelScope.launch {
            repository.addDepartment(Department(name = name, code = code))
        }
    }

    fun deleteDepartment(id: Int) {
        viewModelScope.launch {
            repository.deleteDepartment(id)
        }
    }

    fun updateSettings(start: String, end: String, late: String) {
        viewModelScope.launch {
            repository.saveSettings(AttendanceSettings(startTime = start, endTime = end, lateTime = late))
        }
    }

    fun updateLocation(name: String, lat: Double, lng: Double, rad: Double) {
        viewModelScope.launch {
            repository.saveLocation(AttendanceLocation(name = name, latitude = lat, longitude = lng, radius = rad))
        }
    }

    // --- Common Authentications ---
    fun login(identifier: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // 1. Check Super Admin
            val sa = repository.getSuperAdmin()
            if (sa != null && sa.email.equals(identifier, true) && sa.passwordHash == pass) {
                _authState.value = AuthState.SuperAdminLoggedIn(sa)
                _activeScreen.value = "superadmin_dashboard"
                repository.logSecurityEvent(identifier, "SUPERADMIN", "LOGIN_SUCCESS", "Logged in via Email authentication", "Device Emulator")
                return@launch
            }

            // 2. Check Admin
            val admin = repository.getAdminByEmail(identifier)
            if (admin != null && admin.passwordHash == pass) {
                _authState.value = AuthState.AdminLoggedIn(admin)
                _activeScreen.value = "admin_dashboard"
                repository.logSecurityEvent(identifier, "ADMIN", "LOGIN_SUCCESS", "Logged in to Admin Dashboard", "Android Tablet")
                return@launch
            }

            // 3. Check Student (Email or Register Number)
            val student = repository.getStudentByEmail(identifier) ?: repository.getStudentByRegisterNumber(identifier)
            if (student != null && student.passwordHash == pass) {
                _authState.value = AuthState.StudentLoggedIn(student)
                _activeScreen.value = "student_dashboard"
                repository.logSecurityEvent(student.registerNumber, "STUDENT", "LOGIN_SUCCESS", "Logged in successfully to mobile dashboard", "Android Core")
                return@launch
            }

            // Fail
            repository.logSecurityEvent(identifier, "GUEST", "LOGIN_FAILED", "Failed log-in attempt matching '$identifier'", "Device")
            _authState.value = AuthState.Error("Invalid login credentials")
            _activeScreen.value = "auth_login"
        }
    }

    fun logout() {
        _authState.value = AuthState.Guest
        _activeScreen.value = "auth_login"
    }

    // --- Student Registration ---
    fun registerStudent(
        name: String,
        registerNumber: String,
        departmentId: Int,
        mobileNumber: String,
        email: String,
        pass: String
    , onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || registerNumber.isBlank() || departmentId == 0 || mobileNumber.isBlank() || email.isBlank() || pass.isBlank()) {
                onComplete(false, "Please fill in all registration fields.")
                return@launch
            }
            val existingEmail = repository.getStudentByEmail(email) ?: repository.getAdminByEmail(email) ?: repository.getSuperAdminByEmail(email)
            if (existingEmail != null) {
                onComplete(false, "This email is already in use by another user.")
                return@launch
            }
            val existingReg = repository.getStudentByRegisterNumber(registerNumber)
            if (existingReg != null) {
                onComplete(false, "This Register Number is already registered.")
                return@launch
            }

            val student = User(
                name = name,
                registerNumber = registerNumber,
                departmentId = departmentId,
                mobileNumber = mobileNumber,
                email = email,
                passwordHash = pass,
                profilePhotoUri = "avatar_${(1..4).random()}" // Simulation of select profile photo
            )
            repository.registerStudent(student)
            _authState.value = AuthState.StudentLoggedIn(student)
            _activeScreen.value = "student_dashboard"
            sendSystemNotification("Registration Successful", "Welcome $name (ID $registerNumber) to CampusTrack!", "STUDENT")
            onComplete(true, "Successfully registered.")
        }
    }

    fun updateStudentProfile(updated: User) {
        viewModelScope.launch {
            repository.updateStudent(updated)
            _authState.value = AuthState.StudentLoggedIn(updated)
        }
    }

    // --- GPS Scenario Selector (Testing Simulation helper) ---
    fun selectGpsPreset(preset: GpsUtils.LocationPreset) {
        _currentSelectedPreset.value = preset
        _securityStatus.value = SecurityUtils.performSelfSecurityCheck(
            isMockLocationSimulated = preset.isMockLocation,
            isFakeGpsSimulated = preset.isFakeGps
        )
    }

    // --- Student Check-in / Check-out ---
    fun submitStudentAttendance(isCheckIn: Boolean, studentId: Int, regNo: String) {
        viewModelScope.launch {
            val preset = _currentSelectedPreset.value
            val sec = _securityStatus.value
            val campus = repository.getLocation() ?: AttendanceLocation()
            val settings = repository.getSettings() ?: AttendanceSettings()

            // 1. Calculate relative distance
            val distance = GpsUtils.calculateDistanceMeters(preset.latitude, preset.longitude, campus.latitude, campus.longitude)

            // Logging security incident if spoof or mock or rooted/emulator
            if (sec.hasBreaches) {
                val breachDesc = "Breach indicators: Rooted=${sec.isRooted}, Emulator=${sec.isEmulator}, MockGps=${sec.isMockLocation}, FakeGps=${sec.isFakeGps}"
                repository.logSecurityEvent(regNo, "STUDENT", "POLICY_VIOLATION", "TAMPER REPORT: Attendance block in response to device security failure. $breachDesc", Build.MODEL)
                return@launch
            }

            // 2. Out of campus radius gate validation
            if (distance > campus.radius) {
                repository.logSecurityEvent(regNo, "STUDENT", "FAKE_GPS", "Out of bounds attempt. Tried to check submission at distance: ${distance.toInt()} meters (Allowed limit=${campus.radius}m)", Build.MODEL)
                return@launch
            }

            // Determine arrival status depending on settings & actual local time
            val currentTime = getNowTimeString()
            var status = "Present"
            // Simple string comparison for time to declare Late Arrival
            if (isCheckIn && currentTime > settings.lateTime + ":00") {
                status = "Late"
            }

            val todayDate = getTodayDateString()
            val existing = repository.getStudentAttendanceForDate(studentId, todayDate)

            val log = if (existing != null) {
                existing.copy(
                    checkOutTime = if (!isCheckIn) currentTime else existing.checkOutTime,
                    latitude = preset.latitude,
                    longitude = preset.longitude,
                    distanceFromCenter = distance,
                    status = if (status == "Late") "Late" else existing.status
                )
            } else {
                Attendance(
                    studentId = studentId,
                    date = todayDate,
                    checkInTime = if (isCheckIn) currentTime else null,
                    checkOutTime = if (!isCheckIn) currentTime else null,
                    latitude = preset.latitude,
                    longitude = preset.longitude,
                    distanceFromCenter = distance,
                    accuracy = 5.2,
                    status = status,
                    remarks = "GPS Geolocation validated within ${distance.toInt()}m"
                )
            }

            repository.recordAttendance(log)
            repository.logSecurityEvent(regNo, "STUDENT", "LOGIN_SUCCESS", "Logged position on Campus. Location: ${preset.name}, Distance: ${distance.toInt()}m", Build.MODEL)
            sendSystemNotification("Attendance Recorded", "$regNo tracked successfully as $status for $todayDate at $currentTime.", "STUDENT")

            // REFRESH logged-in student info to trigger UI reload
            val currentLoggedIn = _authState.value
            if (currentLoggedIn is AuthState.StudentLoggedIn) {
                val refreshed = repository.getStudentById(studentId)
                if (refreshed != null) {
                    _authState.value = AuthState.StudentLoggedIn(refreshed)
                }
            }
        }
    }

    // --- Leave Application Workflows ---
    fun applyLeave(studentId: Int, regNo: String, type: String, reason: String, start: String, end: String, docUrl: String) {
        viewModelScope.launch {
            if (reason.isBlank() || start.isBlank() || end.isBlank()) return@launch
            val req = LeaveRequest(
                studentId = studentId,
                leaveType = type,
                reason = reason,
                startDate = start,
                endDate = end,
                documentPath = if (docUrl.isNotBlank()) docUrl else null
            )
            repository.applyLeave(req)
            sendSystemNotification("Leave Requested", "Student $regNo applied Leave starting $start to $end.", "ADMIN")
        }
    }

    fun approveOrRejectLeave(leaveId: Int, isApprove: Boolean, adminName: String) {
        viewModelScope.launch {
            val status = if (isApprove) "Approved" else "Rejected"
            repository.updateLeaveStatus(leaveId, status, adminName)
            sendSystemNotification("Leave Request status updated", "Leave application ID $leaveId status updated to $status by $adminName.", "ALL")
        }
    }

    // --- Notification Sender ---
    fun sendSystemNotification(title: String, message: String, role: String, departmentId: Int? = null) {
        viewModelScope.launch {
            val notif = NotificationEntity(
                title = title,
                message = message,
                targetRole = role,
                departmentId = departmentId
            )
            repository.sendNotification(notif)
        }
    }

    // --- Auto-Absent Scheduler ---
    fun triggerAutoAbsentScheduler(adminEmail: String) {
        viewModelScope.launch {
            val students = repository.getAllStudents()
            val todayDate = getTodayDateString()
            var absentCount = 0

            for (student in students) {
                val attn = repository.getStudentAttendanceForDate(student.id, todayDate)
                if (attn == null) {
                    // Check if student has an approved leave request covering today
                    val leaves = allLeaveRequests.value.filter { it.studentId == student.id && it.status == "Approved" }
                    val leavesForToday = leaves.any { todayDate >= it.startDate && todayDate <= it.endDate }

                    val markedStatus = if (leavesForToday) "Leave" else "Absent"
                    val autoMarked = Attendance(
                        studentId = student.id,
                        date = todayDate,
                        status = markedStatus,
                        remarks = "Auto-Marked by daily deadline clock scheduler"
                    )
                    repository.recordAttendance(autoMarked)
                    absentCount++
                }
            }

            repository.logSecurityEvent(adminEmail, "SYSTEM", "POLICY_VIOLATION", "Executed Auto-Absent Sweep. System synchronized. Marked $absentCount students Absent.", Build.MODEL)
            sendSystemNotification("Auto-Sweep Finished", "Daily absent synchronization scheduled task completed. Identified $absentCount unregistered students.", "ALL")
        }
    }

    // --- Reports Local Exporter ---
    fun generateReportCSV(title: String, reportType: String, adminName: String): String {
        val today = getTodayDateString()
        val headers = "Student ID,Register No,Name,Department,Date,Status,Check In,Check Out,Remarks,Location Offset\n"
        val builder = java.lang.StringBuilder()
        builder.append("CAMPUS-TRACK SYSTEM LOG EXPORT - $title\n")
        builder.append("Generated by: $adminName on $today\n\n")
        builder.append(headers)

        val deptsMap = allDepartments.value.associateBy { it.id }
        val attendanceLogs = allAttendance.value
        val studentsMap = allStudents.value.associateBy { it.id }

        for (log in attendanceLogs) {
            val stud = studentsMap[log.studentId]
            val deptName = stud?.let { deptsMap[it.departmentId]?.name } ?: "Unknown"
            builder.append("${log.studentId},")
            builder.append("${stud?.registerNumber ?: "N/A"},")
            builder.append("${stud?.name ?: "Unknown"},")
            builder.append("$deptName,")
            builder.append("${log.date},")
            builder.append("${log.status},")
            builder.append("${log.checkInTime ?: "N/A"},")
            builder.append("${log.checkOutTime ?: "N/A"},")
            builder.append("${log.remarks ?: "None"},")
            builder.append("${log.distanceFromCenter?.toInt() ?: 0}m\n")
        }

        val reportBody = builder.toString()
        viewModelScope.launch {
            repository.saveReport(
                ReportEntity(
                    title = title,
                    reportType = reportType,
                    summaryText = "Report generated successfully. Rows: ${attendanceLogs.size}. Source file buffered."
                )
            )
        }
        return reportBody
    }
}
