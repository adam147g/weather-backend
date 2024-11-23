package com.example.weatherbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyForecast {
    private String date;
    private int weatherCode;
    private double minTemperature;
    private double maxTemperature;
    private double estimatedEnergy;
}