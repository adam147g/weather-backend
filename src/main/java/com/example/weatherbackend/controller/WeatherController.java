package com.example.weatherbackend.controller;

import com.example.weatherbackend.exceptions.WeatherDataNotFoundException;
import com.example.weatherbackend.model.DailyForecast;
import com.example.weatherbackend.model.WeatherSummary;
import com.example.weatherbackend.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    @Autowired
    private WeatherService weatherService;

    @GetMapping("/7-day-forecast")
    public ResponseEntity<Map<String, Object>> get7DayForecast(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            List<DailyForecast> forecast = weatherService.get7DayForecast(latitude, longitude);
            Map<String, Object> response = new HashMap<>();
            response.put("days", forecast);
            response.put("daily_units", weatherService.getDailyUnits());
            return ResponseEntity.ok(response);
        } catch (WeatherDataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<Map<String, Object>> getWeekSummary(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            WeatherSummary summary = weatherService.getWeekSummary(latitude, longitude);
            Map<String, Object> response = new HashMap<>();
            response.put("weekly_summary", summary);
            response.put("weekly_summary_units", weatherService.getWeeklySummaryUnits());
            return ResponseEntity.ok(response);
        } catch (WeatherDataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
