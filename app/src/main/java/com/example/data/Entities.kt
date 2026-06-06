package com.example.data

import androidx.room.*

@Entity(tableName = "super_admins")
data class SuperAdmin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admins")
data class Admin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val departmentId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val registerNumber: String,
    val departmentId: Int,
    val mobileNumber: String,
    val email: String,
    val passwordHash: String,
    val profilePhotoUri: String? = null,
    val deviceToken: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val date: String, // YYYY-MM-DD
    val checkInTime: String? = null, // HH:MM:SS
    val checkOutTime: String? = null, // HH:MM:SS
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceFromCenter: Double? = null,
    val accuracy: Double? = null,
    val status: String, // Present, Absent, Late, Half-Day, Leave
    val remarks: String? = null
)

@Entity(tableName = "attendance_settings")
data class AttendanceSettings(
    @PrimaryKey val id: Int = 1, // Single active setting row
    val startTime: String = "08:30", // HH:MM
    val endTime: String = "16:30", // HH:MM
    val lateTime: String = "09:00" // HH:MM
)

@Entity(tableName = "attendance_locations")
data class AttendanceLocation(
    @PrimaryKey val id: Int = 1, // Single primary campus location
    val name: String = "Main Campus (Valayankulam)",
    val latitude: Double = 9.8075, // Madurai Hwy (College site coordinate)
    val longitude: Double = 78.0915,
    val radius: Double = 150.0 // Configured boundary radius (meters)
)

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val leaveType: String, // Medical Leave, Personal Leave, Emergency Leave
    val reason: String,
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val documentPath: String? = null,
    val status: String = "Pending", // Pending, Approved, Rejected
    val approvedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val targetRole: String, // ALL, STUDENT, ADMIN
    val departmentId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val reportType: String, // Daily, Weekly, Monthly, Department
    val departmentId: Int? = null,
    val studentId: Int? = null,
    val summaryText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_logs")
data class SecurityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Email or Register Number
    val role: String, // STUDENT, ADMIN, SUPERADMIN, SYSTEM
    val eventType: String, // FAKE_GPS, ROOT_DETECTED, EMULATOR_DETECTED, LOGIN_FAILED, POLICY_VIOLATION
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String = "Android Device"
)
