package com.example.fima_2.ui.dashboard

import Attendance
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _attendance = MutableLiveData<Attendance>()
    val attendance: LiveData<Attendance> = _attendance

    init {
        // Load historical data for currency rates (could be from API, database, etc.)
        loadAttedance()
    }

    private fun loadAttedance() {
        // Simulated currency rate history data
        _attendance.value = Attendance("08:00:00", "17:15:00", "On Time")
    }

    fun editAttendance(clockInTime: String,clockOutTime: String, attendanceStatus: String) {
        val currentList = Attendance(clockInTime, clockOutTime, attendanceStatus)
        _attendance.value = currentList
    }
}