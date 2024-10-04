package com.example.fima_2.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.fima_2.AttendanceActivity
import com.example.fima_2.LeaveRequestActivity
import com.example.fima_2.databinding.FragmentDashboardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private lateinit var clockTV: TextView
    private lateinit var dateTV: TextView
    private lateinit var temperatureTV: TextView
    private lateinit var humidityTV: TextView
    private lateinit var weatherDescTV: TextView
    private lateinit var clockInBtn: Button
    private lateinit var clockInTimeTV: TextView
    private lateinit var clockOutTimeTV: TextView
    private lateinit var attendanceStatusTV: TextView
    private lateinit var requestLeaveBtn: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var clockHandler: Handler
    private lateinit var clockRunnable: Runnable
    private lateinit var sharedPreferences: SharedPreferences


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var lat:Double = 0.0
    private var lon:Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clockTV = binding.clockTV
        dateTV = binding.dateTV
        temperatureTV = binding.temperatureTV
        humidityTV = binding.humidityTV
        weatherDescTV = binding.weatherDescTV
        clockInBtn = binding.clockInBtn
        clockInTimeTV = binding.clockInTimeTV
        clockOutTimeTV = binding.clockOutTimeTV
        attendanceStatusTV = binding.attendanceStatusTV
        requestLeaveBtn = binding.requestLeaveBtn
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        clockHandler = Handler(Looper.getMainLooper())
        clockRunnable = object : Runnable {
            override fun run() {
                val calendar = Calendar.getInstance()
                val sdfClock = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                clockTV.text = sdfClock.format(calendar.time)
                clockHandler.postDelayed(this, 1000) // Update every second
            }
        }
        clockHandler.post(clockRunnable)

        val calendar = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("EEE dd MMMM yyyy", Locale.getDefault())
        dateTV.text = sdfDate.format(calendar.time)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requestLocationUpdates()
        fetchWeather()

        val clockInTimeString = sharedPreferences.getString("clockInTimeString", "-")
        val clockOutTimeString = sharedPreferences.getString("clockOutTimeString", "-")
        val attendanceStatus = sharedPreferences.getString("attendanceStatus", "absent")

        val toast = sharedPreferences.getString("toast","")
        if(toast.equals("clockOut")){
            Toast.makeText(requireContext(),"Succesfully clock out at ${clockOutTimeString}",Toast.LENGTH_SHORT).show()
        } else if(toast.equals("clockIn")){
            Toast.makeText(requireContext(),"Succesfully clock out at ${clockInTimeString}",Toast.LENGTH_SHORT).show()
        } else if(toast.equals("error")){
            Toast.makeText(requireContext(),"You already completed clock in and clock out",Toast.LENGTH_SHORT).show()
        }
        val editor = sharedPreferences.edit()
        editor.putString("toast", "")
        editor.apply()

        clockInTimeTV.text = clockInTimeString
        clockOutTimeTV.text = clockOutTimeString
        attendanceStatusTV.text = attendanceStatus
        // Check permissions and get location
        clockInBtn.setOnClickListener {
            val intent = Intent(activity, AttendanceActivity::class.java)
            // Start the target activity
            startActivity(intent)
        }

        requestLeaveBtn.setOnClickListener {
            val intent = Intent(activity, LeaveRequestActivity::class.java)
            // Start the target activity
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun fetchWeather() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherApiService::class.java)

        val call = weatherService.getWeather(lat.toString(),lon.toString(), "8acc6f82522969085f144382938a7e5b")
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    Log.d("weather", "$weather")
                    // Update the UI with weather info
                    weather?.let {
                        temperatureTV.text = "${it.main.temp} K"
                        humidityTV.text = it.main.humidity.toString()
                        weatherDescTV.text = it.weather.first().description
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Handle failure
                temperatureTV.text = "N/A"
                humidityTV.text = "N/A"
                weatherDescTV.text = "N/A"
            }
        })
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted, proceed to get the location
                requestLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    lat = location.latitude
                    lon = location.longitude
                    val editor = sharedPreferences.edit()
                    editor.putString("lat", "$lat")
                    editor.putString("lon", "$lon")
                    editor.apply()
                    Log.d("weather", "$lat $lon")
                }
            }
        }, Looper.getMainLooper())
    }

}