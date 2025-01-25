package com.example.notebook.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.notebook.R
import com.example.notebook.databinding.FragmentWeatherBinding
import com.example.retrofitweather.model.WeatherInfo
import com.example.retrofitweather.retrofit.RetrofitHelper
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocationAndShowWeather()
        } else {
            Toast.makeText(requireActivity(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refreshBTN.setOnClickListener {
            checkPermissionAndGetWeather()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("VV", "onResume")
        checkPermissionAndGetWeather()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun getWeather(lat: Double, lon: Double): WeatherResult {
        val apiKey = requireContext().getString(R.string.apiWeatherKey)
        val response = try {
                    RetrofitHelper.api.getWeatherForCoordinates(
                        longitude = lon,
                        latitude = lat,
                        apiKey = apiKey
                    )
        } catch (e: IOException) {
            return WeatherResult.Error("IO exception")
        } catch (e: HttpException) {
            return WeatherResult.Error("Http exception")
        }
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            WeatherResult.Success(body)
        } else {
            WeatherResult.Error("Can't get result. Check city or coordinates")
        }
    }

    private fun checkPermissionAndGetWeather(){
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocationAndShowWeather()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndShowWeather() {
        val location = LocationServices.getFusedLocationProviderClient(requireActivity())
            .lastLocation.addOnSuccessListener { location ->
                loadWeather(location.latitude, location.longitude)
            }
    }

    private fun loadWeather(latitude: Double, longitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            setLoading()
            val result = withContext(Dispatchers.IO) {
                getWeather(latitude, longitude)
            }
            when(result){
                is WeatherResult.Error -> setError(result.message)
                is WeatherResult.Success -> setWeather(result.info)
            }
        }
    }

    fun setError(message: String) {
        binding.apply {
            progressIndicatorLPI.visibility = View.GONE
            infoSV.visibility = View.GONE
            errorTV.visibility = View.VISIBLE
            errorTV.text = message
        }
    }

    fun setLoading(){
        binding.apply {
            progressIndicatorLPI.visibility = View.VISIBLE
            infoSV.visibility = View.GONE
            errorTV.visibility = View.GONE
        }
    }

    fun setWeather(info: WeatherInfo) {
        binding.apply {
            progressIndicatorLPI.visibility = View.GONE
            infoSV.visibility = View.VISIBLE
            errorTV.visibility = View.GONE
            locationTV.text = info.name
            descTV.text = info.weather[0].description
            tempTV.text = "Temp: ${info.main.temp.toString()}C"
            minTempTV.text = "Temp min: ${info.main.tempMin.toString()}C"
            maxTempTV.text = "Temp max: ${info.main.tempMax.toString()}C"
            val iconId = info.weather[0].icon
            val url = "https://openweathermap.org/img/wn/${iconId}@4x.png"
            Picasso.get().load(url).into(iconIV)
            humidityTV.text = "Humidity: ${info.main.humidity}"
            pressureTV.text = "Pressure: ${info.main.pressure}"
        }
    }


    sealed interface WeatherResult {
        data class Error(val message: String): WeatherResult
        data class Success(val info: WeatherInfo): WeatherResult
    }
}