package weather;

import java.io.IOException;
import java.util.OptionalDouble;

//точка входа
public class Main {

    private static final double DEFAULT_LAT = 55.75;
    private static final double DEFAULT_LON = 37.62;
    private static final int DEFAULT_LIMIT = 5;

    public static void main(String[] args) {
        String apiKey = System.getenv("YANDEX_WEATHER_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            exitWithError("Не задан ключ API. Укажите его в переменной окружения YANDEX_WEATHER_KEY.");
        }

        double lat;
        double lon;
        int limit;
        try {
            lat = args.length > 0 ? Double.parseDouble(args[0]) : DEFAULT_LAT;
            lon = args.length > 1 ? Double.parseDouble(args[1]) : DEFAULT_LON;
            limit = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_LIMIT;
        } catch (NumberFormatException e) {
            exitWithError("Неверные параметры. Формат: [lat] [lon] [limit], например: 55.75 37.62 5");
            return;
        }
        if (limit < 1) {
            exitWithError("limit должен быть больше 0");
        }

        WeatherClient client = new WeatherClient(apiKey);
        try {
            String json = client.getForecast(lat, lon, limit);

            System.out.println("=== ответ сервиса ===");
            System.out.println(json);
            System.out.println();

            ForecastAnalyzer analyzer = new ForecastAnalyzer(json);

            OptionalDouble currentTemp = analyzer.getCurrentTemp();
            if (currentTemp.isPresent()) {
                System.out.printf("Текущая температура (fact.temp): %.0f °C%n", currentTemp.getAsDouble());
            } else {
                System.out.println("Текущая температура (fact.temp): нет данных в ответе");
            }

            OptionalDouble avgTemp = analyzer.getAverageDayTemp(limit);
            if (avgTemp.isPresent()) {
                System.out.printf("Средняя температура за %d сут. (forecasts[*].parts.day.temp_avg): %.1f °C%n",
                        limit, avgTemp.getAsDouble());
            } else {
                System.out.println("Средняя температура: в ответе нет данных прогноза");
            }
        } catch (WeatherApiException | JsonParseException e) {
            exitWithError(e.getMessage());
        } catch (IOException e) {
            exitWithError("Ошибка сети: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exitWithError("Запрос прерван");
        }
    }

    private static void exitWithError(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
