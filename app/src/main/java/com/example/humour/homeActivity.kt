package com.example.humour

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.humour.databinding.ActivityHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val apiKey = "832824ccfea675ee8800116357930bdf"
    private val city = "Paris" // Vous pouvez changer la ville

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateDate()
        fetchRealWeather()
        setupNavigation()
    }

    private fun updateDate() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.dateTextView.text = currentDate
    }

    private fun fetchRealWeather() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.weatherApiService.getWeather(
                    city = city,
                    units = "metric",
                    apiKey = apiKey
                )

                runOnUiThread {
                    val minTemp = response.main.temp_min.toInt()
                    val maxTemp = response.main.temp_max.toInt()

                    binding.temperatureTextView.text = "$minTemp°C - $maxTemp°C"
                    binding.weatherMessageTextView.text = getWeatherMessage(minTemp, maxTemp)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    // En cas d'erreur
                    binding.temperatureTextView.text = "Error"
                    binding.weatherMessageTextView.text = "Unable to fetch weather data. Check your API key."
                }
            }
        }
    }

    private fun getWeatherMessage(minTemp: Int, maxTemp: Int): String {
        val averageTemp = (minTemp + maxTemp) / 2

        return when {
            averageTemp < 0 -> "Brrr! It's freezing! Stay warm with a hot drink."
            averageTemp in 0..5 -> "Very cold! Perfect for hot chocolate under a blanket."
            averageTemp in 6..12 -> "A bit chilly! Ideal for reading a good book inside."
            averageTemp in 13..18 -> "Pleasant temperature! Perfect for a little walk outside."
            averageTemp in 19..24 -> "Nice and warm! Great time to go out and relax."
            averageTemp in 25..30 -> "It's hot! A good time for outdoor activities."
            else -> "It's very hot! Stay hydrated and seek shade."
        }
    }

    private fun setupNavigation() {
        binding.homeButton.setOnClickListener {
            // Already on Home
        }

        binding.statsButton.setOnClickListener {
            // Launch stats activity
        }

        binding.historyButton.setOnClickListener {
            // Launch history activity
        }
    }
}