package com.example.weatherbackend.controller;

import com.example.weatherbackend.exceptions.WeatherDataNotFoundException;
import com.example.weatherbackend.model.DailyForecast;
import com.example.weatherbackend.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            response.put("daily", forecast);
            response.put("daily_units", weatherService.getDailyUnits());
            return ResponseEntity.ok(response);
        } catch (WeatherDataNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
