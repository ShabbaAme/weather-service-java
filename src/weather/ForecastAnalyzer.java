package weather;

import java.util.List;
import java.util.OptionalDouble;

public class ForecastAnalyzer {

    private final Object root;

    public ForecastAnalyzer(String json) throws JsonParseException {
        this.root = JsonParser.parse(json);
    }


    public OptionalDouble getCurrentTemp() {
        Object temp = JsonParser.get(root, "fact", "temp");
        return temp instanceof Number
                ? OptionalDouble.of(((Number) temp).doubleValue())
                : OptionalDouble.empty();
    }

   
    public OptionalDouble getAverageDayTemp(int limit) {
        Object forecasts = JsonParser.get(root, "forecasts");
        if (!(forecasts instanceof List)) {
            return OptionalDouble.empty();
        }
        List<?> days = (List<?>) forecasts;

        int count = Math.min(limit, days.size());
        double sum = 0;
        int found = 0;
        for (int i = 0; i < count; i++) {
            Object temp = JsonParser.get(days.get(i), "parts", "day", "temp_avg");
            if (temp instanceof Number) {
                sum += ((Number) temp).doubleValue();
                found++;
            }
        }
        return found == 0 ? OptionalDouble.empty() : OptionalDouble.of(sum / found);
    }
}
