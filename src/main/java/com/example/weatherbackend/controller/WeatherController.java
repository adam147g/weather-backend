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

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherController {
    @Autowired
    private WeatherService weatherService;

    @GetMapping("/7-day-forecast")
    public ResponseEntity<Map<String, Object>> get7DayForecast(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            if (latitude > 90 || latitude < -90 || longitude > 180 || longitude < -180) {
                throw new InvalidInputException("Szerokość geograficzna musi mieścić się w przedziale [-90, 90], długość geograficzna w przedziale [-180, 180]");
            }

            List<DailyForecast> forecast = weatherService.get7DayForecast(latitude, longitude);
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
    public ResponseEntity<Map<String, Object>> getWeekSummary(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                throw new InvalidInputException("Latitude must be in range [-90, 90], Longitude must be in range [-180, 180]");
            }

            WeatherSummary summary = weatherService.getWeekSummary(latitude, longitude);

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
}
