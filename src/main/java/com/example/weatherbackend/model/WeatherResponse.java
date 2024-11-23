package com.example.weatherbackend.model;

import java.util.List;

public record WeatherResponse(
        Daily daily,
        DailyUnits daily_units
) {
    public record Daily(
            List<String> time,
            List<Integer> weather_code,
            List<Double> temperature_2m_min,
            List<Double> temperature_2m_max,
            List<Double> sunshine_duration
    ) {
    }

    public record DailyUnits(
            String date,
            String weatherCode,
            String minTemperature,
            String maxTemperature,
            String estimatedEnergy
    ) {
        public DailyUnits() {
            this("YYYY-MM-DD", "wmo code", "°C", "°C", "kWh");
        }
    }
}

