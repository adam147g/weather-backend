package com.example.weatherbackend.service;

import com.example.weatherbackend.exceptions.WeatherDataNotFoundException;
import com.example.weatherbackend.model.DailyForecast;
import com.example.weatherbackend.model.WeatherResponse;
import com.example.weatherbackend.model.WeatherSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
        try {
            String string_latitude = String.format("%.6f", latitude).replace(",", ".");
            String string_longitude = String.format("%.6f", longitude).replace(",", ".");

            String url = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=weather_code,temperature_2m_min,temperature_2m_max,sunshine_duration&timezone=auto",
                    string_latitude, string_longitude);

            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            if (response == null) {
                throw new WeatherDataNotFoundException("Nie udało się pobrać danych pogodowych.");
            }
            return processWeatherData(response);
        } catch (RestClientException e) {
            throw new WeatherDataNotFoundException("Błąd połączenia z zewnętrznym API: " + e.getMessage());
        }
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

    public WeatherSummary getWeekSummary(double latitude, double longitude) {
        try {
            String string_latitude = String.format("%.6f", latitude).replace(",", ".");
            String string_longitude = String.format("%.6f", longitude).replace(",", ".");

            String url = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=surface_pressure&daily=temperature_2m_max,temperature_2m_min,sunshine_duration,rain_sum&timezone=auto",
                    string_latitude, string_longitude);

            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            if (response == null) {
                throw new WeatherDataNotFoundException("Nie udało się pobrać danych pogodowych.");
            }
            return processWeeklyData(response);
        } catch (RestClientException e) {
            throw new WeatherDataNotFoundException("Błąd połączenia z zewnętrznym API: " + e.getMessage());
        }
    }

    private WeatherSummary processWeeklyData(WeatherResponse response) {
        WeatherResponse.Daily daily = response.daily();
        WeatherResponse.Hourly hourly = response.hourly();
        double averageSurface = hourly.surface_pressure().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double averageSunshineDuration = 0;
        double minTemperature = Double.MAX_VALUE;
        double maxTemperature = Double.MIN_VALUE;
        double rainyDays = 0;
        for (int i = 0; i < daily.time().size(); i++) {
            averageSunshineDuration += daily.sunshine_duration().get(i);
            minTemperature = Math.min(minTemperature, daily.temperature_2m_min().get(i));
            maxTemperature = Math.max(maxTemperature, daily.temperature_2m_max().get(i));
            if (daily.rain_sum().get(i) > 0)
                rainyDays++;
        }
        averageSunshineDuration /= daily.time().size();
        return new WeatherSummary(round(averageSurface, 4), round(averageSunshineDuration, 2), minTemperature, maxTemperature, rainyDays > 3 ? "Rainy" : "Not rainy");
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

    public WeatherResponse.WeeklySummaryUnits getWeeklySummaryUnits() {
        return new WeatherResponse.WeeklySummaryUnits();
    }
}
