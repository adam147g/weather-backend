package com.example.weatherbackend.controller;

import com.example.weatherbackend.exceptions.InvalidInputException;
import com.example.weatherbackend.exceptions.WeatherDataNotFoundException;
import com.example.weatherbackend.model.DailyForecast;
import com.example.weatherbackend.model.WeatherSummary;
import com.example.weatherbackend.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"https://weather-frontend-vqf8.onrender.com", "http://localhost:3000"})
@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/7-day-forecast")
    public ResponseEntity<Map<String, Object>> get7DayForecast(
            @RequestParam String latitude,
            @RequestParam String longitude) {
        try {
            // Sprawdzanie i walidacja dla szerokości i długości geograficznej
            double lat = parseAndValidateCoordinate(latitude, "Szerokość geograficzna", -90.0, 90.0);
            double lon = parseAndValidateCoordinate(longitude, "Długość geograficzna", -180.0, 180.0);


            List<DailyForecast> forecast = weatherService.get7DayForecast(lat, lon);
            Map<String, Object> response = new HashMap<>();
            response.put("days", forecast);
            response.put("daily_units", weatherService.getDailyUnits());

            return ResponseEntity.ok(response);

        } catch (WeatherDataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (InvalidInputException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<Map<String, Object>> getWeekSummary(
            @RequestParam String latitude,
            @RequestParam String longitude) {
        try {
            // Sprawdzanie i walidacja dla szerokości i długości geograficznej
            double lat = parseAndValidateCoordinate(latitude, "Szerokość geograficzna", -90.0, 90.0);
            double lon = parseAndValidateCoordinate(longitude, "Długość geograficzna", -180.0, 180.0);

            WeatherSummary summary = weatherService.getWeekSummary(lat, lon);

            Map<String, Object> response = new HashMap<>();
            response.put("weekly_summary", summary);
            response.put("weekly_summary_units", weatherService.getWeeklySummaryUnits());

            return ResponseEntity.ok(response);
        } catch (WeatherDataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (InvalidInputException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Metoda pomocnicza do parsowania i walidacji współrzędnych
    private double parseAndValidateCoordinate(String coordinate, String coordinateName, double minValue, double maxValue) throws InvalidInputException {
        try {
            double value = Double.parseDouble(coordinate);

            // Walidacja zakresu
            if (value < minValue || value > maxValue) {
                throw new InvalidInputException(coordinateName + " musi być w przedziale [" + minValue + ", " + maxValue + "]");
            }

            return value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(coordinateName + " musi być liczbą zmiennoprzecinkową.");
        }
    }
}