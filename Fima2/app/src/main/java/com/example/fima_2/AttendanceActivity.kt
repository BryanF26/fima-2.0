package com.example.fima_2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.fima_2.R
import com.example.fima_2.ui.dashboard.Attendance
import com.example.fima_2.ui.dashboard.DashboardViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var viewFinder: PreviewView
    private lateinit var capturedImageView: ImageView
    private lateinit var outputDirectory: File
    private lateinit var captureButton: Button
    private lateinit var clockButton: Button

    private lateinit var dashboardViewModel: DashboardViewModel
    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 200
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_attendance)

        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        // Initialize viewFinder
        viewFinder = findViewById(R.id.capturedImagePreview)  // Ensure the ID matches your XML
        clockButton =  findViewById(R.id.attendanceSubmitButton)
        captureButton = findViewById(R.id.takePhotoButton)
        capturedImageView = findViewById(R.id.capturedImageView)


        // Request permissions and start camera
        requestCameraPermissions()
        captureButton.setOnClickListener { onCaptureButtonClick() }

        // Create the output directory for photos
        outputDirectory = getOutputDirectory()

        clockButton.setOnClickListener {

            clockInOut(dashboardViewModel)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent    )
        }


    }

    private fun requestCameraPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST_CODE)
    }

    private fun onCaptureButtonClick() {
        if (captureButton.text == "Capture") {
            takePhoto()
        } else {
            // If retaking, restart camera and reset UI
            startCamera()
            captureButton.text = "Capture"
            capturedImageView.visibility = View.GONE
            viewFinder.visibility = View.VISIBLE
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                // Handle permission denial
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                // Handle error
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = File(outputDirectory, SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                // Handle error
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                capturedImageView.setImageURI(savedUri)
                capturedImageView.visibility = View.VISIBLE
                viewFinder.visibility = View.GONE
                captureButton.text = "Retake"
            }
        })
    }


    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }



    fun clockInOut(dashboardViewModel: DashboardViewModel){
//        val dashboardViewModel =
//            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        // Update the adapter with new data

        val sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var clockInTimeString = sharedPreferences.getString("clockInTimeString", null)
        var clockOutTimeString = sharedPreferences.getString("clockOutTimeString", null)

        val calendar = Calendar.getInstance()
        if(clockInTimeString == null) {
            clockInTimeString = formatter.format(calendar.time).toString()
            editor.putString("toast", "clockIn")
        } else if (clockOutTimeString == null){
            clockOutTimeString = formatter.format(calendar.time).toString()
            editor.putString("toast", "clockOut")
        } else {
            editor.putString("toast", "error")
            editor.apply()
            return
        }
        val clockInTime: Date? = clockInTimeString?.let { formatter.parse(it) }
        val clockOutTime: Date? = clockOutTimeString?.let { formatter.parse(it) }


        val attendanceStatus: String = when {
            // Case 1: ClockInTime or ClockOutTime is null
            clockInTime == null || clockOutTime == null -> {
                "Absent"
            }

            // Case 2: On time (Clock in <= 08:00:00 and Clock out >= 17:00:00)
            clockInTime <= formatter.parse("08:00:00") && clockOutTime >= formatter.parse("17:00:00") -> {
                "OnTime"
            }

            // Case 3: Late Come (ClockInTime > 08:00:00)
            clockInTime > formatter.parse("08:00:00") -> {
                "Late Come"
            }

            // Case 4: Early Leave (ClockOutTime < 17:00:00)
            clockOutTime < formatter.parse("17:00:00") -> {
                "Early Leave"
            }

            // Default case: Handle unexpected scenarios
            else -> {
                "Invalid"
            }
        }

        editor.putString("clockInTimeString", clockInTimeString)
        editor.putString("clockOutTimeString", clockOutTimeString)
        editor.putString("attendanceStatus", attendanceStatus)
        editor.apply()
//        dashboardViewModel.editAttendance(clockInTimeString, clockOutTimeString, attendanceStatus)
//        Log.d("exit", "${dashboardViewModel.attendance.value?.clockInTime}")
    }
}
