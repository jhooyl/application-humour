package com.example.humour

data class WeatherResponse(
    val main: Main,
    val name: String
)

data class Main(
    val temp: Double,
    val temp_min: Double,
    val temp_max: Double
)