package com.example.weatherbackend;

import com.example.weatherbackend.exceptions.WeatherDataNotFoundException;
import com.example.weatherbackend.model.DailyForecast;
import com.example.weatherbackend.model.WeatherResponse;
import com.example.weatherbackend.model.WeatherSummary;
import com.example.weatherbackend.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WeatherServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherService weatherService;

    private WeatherResponse weatherResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        weatherResponse = new WeatherResponse(
                new WeatherResponse.Daily(
                        Arrays.asList("2024-11-20", "2024-11-21", "2024-11-22"),
                        Arrays.asList(1, 2, 1),
                        Arrays.asList(5.0, 6.0, 7.0),
                        Arrays.asList(15.0, 16.0, 17.0),
                        Arrays.asList(36000.0, 30000.0, 42000.0),
                        Arrays.asList(0.0, 1.0, 0.0)
                ),
                new WeatherResponse.Hourly(
                        Arrays.asList(1013.0, 1015.0, 1017.0)
                )
        );
    }

    @Test
    public void testGet7DayForecast_validResponse() {
        // Stworzenie mockowanych argumentów
        String anyUrl = anyString();
        Class<WeatherResponse> responseType = eq(WeatherResponse.class);

        // Ustawienie zachowania mocka
        when(restTemplate.getForObject(anyUrl, responseType))
                .thenReturn(weatherResponse);

        List<DailyForecast> forecasts = weatherService.get7DayForecast(52.2298, 21.0118);

        assertNotNull(forecasts);
        assertEquals(3, forecasts.size());

        // Dane dla każdego z dni
        String[] expectedDates = {"2024-11-20", "2024-11-21", "2024-11-22"};
        int[] expectedWeatherCodes = {1, 2, 1};
        double[] expectedMinTemperatures = {5.0, 6.0, 7.0};
        double[] expectedMaxTemperatures = {15.0, 16.0, 17.0};
        double[] expectedEstimatedEnergy = {5.0, 4.167, 5.834};

        // Weryfikacja każdego dnia
        for (int i = 0; i < forecasts.size(); i++) {
            DailyForecast forecast = forecasts.get(i);
            assertEquals(expectedDates[i], forecast.date());
            assertEquals(expectedWeatherCodes[i], forecast.weatherCode());
            assertEquals(expectedMinTemperatures[i], forecast.minTemperature(), 0.01);
            assertEquals(expectedMaxTemperatures[i], forecast.maxTemperature(), 0.01);
            assertEquals(expectedEstimatedEnergy[i], forecast.estimatedEnergy(), 0.01);
        }
    }

    @Test
    public void testGet7DayForecast_apiReturnsNull() {
        // Przygotowanie matchera dla URL-a
        String anyUrl = anyString();

        // Przygotowanie matchera dla typu odpowiedzi
        Class<WeatherResponse> responseType = eq(WeatherResponse.class);

        // Ustawienie zachowania mocka dla przypadku, gdy API zwraca null
        when(restTemplate.getForObject(anyUrl, responseType))
                .thenReturn(null);

        // Sprawdzenie, czy rzucany jest wyjątek
        assertThrows(WeatherDataNotFoundException.class, () -> weatherService.get7DayForecast(52.2298, 21.0118));
    }

    @Test
    public void testGetWeekSummary_validResponse() {
        // Przygotowanie matchera dla URL-a
        String anyUrl = anyString();

        // Przygotowanie matchera dla typu odpowiedzi
        Class<WeatherResponse> responseType = eq(WeatherResponse.class);

        // Ustawienie zachowania mocka dla poprawnej odpowiedzi
        when(restTemplate.getForObject(anyUrl, responseType))
                .thenReturn(weatherResponse);

        WeatherSummary summary = weatherService.getWeekSummary(52.2298, 21.0118);

        assertNotNull(summary);
        assertEquals(1015.0, summary.averageSurfacePressure());
        assertEquals(36000.0, summary.averageSunshineDuration());
        assertEquals(5.0, summary.minTemperature());
        assertEquals(17.0, summary.maxTemperature());
        assertEquals("Not rainy", summary.weatherSummary());
    }

    @Test
    public void testRound() {
        double result = WeatherService.round(2.5555, 2);
        assertEquals(2.56, result);

        result = WeatherService.round(2.5544, 3);
        assertEquals(2.554, result);
    }

    @Test
    public void testGetDailyUnits() {
        WeatherResponse.DailyUnits units = weatherService.getDailyUnits();
        assertNotNull(units);
        assertEquals("YYYY-MM-DD", units.date());
        assertEquals("wmo code", units.weatherCode());
        assertEquals("°C", units.minTemperature());
        assertEquals("°C", units.maxTemperature());
        assertEquals("kWh", units.estimatedEnergy());
    }

    @Test
    public void testGetWeeklySummaryUnits() {
        WeatherResponse.WeeklySummaryUnits units = weatherService.getWeeklySummaryUnits();
        assertNotNull(units);
        assertEquals("hPa", units.averageSurfacePressure());
        assertEquals("s", units.averageSunshineDuration());
        assertEquals("°C", units.minTemperature());
        assertEquals("°C", units.maxTemperature());
    }
}
