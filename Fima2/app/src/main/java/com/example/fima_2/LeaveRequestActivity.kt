package com.example.fima_2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.fima_2.database.DatabaseHelper

class LeaveRequestActivity : AppCompatActivity() {

    private lateinit var startDateET: EditText
    private lateinit var leaveDateET: EditText
    private lateinit var leaveRequestBtn: Button
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leave_request)

        // Initialize views
        startDateET = findViewById(R.id.startDateET)
        leaveDateET = findViewById(R.id.leaveDateET)
        leaveRequestBtn = findViewById(R.id.leaveRequestBtn)

        // Initialize the database helper
        databaseHelper = DatabaseHelper(this)
        // Set up the button click listener
        leaveRequestBtn.setOnClickListener {
            submitLeaveRequest()
        }
    }

    private fun submitLeaveRequest() {
        // Get input values
        val startDate = startDateET.text.toString()
        val leaveDate = leaveDateET.text.toString()

        // Validate input
        if (startDate.isNotBlank() && leaveDate.isNotBlank()) {
            // Insert the leave request into the database
            val result = databaseHelper.insertLeaveRequest(startDate, leaveDate, "Leave request") // Assuming the reason is fixed or you may add another EditText for it

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
    }
}
