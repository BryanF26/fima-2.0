package com.example.fima_2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fima_2.database.DatabaseHelper
import java.util.Calendar

class LeaveRequestActivity : AppCompatActivity() {

    private lateinit var startDateET: EditText
    private lateinit var leaveDateET: EditText
    private lateinit var leaveRequestBtn: Button
    private lateinit var leaveReasonET: EditText
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_leave_request)

        // Initialize views
        startDateET = findViewById(R.id.startDateET)
        leaveDateET = findViewById(R.id.leaveDateET)
        leaveReasonET = findViewById(R.id.leaveReasonET)
        leaveRequestBtn = findViewById(R.id.leaveRequestBtn)

        // Initialize the database helper
        databaseHelper = DatabaseHelper(this)

        startDateET.setOnClickListener {
            showDatePickerDialog(startDateET)
        }

        // Set DatePicker for leaveDateET
        leaveDateET.setOnClickListener {
            showDatePickerDialog(leaveDateET)
        }
        // Set up the button click listener
        leaveRequestBtn.setOnClickListener {
            submitLeaveRequest()
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create and show DatePickerDialog
        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date as dd/MM/yy
                val selectedDate = String.format("%02d/%02d/%02d", selectedDay, selectedMonth + 1, selectedYear % 100)
                editText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun submitLeaveRequest() {
        // Get input values
        val startDate = startDateET.text.toString()
        val leaveDate = leaveDateET.text.toString()
        val leaveReason = leaveReasonET.text.toString()

        // Validate input
        if (startDate.isNotBlank() && leaveDate.isNotBlank() && leaveReason.isNotBlank()) {
            // Insert the leave request into the database
            val result = databaseHelper.insertLeaveRequest(startDate, leaveDate, leaveReason) // Assuming the reason is fixed or you may add another EditText for it

            if (result != -1L) {
                Toast.makeText(this, "Leave request submitted", Toast.LENGTH_SHORT).show()
                clearForm()
            } else {
                Toast.makeText(this, "Failed to submit leave request", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        startDateET.text.clear()
        leaveDateET.text.clear()
        leaveReasonET.text.clear()
    }
}
