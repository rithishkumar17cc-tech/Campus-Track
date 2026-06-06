package com.example.data

import android.content.Context
import com.example.security.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {

    // Bootstraps default values if they are empty
    suspend fun initializeDefaults() {
        // 1. Settings Init
        if (appDao.getSettings() == null) {
            appDao.insertSettings(AttendanceSettings())
        }

        // 2. Location Init
        if (appDao.getLocation() == null) {
            appDao.insertLocation(AttendanceLocation())
        }

        // 3. Departments Init
        val currentDepts = appDao.getAllDepartments()
        if (currentDepts.isEmpty()) {
            appDao.insertDepartment(Department(name = "Computer Science & Engineering", code = "CSE"))
            appDao.insertDepartment(Department(name = "Electronics & Communication Engineering", code = "ECE"))
            appDao.insertDepartment(Department(name = "Information Technology", code = "IT"))
            appDao.insertDepartment(Department(name = "Mechanical Engineering", code = "ME"))
        }
    }

    // --- Super Admin ---
    val superAdminFlow: Flow<SuperAdmin?> = appDao.getSuperAdminFlow()
    suspend fun getSuperAdmin() = appDao.getSuperAdmin()
    suspend fun createSuperAdmin(admin: SuperAdmin) = appDao.insertSuperAdmin(admin)
    suspend fun getSuperAdminByEmail(email: String) = appDao.getSuperAdminByEmail(email)

    // --- Admins ---
    val allAdminsFlow: Flow<List<Admin>> = appDao.getAllAdminsFlow()
    suspend fun createAdmin(admin: Admin) = appDao.insertAdmin(admin)
    suspend fun deleteAdmin(adminId: Int) = appDao.deleteAdmin(adminId)
    suspend fun resetAdminPassword(adminId: Int, newHash: String) = appDao.resetAdminPassword(adminId, newHash)
    suspend fun getAdminByEmail(email: String) = appDao.getAdminByEmail(email)

    // --- Students ---
    val allStudentsFlow: Flow<List<User>> = appDao.getAllStudentsFlow()
    suspend fun getAllStudents() = appDao.getAllStudents()
    suspend fun getStudentByRegisterNumber(regNo: String) = appDao.getStudentByRegisterNumber(regNo)
    suspend fun getStudentByEmail(email: String) = appDao.getStudentByEmail(email)
    suspend fun registerStudent(student: User): Long = appDao.insertStudent(student)
    suspend fun updateStudent(student: User) = appDao.updateStudent(student)
    suspend fun deleteStudent(studentId: Int) = appDao.deleteStudent(studentId)
    suspend fun getStudentById(studentId: Int) = appDao.getStudentById(studentId)

    // --- Departments ---
    val allDepartmentsFlow: Flow<List<Department>> = appDao.getAllDepartmentsFlow()
    suspend fun getAllDepartments() = appDao.getAllDepartments()
    suspend fun addDepartment(department: Department) = appDao.insertDepartment(department)
    suspend fun deleteDepartment(deptId: Int) = appDao.deleteDepartment(deptId)

    // --- Attendance Settings ---
    val settingsFlow: Flow<AttendanceSettings?> = appDao.getSettingsFlow()
    suspend fun getSettings() = appDao.getSettings()
    suspend fun saveSettings(settings: AttendanceSettings) = appDao.insertSettings(settings)

    // --- Location Center ---
    val locationFlow: Flow<AttendanceLocation?> = appDao.getLocationFlow()
    suspend fun getLocation() = appDao.getLocation()
    suspend fun saveLocation(location: AttendanceLocation) = appDao.insertLocation(location)

    // --- Attendance Logs ---
    val allAttendanceFlow: Flow<List<Attendance>> = appDao.getAllAttendanceFlow()
    fun getAttendanceForDateFlow(date: String) = appDao.getAttendanceForDateFlow(date)
    suspend fun getAttendanceForDate(date: String) = appDao.getAttendanceForDate(date)
    fun getStudentAttendanceFlow(studentId: Int) = appDao.getStudentAttendanceFlow(studentId)
    suspend fun getStudentAttendanceForDate(studentId: Int, date: String) = appDao.getStudentAttendanceForDate(studentId, date)
    suspend fun recordAttendance(attendance: Attendance) {
        val existing = appDao.getStudentAttendanceForDate(attendance.studentId, attendance.date)
        if (existing != null) {
            // Update retaining checks if necessary
            appDao.updateAttendance(attendance.copy(id = existing.id))
        } else {
            appDao.insertAttendance(attendance)
        }
    }

    // --- Leave Requests ---
    val allLeaveRequestsFlow: Flow<List<LeaveRequest>> = appDao.getAllLeaveRequestsFlow()
    fun getStudentLeaveRequestsFlow(studentId: Int) = appDao.getStudentLeaveRequestsFlow(studentId)
    suspend fun applyLeave(leaveRequest: LeaveRequest) = appDao.insertLeaveRequest(leaveRequest)
    suspend fun updateLeaveStatus(leaveId: Int, status: String, adminName: String) = appDao.updateLeaveStatus(leaveId, status, adminName)

    // --- Notifications ---
    val allNotificationsFlow: Flow<List<NotificationEntity>> = appDao.getAllNotificationsFlow()
    suspend fun sendNotification(notif: NotificationEntity) = appDao.insertNotification(notif)
    suspend fun markNotificationAsRead(notifId: Int) = appDao.markNotificationAsRead(notifId)

    // --- Reports ---
    val allReportsFlow: Flow<List<ReportEntity>> = appDao.getAllReportsFlow()
    suspend fun saveReport(report: ReportEntity) = appDao.insertReport(report)

    // --- Security Logs ---
    val allSecurityLogsFlow: Flow<List<SecurityLog>> = appDao.getAllSecurityLogsFlow()
    suspend fun logSecurityEvent(userId: String, role: String, eventType: String, description: String, deviceModel: String) {
        val log = SecurityLog(
            userId = userId,
            role = role,
            eventType = eventType,
            description = description,
            deviceModel = deviceModel
        )
        appDao.insertSecurityLog(log)
    }
}
