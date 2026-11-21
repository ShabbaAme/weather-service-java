package weather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class WeatherClient {

    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String KEY_HEADER = "X-Yandex-Weather-Key";

    private final String apiKey;
    private final HttpClient httpClient;

    public WeatherClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

 
    public String getForecast(double lat, double lon, int limit)
            throws IOException, InterruptedException, WeatherApiException {
        String url = API_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(KEY_HEADER, apiKey)
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new WeatherApiException(
                    "Сервис вернул код " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }
}
