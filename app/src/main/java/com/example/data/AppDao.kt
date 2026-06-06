package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Super Admin Queries ---
    @Query("SELECT * FROM super_admins LIMIT 1")
    fun getSuperAdminFlow(): Flow<SuperAdmin?>

    @Query("SELECT * FROM super_admins LIMIT 1")
    suspend fun getSuperAdmin(): SuperAdmin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuperAdmin(superAdmin: SuperAdmin)

    @Query("SELECT * FROM super_admins WHERE email = :email LIMIT 1")
    suspend fun getSuperAdminByEmail(email: String): SuperAdmin?

    // --- Admin Queries ---
    @Query("SELECT * FROM admins ORDER BY id DESC")
    fun getAllAdminsFlow(): Flow<List<Admin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: Admin)

    @Query("SELECT * FROM admins WHERE email = :email LIMIT 1")
    suspend fun getAdminByEmail(email: String): Admin?

    @Query("DELETE FROM admins WHERE id = :adminId")
    suspend fun deleteAdmin(adminId: Int)

    @Query("UPDATE admins SET passwordHash = :newHash WHERE id = :adminId")
    suspend fun resetAdminPassword(adminId: Int, newHash: String)

    // --- User (Student) Queries ---
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllStudentsFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllStudents(): List<User>

    @Query("SELECT * FROM users WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Int): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getStudentByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE registerNumber = :regNo LIMIT 1")
    suspend fun getStudentByRegisterNumber(regNo: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: User): Long

    @Update
    suspend fun updateStudent(student: User)

    @Query("DELETE FROM users WHERE id = :studentId")
    suspend fun deleteStudent(studentId: Int)

    // --- Department Queries ---
    @Query("SELECT * FROM departments ORDER BY code ASC")
    fun getAllDepartmentsFlow(): Flow<List<Department>>

    @Query("SELECT * FROM departments")
    suspend fun getAllDepartments(): List<Department>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: Department)

    @Query("DELETE FROM departments WHERE id = :deptId")
    suspend fun deleteDepartment(deptId: Int)

    // --- Attendance Queries ---
    @Query("SELECT * FROM attendance ORDER BY id DESC")
    fun getAllAttendanceFlow(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDateFlow(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceForDate(date: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getStudentAttendanceFlow(studentId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getStudentAttendanceForDate(studentId: Int, date: String): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    // --- Settings Queries ---
    @Query("SELECT * FROM attendance_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AttendanceSettings?>

    @Query("SELECT * FROM attendance_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AttendanceSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AttendanceSettings)

    // --- Location Queries ---
    @Query("SELECT * FROM attendance_locations WHERE id = 1 LIMIT 1")
    fun getLocationFlow(): Flow<AttendanceLocation?>

    @Query("SELECT * FROM attendance_locations WHERE id = 1 LIMIT 1")
    suspend fun getLocation(): AttendanceLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: AttendanceLocation)

    // --- Leave Requests Queries ---
    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeaveRequestsFlow(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE studentId = :studentId ORDER BY id DESC")
    fun getStudentLeaveRequestsFlow(studentId: Int): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(leaveRequest: LeaveRequest)

    @Query("UPDATE leave_requests SET status = :status, approvedBy = :adminName WHERE id = :leaveId")
    suspend fun updateLeaveStatus(leaveId: Int, status: String, adminName: String)

    // --- Notifications Queries ---
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notifId")
    suspend fun markNotificationAsRead(notifId: Int)

    // --- Reports Queries ---
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    // --- Security Logs Queries ---
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC")
    fun getAllSecurityLogsFlow(): Flow<List<SecurityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityLog(log: SecurityLog)
}
