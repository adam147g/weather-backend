package com.example.weatherbackend.service;

import com.example.weatherbackend.exceptions.WeatherDataNotFoundException;
import com.example.weatherbackend.model.DailyForecast;
import com.example.weatherbackend.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {
    private static final double SOLAR_PANEL_POWER = 2.5; // kW
    private static final double PANEL_EFFICIENCY = 0.2; // 20%

    @Autowired
    private RestTemplate restTemplate;

    public List<DailyForecast> get7DayForecast(double latitude, double longitude) {
        String string_latitude = String.format("%.6f", latitude).replace(",", ".");
        String string_longitude = String.format("%.6f", longitude).replace(",", ".");

        String url = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=weather_code,temperature_2m_min,temperature_2m_max,sunshine_duration&timezone=auto",
                string_latitude, string_longitude);

        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
        if (response == null) {
            throw new WeatherDataNotFoundException("Nie udało się pobrać danych pogodowych.");
        }
        return processWeatherData(response);
    }

    private List<DailyForecast> processWeatherData(WeatherResponse response) {
        List<DailyForecast> forecasts = new ArrayList<>();
        WeatherResponse.Daily daily = response.daily();

        for (int i = 0; i < daily.time().size(); i++) {
            double sunshineHours = daily.sunshine_duration().get(i) / 3600; // sekundy na godziny

            double estimatedEnergy = SOLAR_PANEL_POWER * sunshineHours * PANEL_EFFICIENCY;
            estimatedEnergy = round(estimatedEnergy, 3);
            forecasts.add(new DailyForecast(
                    daily.time().get(i),
                    daily.weather_code().get(i),
                    daily.temperature_2m_min().get(i),
                    daily.temperature_2m_max().get(i),
                    estimatedEnergy
            ));
        }

        return forecasts;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public WeatherResponse.DailyUnits getDailyUnits() {
        return new WeatherResponse.DailyUnits();
    }
}
