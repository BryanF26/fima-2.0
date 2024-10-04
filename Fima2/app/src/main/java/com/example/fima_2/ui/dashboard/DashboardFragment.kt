package com.example.fima_2.ui.dashboard

import WeatherResponse
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fima_2.AttendanceActivity
import com.example.fima_2.LeaveRequestActivity
import com.example.fima_2.databinding.FragmentDashboardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar

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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        val calendar = Calendar.getInstance()
        val sdfClock = SimpleDateFormat("HH:mm:ss")
        clockTV.text = sdfClock.format(calendar.time)
        val sdfDate = SimpleDateFormat("EEE dd MMMM yyyy")
        dateTV.text = sdfDate.format(calendar.time)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Check permissions and get location
        getLocation()
        fetchWeather()
        clockInBtn.setOnClickListener {
            val intent = Intent(activity, AttendanceActivity::class.java)

            // Optionally, add some data to the intent
            intent.putExtra("EXTRA_KEY", "Some data")

            // Start the target activity
            startActivity(intent)
        }
//        clockInTimeTV.text =
//        clockOutTimeTV.text =
//        attendanceStatusTV.text =
        requestLeaveBtn.setOnClickListener {
            val intent = Intent(activity, LeaveRequestActivity::class.java)

            // Optionally, add some data to the intent
            intent.putExtra("EXTRA_KEY", "Some data")

            // Start the target activity
            startActivity(intent)
        }

//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
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

        val call = weatherService.getWeather("Jakarta", "8acc6f82522969085f144382938a7e5b")
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    // Update the UI with weather info
                    weather?.let {
                        temperatureTV.text = it.temp.toString()
                        humidityTV.text = it.humidity.toString()
                        weatherDescTV.text = it.description
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    fun getLocation() {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if they are not already granted
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Get last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Use the location here
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(requireContext(), "Lat: $latitude, Lon: $longitude", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted, proceed to get the location
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}