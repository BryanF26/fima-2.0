package com.example.fima_2.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _attendance = MutableLiveData<Attendance>()
    val attendance: LiveData<Attendance> get() = _attendance

//    init {
//        // Load historical data for currency rates (could be from API, database, etc.)
//        loadAttedance()
//    }
//
//    private fun loadAttedance() {
//        // Simulated currency rate history data
//        _attendance.value = Attendance("null1", "null2", "null3")
//    }

    fun editAttendance(clockInTime: String?,clockOutTime: String?, attendanceStatus: String?) {
        val temp = Attendance(clockInTime, clockOutTime, attendanceStatus)
        Log.d("exit3", "${temp.clockInTime}")
        _attendance.value = temp
    }
}