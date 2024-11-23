package com.example.weatherbackend.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
public class WeatherResponse {
    private Daily daily;

    @Data
    @NoArgsConstructor
    public static class Daily {
        private List<String> time;
        private List<Integer> weather_code;
        private List<Double> temperature_2m_min;
        private List<Double> temperature_2m_max;
        private List<Double> sunshine_duration;
    }
}
